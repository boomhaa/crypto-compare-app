package com.cryptocompare.network.api

import com.cryptocompare.network.dto.apiDTO.cryptoHistoryDTO.TickerHistoryResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface CryptoHistoryApi {
    @GET("data/v2/histoday")
    suspend fun getTickerHistory(
        @Query("fsym") fromSymbols: String,
        @Query("tsym") toSymbols: String,
        @Query("limit") limit: Int = 50,
        @Query("e") exchange: String? = null,
    ): TickerHistoryResponse
}
