package com.cryptocompare.domain.repository

import com.cryptocompare.model.PriceCandle

interface CryptoHistoryRepository {
    suspend fun getTickerHistory(
        fromSymbol: String,
        toSymbol: String,
        exchange: String?,
        limit: Int,
    ): Result<List<PriceCandle>>
}
