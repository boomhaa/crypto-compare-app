package com.example.data.repository

import com.example.data.mapper.toDomain
import com.example.domain.repository.CryptoCompareRepository
import com.example.model.Provider
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
    }
