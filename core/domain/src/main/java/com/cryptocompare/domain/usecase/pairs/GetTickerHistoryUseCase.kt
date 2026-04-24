package com.cryptocompare.domain.usecase.pairs

import com.cryptocompare.domain.repository.CryptoHistoryRepository
import com.cryptocompare.model.PriceCandle
import javax.inject.Inject

class GetTickerHistoryUseCase
    @Inject
    constructor(
        private val historyRepository: CryptoHistoryRepository,
    ) {
        suspend operator fun invoke(
            fromSymbol: String,
            toSymbol: String,
            exchange: String?,
            limit: Int = 100,
        ): Result<List<PriceCandle>> =
            historyRepository.getTickerHistory(
                fromSymbol = fromSymbol,
                toSymbol = toSymbol,
                exchange = exchange,
                limit = limit,
            )
    }
