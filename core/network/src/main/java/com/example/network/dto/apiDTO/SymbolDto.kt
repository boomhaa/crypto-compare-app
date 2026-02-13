package com.example.network.dto.apiDTO

data class SymbolDto(
    val id: Long,
    val ticker: String?,
    val symbol: String?,
    val providerId: Int,
    val priceSell: Double,
    val priceBuy: Double,
    val updatedAt: String,
)
