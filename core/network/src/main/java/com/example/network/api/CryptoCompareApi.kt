package com.example.network.api

import com.example.network.dto.apiDTO.GetProviderResponse
import com.example.network.dto.apiDTO.GetProvidersResponse
import com.example.network.dto.apiDTO.GetSymbolResponse
import com.example.network.dto.apiDTO.GetSymbolsResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CryptoCompareApi {
    @GET("providers")
    suspend fun getProviders(
        @Query("skip") skip: Int? = null,
        @Query("rows") rows: Int? = null,
    ): GetProvidersResponse

    @GET("providers/{id}")
    suspend fun getProvider(
        @Path("id") id: Int,
    ): GetProviderResponse

    @GET("symbols/provider/{providerId}")
    suspend fun getSymbolsByProvider(
        @Path("providerId") providerId: Int,
        @Query("skip") skip: Int? = null,
        @Query("rows") rows: Int? = null,
    ): GetSymbolsResponse

    @GET("symbols/{id}")
    suspend fun getSymbol(
        @Path("id") id: Long,
    ): GetSymbolResponse

    @GET("symbols/ticker/{ticker}")
    suspend fun getSymbolsByTicker(
        @Path("ticker") ticker: String,
    ): GetSymbolsResponse
}
