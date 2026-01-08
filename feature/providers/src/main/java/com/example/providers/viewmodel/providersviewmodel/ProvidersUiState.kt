package com.example.providers.viewmodel.providersviewmodel

import com.example.model.Provider

data class ProvidersUiState(
    val loading: Boolean = false,
    val providers: List<Provider> = emptyList(),
    val error: String? = null,
)
