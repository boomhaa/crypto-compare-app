package com.example.pairs.viewmodel.mainViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.mapper.toPairItems
import com.example.domain.repository.CryptoCompareRepository
import com.example.domain.repository.TickerStreamRepository
import com.example.model.Symbol
import com.example.model.TickerStreamEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel
    @Inject
    constructor(
        private val cryptoCompareRepository: CryptoCompareRepository,
        private val tickerStreamRepository: TickerStreamRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(MainUiState())
        val uiState = _uiState.asStateFlow()

        private val symbolsById = mutableMapOf<Long, Symbol>()

        init {
            observeSocket()
            loadPairs()
        }

        fun loadPairs() {
            _uiState.update { it.copy(error = null, loading = true) }

            viewModelScope.launch {
                cryptoCompareRepository
                    .getSymbols()
                    .onSuccess { symbols ->
                        symbolsById.clear()
                        symbols.forEach { symbolsById[it.id] = it }

                        _uiState.update {
                            it.copy(
                                pairs = symbolsById.values.toPairItems(),
                                loading = false,
                                error = null,
                            )
                        }

                        tickerStreamRepository.connect()
                        symbolsById.values.mapNotNull { it.ticker?.lowercase() }.toSet().forEach { ticker ->
                            tickerStreamRepository.subscribe(ticker)
                        }
                    }.onFailure { exception ->
                        _uiState.update {
                            it.copy(
                                loading = false,
                                error = exception.message ?: "Error",
                            )
                        }
                    }
            }
        }

        fun onSearchQueryChange(query: String) {
            _uiState.update { it.copy(searchQuery = query) }
        }

        private fun observeSocket() {
            viewModelScope.launch {
                tickerStreamRepository.event.collect { event ->
                    if (event is TickerStreamEvent.TickerPriceChange) {
                        val symbolId = event.data.symbolId.toLong()
                        val byId = symbolsById[symbolId]
                        when {
                            byId != null -> {
                                symbolsById[symbolId] =
                                    byId.copy(
                                        priceSell = event.data.priceSell,
                                        priceBuy = event.data.priceBuy,
                                    )
                            }

                            else -> {
                                val keyByProviderAndTicker =
                                    symbolsById.entries
                                        .firstOrNull { (_, symbol) ->
                                            symbol.providerId == event.data.providerId &&
                                                symbol.ticker.equals(event.data.ticker, ignoreCase = true)
                                        }?.key
                                        ?: return@collect

                                val current = symbolsById[keyByProviderAndTicker] ?: return@collect
                                symbolsById[keyByProviderAndTicker] =
                                    current.copy(
                                        priceSell = event.data.priceSell,
                                        priceBuy = event.data.priceBuy,
                                    )
                            }
                        }

                        _uiState.update { state ->
                            state.copy(pairs = symbolsById.values.toPairItems())
                        }
                    }
                }
            }
        }

        override fun onCleared() {
            tickerStreamRepository.disconnect()
            super.onCleared()
        }
    }
