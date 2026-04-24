package com.cryptocompare.network.dto.apiDTO.cryptoCompareDTO

data class SymbolDto(
    val id: Long,
    val ticker: String?,
    val symbol: String?,
    val providerSellId: Int,
    val priceSell: Double,
    val providerBuyId: Int,
    val priceBuy: Double,
    val updatedAt: String,
)
