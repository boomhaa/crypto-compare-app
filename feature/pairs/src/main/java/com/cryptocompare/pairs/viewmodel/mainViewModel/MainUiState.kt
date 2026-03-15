package com.cryptocompare.pairs.viewmodel.mainViewModel

import com.cryptocompare.model.PairUiItem

data class MainUiState(
    val loading: Boolean = false,
    val pairs: List<PairUiItem> = emptyList(),
    val searchQuery: String = "",
    val error: String? = null,
    val subscribedTickers: Set<String> = emptySet(),
)
