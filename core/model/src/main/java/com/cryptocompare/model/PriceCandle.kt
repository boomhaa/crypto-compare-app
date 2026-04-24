package com.cryptocompare.model

data class PriceCandle(
    val time: Long,
    val high: Double,
    val low: Double,
    val open: Double,
    val volumeFrom: Double,
    val volumeTo: Double,
    val close: Double,
)
