package com.cryptocompare.pairs.viewmodel.mainViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cryptocompare.data.mapper.toPairItemByTicker
import com.cryptocompare.data.mapper.toPairItems
import com.cryptocompare.domain.repository.CryptoCompareRepository
import com.cryptocompare.domain.repository.TickerStreamRepository
import com.cryptocompare.model.Symbol
import com.cryptocompare.model.TickerStreamEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

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
        private val subscribedTickers = mutableSetOf<String>()

        init {
            observeSocket()
            loadPairs()
        }

        fun loadPairs() {
            _uiState.update { it.copy(error = null, loading = true) }
            viewModelScope.launch {
                try {
                    tickerStreamRepository.connect()

                    cryptoCompareRepository.getSymbols().collect { page ->
                        page.forEach { symbol ->
                            symbolsById[symbol.id] = symbol
                        }
                        _uiState.update { state ->
                            state.copy(
                                pairs = symbolsById.values.toPairItems(),
                                loading = false,
                                error = null,
                            )
                        }
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    _uiState.update { it.copy(loading = false, error = e.message ?: "Error") }
                }
            }
        }

        fun onSearchQueryChange(query: String) {
            _uiState.update { it.copy(searchQuery = query) }
        }

        fun onVisibleTickersChange(visibleTickers: List<String>) {
            val normalizedVisibleTickers =
                visibleTickers
                    .map { it.lowercase() }
                    .filter { it.isNotBlank() }
                    .toSet()

            val toUnsubscribe = subscribedTickers - normalizedVisibleTickers
            val toSubscribe = normalizedVisibleTickers - subscribedTickers

            toUnsubscribe.forEach { ticker ->
                tickerStreamRepository.unsubscribe(ticker)
                subscribedTickers.remove(ticker)
            }

            toSubscribe.forEach { ticker ->
                tickerStreamRepository.subscribe(ticker)
                subscribedTickers.add(ticker)
            }

            _uiState.update { it.copy(subscribedTickers = subscribedTickers.toSet()) }
        }

        private fun observeSocket() {
            viewModelScope.launch {
                tickerStreamRepository.event.collect { event ->
                    if (event is TickerStreamEvent.TickerPriceChange) {
                        val symbolId = event.data.symbolId.toLong()
                        val currentSymbol = symbolsById[symbolId] ?: return@collect

                        val updatedSymbol =
                            currentSymbol.copy(
                                priceBuy = event.data.priceBuy,
                                priceSell = event.data.priceSell,
                            )

                        symbolsById[symbolId] = updatedSymbol

                        val normalizedTicker = updatedSymbol.ticker?.uppercase().orEmpty()
                        if (normalizedTicker.isEmpty()) return@collect

                        val updatedItem =
                            symbolsById.values.toPairItemByTicker(normalizedTicker)
                                ?: return@collect

                        _uiState.update { state ->
                            val currentPairs = state.pairs.toMutableList()
                            val index = currentPairs.indexOfFirst { it.ticker == normalizedTicker }

                            if (index == -1) {
                                state
                            } else {
                                currentPairs[index] = updatedItem
                                state.copy(pairs = currentPairs)
                            }
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
