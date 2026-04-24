package com.cryptocompare.model

data class TickerDetail(
    val ticker: String,
    val exchanges: List<ProviderDetail>,
)
