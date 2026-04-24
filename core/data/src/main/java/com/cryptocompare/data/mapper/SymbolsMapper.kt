package com.cryptocompare.data.mapper

import com.cryptocompare.data.local.entity.SymbolEntity
import com.cryptocompare.model.Symbol
import com.cryptocompare.network.dto.apiDTO.cryptoCompareDTO.SymbolDto

fun SymbolEntity.toDomainFromEntity(): Symbol =
    Symbol(
        id = id,
        ticker = ticker,
        symbol = symbol,
        providerSellId = providerSellId,
        priceSell = priceSell,
        providerBuyId = providerBuyId,
        priceBuy = priceBuy,
    )

fun SymbolDto.toEntityFromDto(syncedAtMillis: Long): SymbolEntity =
    SymbolEntity(
        id = id,
        ticker = ticker,
        symbol = symbol,
        providerSellId = providerSellId,
        priceSell = priceSell,
        providerBuyId = providerBuyId,
        priceBuy = priceBuy,
        updatedAt = updatedAt,
        syncedAtMillis = syncedAtMillis,
    )

fun List<SymbolDto>.toEntityFromDto(syncedAtMillis: Long): List<SymbolEntity> =
    map {
        it.toEntityFromDto(syncedAtMillis)
    }

fun List<SymbolEntity>.toDomainFromEntity(): List<Symbol> = map(SymbolEntity::toDomainFromEntity)
