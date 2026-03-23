package com.cryptocompare.data.repository

import android.util.Log
import com.cryptocompare.data.local.dao.ProviderDao
import com.cryptocompare.data.local.dao.SymbolDao
import com.cryptocompare.data.local.entity.SymbolEntity
import com.cryptocompare.data.mapper.toDomainFromEntity
import com.cryptocompare.data.mapper.toEntityFromDomain
import com.cryptocompare.data.mapper.toEntityFromDto
import com.cryptocompare.domain.repository.CryptoCompareRepository
import com.cryptocompare.helpers.util.Constants
import com.cryptocompare.model.Provider
import com.cryptocompare.model.Symbol
import com.cryptocompare.network.api.CryptoCompareApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlin.collections.orEmpty

@Singleton
class CryptoCompareRepositoryImpl
@Inject
constructor(
    private val cryptoCompareApi: CryptoCompareApi,
    private val symbolDao: SymbolDao,
    private val providerDao: ProviderDao,
    @Named("ioDispatcher") private val ioDispatcher: CoroutineDispatcher
) : CryptoCompareRepository {
    val refreshSymbolsScope = CoroutineScope(SupervisorJob() + ioDispatcher)
    var refreshSymbolsJob: Job? = null

    // PROVIDERS
    //get providers
    override suspend fun getProviders(): Result<List<Provider>> =
        withContext(ioDispatcher) {
            try {
                val providers = providerDao.getAll().toDomainFromEntity()
                val isCacheStale =
                    Constants.CryptoCompareRepositoryConstants.CATALOG_CACHE_TTL_MILLIS <= System.currentTimeMillis() - providerDao.getLastUpdate()

                if (providers.isNotEmpty() && !isCacheStale) Result.success(providers)
                else refreshProviders()

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val providers = providerDao.getAll().toDomainFromEntity()
                if (providers.isNotEmpty()) Result.success(providers)
                else Result.failure(e)

            }
        }
    // update providers
    private suspend fun refreshProviders(): Result<List<Provider>> =
        runCatching {
            val syncedAtMillis = System.currentTimeMillis()
            val response = cryptoCompareApi.getProviders()

            if (response.errorCode != 0) {
                val message = response.errorMsgs?.joinToString("\n") ?: "Unknown error"
                throw IllegalStateException(message)
            }

            val providers = response.providers.orEmpty().toEntityFromDomain(syncedAtMillis)
            providerDao.syncProviders(providers)
            providers.toDomainFromEntity()
        }.onFailure { error ->
            if (error is CancellationException) {
                throw error
            }
        }

    // SYMBOLS
    // get symbols
    override suspend fun getSymbols(): Flow<List<Symbol>> {
        val cachedSymbols = withContext(ioDispatcher) {
            symbolDao.getAll()
        }
        val isRefresh = cachedSymbols.isEmpty() ||  Constants.CryptoCompareRepositoryConstants.CATALOG_CACHE_TTL_MILLIS <= System.currentTimeMillis() - symbolDao.getLastUpdate()

        if (cachedSymbols.isEmpty()){
            withContext(ioDispatcher) {
                refreshSymbols().getOrThrow()
            }
        }else if (isRefresh){
            refreshJobStart()
        }

        return symbolDao.observeAll().map { symbolEntity -> symbolEntity.toDomainFromEntity() }.flowOn(ioDispatcher)
    }

    // start job to refresh symbols in background
    private fun refreshJobStart(){
        if (refreshSymbolsJob?.isActive == true) return

        refreshSymbolsJob = refreshSymbolsScope.launch {
            refreshSymbols()
        }
    }

    // update symbols
    private suspend fun refreshSymbols(): Result<List<Symbol>> =
        runCatching {
            var skip = 0
            val syncedAtMillis = System.currentTimeMillis()
            val refreshedSymbols = mutableListOf<SymbolEntity>()
            refreshProviders().getOrThrow()

            while (true) {
                val response = cryptoCompareApi.getSymbols(
                    skip = skip,
                    rows = Constants.CryptoCompareRepositoryConstants.SYMBOLS_IN_ROW
                )

                if (response.errorCode != 0) {
                    val message = response.errorMsgs?.joinToString("\n") ?: "Unknown error"
                    throw IllegalStateException(message)
                }

                val symbols = response.symbols.orEmpty()
                if (symbols.isEmpty()) break
                Log.d("CryptoRepo", response.toString())

                refreshedSymbols += symbols.toEntityFromDto(syncedAtMillis)
                skip += Constants.CryptoCompareRepositoryConstants.SYMBOLS_IN_ROW
            }


            symbolDao.syncSymbols(refreshedSymbols)
            refreshedSymbols.toDomainFromEntity()
        }.onFailure { error ->
            if (error is CancellationException) {
                throw error
            }

        }
}
