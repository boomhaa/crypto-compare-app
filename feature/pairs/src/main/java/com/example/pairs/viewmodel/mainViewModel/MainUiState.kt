package com.example.pairs.viewmodel.mainViewModel

import com.example.model.PairUiItem

data class MainUiState(
    val loading: Boolean = false,
    val pairs: List<PairUiItem> = emptyList(),
    val searchQuery: String = "",
    val error: String? = null,
)
