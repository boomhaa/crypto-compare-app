package com.cryptocompare.network.dto.apiDTO.cryptoHistoryDTO

import com.google.gson.annotations.SerializedName

data class TickerCandle(
    val time: Long,
    val high: Double,
    val low: Double,
    val open: Double,
    @SerializedName("volumefrom") val volumeFrom: Double,
    @SerializedName("volumeto") val volumeTo: Double,
    val close: Double,
)
