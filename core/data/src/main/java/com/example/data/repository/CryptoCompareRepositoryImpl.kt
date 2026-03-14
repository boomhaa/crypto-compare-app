package com.example.data.repository

import com.example.data.mapper.toDomain
import com.example.domain.repository.CryptoCompareRepository
import com.example.helpers.util.Constants
import com.example.model.Provider
import com.example.model.Symbol
import com.example.network.api.CryptoCompareApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.orEmpty

@Singleton
class CryptoCompareRepositoryImpl
    @Inject
    constructor(
        val cryptoCompareApi: CryptoCompareApi,
    ) : CryptoCompareRepository {
        override suspend fun getProviders(): Result<List<Provider>> =
            try {
                val response = cryptoCompareApi.getProviders()
                if (response.errorCode != 0) {
                    val message = response.errorMsgs?.joinToString("\n") ?: "Unknown error"
                    Result.failure(Exception(message))
                } else {
                    Result.success(response.providers.orEmpty().toDomain())
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(e)
            }

        override suspend fun getSymbols(): Flow<List<Symbol>> =
            flow {
                var skip = 0

                while (true) {
                    val response = cryptoCompareApi.getSymbols(skip = skip, rows = Constants.SYMBOLS_IN_ROW)

                    if (response.errorCode != 0) {
                        val message = response.errorMsgs?.joinToString("\n") ?: "Unknown error"
                        throw IllegalStateException(message)
                    }

                    val symbols = response.symbols.orEmpty()
                    if (symbols.isEmpty()) break

                    emit(symbols.toDomain())
                    skip += Constants.SYMBOLS_IN_ROW
                }
            }
    }
