package com.example.providers.viewmodel.providersviewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.repository.CryptoCompareRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProvidersViewModel
    @Inject
    constructor(
        private val cryptoCompareRepository: CryptoCompareRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(ProvidersUiState())
        val uiState = _uiState.asStateFlow()

        init {
            loadProviders()
        }

        fun loadProviders() {
            _uiState.update { _uiState.value.copy(error = null, loading = true) }

            viewModelScope.launch {
                cryptoCompareRepository
                    .getProviders()
                    .onSuccess { providers ->
                        _uiState.update {
                            _uiState.value.copy(
                                providers = providers,
                                loading = false,
                                error = null,
                            )
                        }
                    }.onFailure { exception ->
                        _uiState.update {
                            _uiState.value.copy(
                                loading = false,
                                error = exception.message ?: "Error",
                            )
                        }
                    }
            }
        }
    }
