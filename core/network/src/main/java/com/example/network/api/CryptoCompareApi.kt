package com.example.network.api

import com.example.network.dto.GetProvidesResponse
import retrofit2.http.GET

interface CryptoCompareApi {
    @GET("providers")
    suspend fun getProviders(): GetProvidesResponse
}
