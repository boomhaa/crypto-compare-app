package com.cryptocompare.data.mapper

import com.cryptocompare.data.local.entity.SymbolEntity
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

fun SymbolEntity.toDomain(): Symbol =
    Symbol(
        id = id,
        ticker = ticker,
        symbol = symbol,
        providerId = providerId,
        priceSell = priceSell,
        priceBuy = priceBuy,
    )

fun SymbolDto.toEntity(syncedAtMillis: Long): SymbolEntity =
    SymbolEntity(
        id = id,
        ticker = ticker,
        symbol = symbol,
        providerId = providerId,
        priceBuy = priceBuy,
        priceSell = priceSell,
        updatedAt = updatedAt,
        syncedAtMillis = syncedAtMillis,
    )

fun List<SymbolDto>.toEntity(syncedAtMillis: Long): List<SymbolEntity> = map { it.toEntity(syncedAtMillis) }

fun List<SymbolEntity>.toDomain(): List<Symbol> = map(SymbolEntity::toDomain)
