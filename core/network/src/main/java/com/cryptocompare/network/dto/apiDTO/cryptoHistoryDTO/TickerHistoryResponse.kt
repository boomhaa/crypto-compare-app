package com.cryptocompare.network.dto.apiDTO.cryptoHistoryDTO

import com.google.gson.annotations.SerializedName

data class TickerHistoryResponse(
    @SerializedName("Response") val response: String,
    @SerializedName("Message") val message: String,
    @SerializedName("HasWarning") val hasWarning: Boolean,
    @SerializedName("Data") val data: TickerCandlesData,
)
