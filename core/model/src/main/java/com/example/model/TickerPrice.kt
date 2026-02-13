package com.example.model

data class TickerPrice(
    val ticker: String,
    val symbolId: Int,
    val providerId: Int,
    val priceSell: Double,
    val priceBuy: Double,
)
