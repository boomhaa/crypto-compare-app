package com.example.data.mapper

import com.example.model.PairUiItem
import com.example.model.Symbol
import com.example.network.dto.apiDTO.SymbolDto

fun SymbolDto.toDomain(): Symbol =
    Symbol(
        id = id,
        ticker = ticker,
        symbol = symbol,
        providerId = providerId,
        priceSell = priceSell,
        priceBuy = priceBuy,
    )

fun List<SymbolDto>.toDomain(): List<Symbol> = map { it.toDomain() }

fun MutableCollection<Symbol>.toPairItems(): List<PairUiItem> =
    groupBy { it.ticker?.uppercase().orEmpty() }
        .map { (ticker, symbols) ->
            val prices = symbols.flatMap { listOf(it.priceBuy, it.priceSell) }
            PairUiItem(
                ticker = ticker,
                minPrice = prices.minOrNull() ?: 0.0,
                maxPrice = prices.maxOrNull() ?: 0.0,
            )
        }.sortedBy { it.ticker }
