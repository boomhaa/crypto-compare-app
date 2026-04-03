package com.cryptocompare.model

data class Symbol(
    val id: Long,
    val ticker: String?,
    val symbol: String?,
    val providerSellId: Int,
    val priceSell: Double,
    val providerBuyId: Int,
    val priceBuy: Double,
)
