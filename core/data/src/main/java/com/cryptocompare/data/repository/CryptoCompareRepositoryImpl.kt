package com.cryptocompare.data.repository

import com.cryptocompare.data.local.dao.ProviderDao
import com.cryptocompare.data.local.dao.SymbolDao
import com.cryptocompare.data.local.entity.SymbolEntity
import com.cryptocompare.data.mapper.toDomainFromEntity
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
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.flowOn
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
        @Named("ioDispatcher") private val ioDispatcher: CoroutineDispatcher,
    ) : CryptoCompareRepository {
        private val refreshSymbolsScope = CoroutineScope(SupervisorJob() + ioDispatcher)
        private var refreshSymbolsJob: Job? = null

        // ─── Providers ────────────────────────────────────────────────────────────

        // get providers
        override suspend fun getProviders(): Result<List<Provider>> =
            withContext(ioDispatcher) {
                runCatching {
                    val providers = providerDao.getAll().toDomainFromEntity()

                    if (providers.isNotEmpty() && !isCacheStale(providerDao.getLastUpdate())) {
                        return@runCatching providers
                    }
                    refreshProviders()
                }.onFailure { error -> if (error is CancellationException) throw error }
                    .recoverCatching { error ->
                        val cached = providerDao.getAll().toDomainFromEntity()
                        cached.ifEmpty { throw error }
                    }
            }

        // update providers
        private suspend fun refreshProviders(): List<Provider> {
            val syncedAtMillis = System.currentTimeMillis()
            val response = cryptoCompareApi.getProviders()

            if (response.errorCode != 0) {
                val message = response.errorMsgs?.joinToString("\n") ?: "Unknown error"
                throw IllegalStateException(message)
            }

            val providers = response.providers.orEmpty().toEntityFromDto(syncedAtMillis)
            providerDao.syncProviders(providers)
            return providers.toDomainFromEntity()
        }

        // ─── Symbols ──────────────────────────────────────────────────────────────

        // get symbols
        override fun getSymbols(): Flow<List<Symbol>> =
            channelFlow {
                val cachedSymbols =
                    withContext(ioDispatcher) {
                        symbolDao.getAll()
                    }
                val isRefresh =
                    cachedSymbols.isEmpty() || isCacheStale(symbolDao.getLastUpdate())

                if (cachedSymbols.isEmpty()) {
                    refreshJobStart(streamToDb = true)
                    symbolDao
                        .observeAll()
                        .dropWhile { it.isEmpty() }
                        .collect { send(it.toDomainFromEntity()) }
                } else {
                    if (isRefresh) refreshJobStart()
                    symbolDao
                        .observeAll()
                        .collect { send(it.toDomainFromEntity()) }
                }
            }.flowOn(ioDispatcher)

        override suspend fun refreshCatalog(): Result<Unit> =
            withContext(ioDispatcher) {
                runCatching {
                    refreshSymbols()
                }.onFailure { error ->
                    if (error is CancellationException) {
                        throw error
                    }
                }
            }

        // start job to refresh symbols in background
        private fun refreshJobStart(streamToDb: Boolean = false) {
            if (refreshSymbolsJob?.isActive == true) return

            refreshSymbolsJob =
                refreshSymbolsScope.launch {
                    try {
                        refreshSymbols(streamToDb)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (_: Exception) {
                    }
                }
        }

        // update symbols
        private suspend fun refreshSymbols(streamToDb: Boolean = false) {
            var skip = 0
            val syncedAtMillis = System.currentTimeMillis()
            val refreshedSymbols = mutableListOf<SymbolEntity>()
            refreshProviders()

            while (true) {
                val response =
                    cryptoCompareApi.getSymbols(
                        skip = skip,
                        rows = Constants.CryptoCompareRepositoryConstants.SYMBOLS_IN_ROW,
                    )

                if (response.errorCode != 0) {
                    val message = response.errorMsgs?.joinToString("\n") ?: "Unknown error"
                    throw IllegalStateException(message)
                }

                val symbols = response.symbols.orEmpty()
                if (symbols.isEmpty()) break

                val refreshedSymbolsPage = symbols.toEntityFromDto(syncedAtMillis)
                if (streamToDb) {
                    symbolDao.upsertAll(refreshedSymbolsPage)
                }
                refreshedSymbols += refreshedSymbolsPage
                skip += Constants.CryptoCompareRepositoryConstants.SYMBOLS_IN_ROW
            }

            if (!streamToDb) {
                symbolDao.syncSymbols(refreshedSymbols)
            }
            refreshedSymbols.toDomainFromEntity()
        }

        private fun isCacheStale(lastUpdatedMillis: Long): Boolean =
            System.currentTimeMillis() - lastUpdatedMillis >=
                Constants.CryptoCompareRepositoryConstants.CATALOG_CACHE_TTL_MILLIS
    }
