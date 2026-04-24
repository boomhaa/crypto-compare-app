package com.cryptocompare.network.dto.apiDTO.cryptoHistoryDTO

import com.google.gson.annotations.SerializedName

data class TickerCandlesData(
    @SerializedName("TimeFrom") val timeFrom: Long,
    @SerializedName("TimeTo") val timeTo: Long,
    @SerializedName("Data") val data: List<TickerCandle>,
)
