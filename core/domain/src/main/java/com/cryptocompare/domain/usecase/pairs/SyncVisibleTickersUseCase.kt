package com.cryptocompare.domain.usecase.pairs

import com.cryptocompare.domain.repository.TickerStreamRepository
import javax.inject.Inject

class SyncVisibleTickersUseCase
    @Inject
    constructor(
        private val tickerStreamRepository: TickerStreamRepository,
    ) {
        operator fun invoke(
            visibleTickers: List<String>,
            subscribedTickers: MutableSet<String>,
        ): Set<String> {
            val normalizedVisibleTickers =
                visibleTickers
                    .map { it.lowercase() }
                    .filter { it.isNotBlank() }
                    .toSet()

            val toUnsubscribe = subscribedTickers - normalizedVisibleTickers
            val toSubscribe = normalizedVisibleTickers - subscribedTickers

            toUnsubscribe.forEach(tickerStreamRepository::unsubscribe)
            toSubscribe.forEach(tickerStreamRepository::subscribe)

            return normalizedVisibleTickers
        }
    }
