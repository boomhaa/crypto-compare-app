package com.example.network.dto.apiDTO

data class GetSymbolResponse(
    val errorCode: Int,
    val errorMsgs: List<String>?,
    val id: Long,
    val ticker: String?,
    val symbol: String?,
    val providerId: Int,
    val priceSell: Double,
    val priceBuy: Double,
    val updatedAt: String,
)
