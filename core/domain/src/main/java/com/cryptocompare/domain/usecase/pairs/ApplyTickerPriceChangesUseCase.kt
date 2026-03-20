package com.cryptocompare.domain.usecase.pairs

import com.cryptocompare.domain.mapper.toPairItemByTicker
import com.cryptocompare.model.PairUiItem
import com.cryptocompare.model.Symbol
import com.cryptocompare.model.TickerStreamEvent
import javax.inject.Inject

class ApplyTickerPriceChangesUseCase
    @Inject
    constructor() {
        operator fun invoke(
            event: TickerStreamEvent.TickerPriceChange,
            symbolsById: MutableMap<Long, Symbol>,
            currentPairs: MutableList<PairUiItem>,
        ): MutableList<PairUiItem> {
            val symbolId = event.data.symbolId.toLong()
            val currentSymbol = symbolsById[symbolId] ?: return currentPairs
            val updatedSymbol =
                currentSymbol.copy(
                    priceBuy = event.data.priceBuy,
                    priceSell = event.data.priceSell,
                )

            symbolsById[symbolId] = updatedSymbol

            val normalizedTicker = updatedSymbol.ticker?.uppercase().orEmpty()
            if (normalizedTicker.isEmpty()) return currentPairs

            val updatedItem =
                symbolsById.values.toPairItemByTicker(normalizedTicker)
                    ?: return currentPairs
            val index = currentPairs.indexOfFirst { it.ticker == normalizedTicker }

            if (index == -1) return currentPairs
            currentPairs[index] = updatedItem

            return currentPairs
        }
    }
