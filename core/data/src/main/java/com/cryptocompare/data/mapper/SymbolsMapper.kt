package com.cryptocompare.data.mapper

import com.cryptocompare.model.Symbol
import com.cryptocompare.network.dto.apiDTO.SymbolDto

fun SymbolDto.toDomain(): Symbol =
    Symbol(
        id = id,
        ticker = ticker,
        symbol = symbol,
        providerId = providerId,
        priceSell = priceSell,
        priceBuy = priceBuy,
    )

fun List<SymbolDto>.toDomain(): List<Symbol> = map(SymbolDto::toDomain)
