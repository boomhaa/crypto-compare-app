package com.cryptocompare.domain.mapper

import com.cryptocompare.model.PairUiItem
import com.cryptocompare.model.Symbol
import kotlin.collections.component1
import kotlin.collections.component2

fun Collection<Symbol>.toPairItems(): List<PairUiItem> =
    groupBy { it.ticker?.uppercase().orEmpty() }
        .map { (ticker, symbols) ->
            val prices = symbols.flatMap { listOf(it.priceBuy, it.priceSell) }
            val providerIds =
                symbols
                    .flatMap { listOf(it.providerSellId, it.providerBuyId) }
                    .filter { it > 0 }
                    .distinct()

            PairUiItem(
                ticker = ticker,
                symbolIds = symbols.map { it.id },
                providerIds = providerIds,
                minPrice = prices.minOrNull() ?: 0.0,
                maxPrice = prices.maxOrNull() ?: 0.0,
            )
        }.sortedBy { it.ticker }

fun Collection<Symbol>.toPairItemByTicker(ticker: String): PairUiItem? {
    val symbols = filter { it.ticker?.uppercase().orEmpty() == ticker.uppercase() }
    if (symbols.isEmpty()) return null

    val prices = symbols.flatMap { listOf(it.priceBuy, it.priceSell) }
    val providerIds =
        symbols
            .flatMap { listOf(it.providerSellId, it.providerBuyId) }
            .filter { it > 0 }
            .distinct()

    return PairUiItem(
        ticker = ticker.uppercase(),
        symbolIds = symbols.map { it.id },
        providerIds = providerIds,
        minPrice = prices.minOrNull() ?: 0.0,
        maxPrice = prices.maxOrNull() ?: 0.0,
    )
}
