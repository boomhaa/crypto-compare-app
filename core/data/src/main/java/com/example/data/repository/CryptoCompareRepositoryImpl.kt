package com.example.data.repository

import com.example.data.mapper.toDomain
import com.example.domain.repository.CryptoCompareRepository
import com.example.model.Provider
import com.example.model.Symbol
import com.example.network.api.CryptoCompareApi
import kotlinx.coroutines.CancellationException
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

        override suspend fun getSymbols(): Result<List<Symbol>> {
            return try {
                val providersResponse = cryptoCompareApi.getProviders()

                if (providersResponse.errorCode != 0) {
                    val message = providersResponse.errorMsgs?.joinToString("\n") ?: "Unknown error"
                    return Result.failure(Exception(message))
                }

                val allSymbols =
                    providersResponse.providers.orEmpty().flatMap { provider ->
                        val symbolsResponse = cryptoCompareApi.getSymbolsByProvider(providerId = provider.id)
                        if (symbolsResponse.errorCode != 0) {
                            val message = symbolsResponse.errorMsgs?.joinToString("\n") ?: "Unknown error"
                            throw IllegalStateException(message)
                        }
                        symbolsResponse.symbols.orEmpty().toDomain()
                    }

                Result.success(allSymbols)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
