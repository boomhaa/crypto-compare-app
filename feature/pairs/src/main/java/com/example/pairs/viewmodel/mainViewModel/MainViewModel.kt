package com.example.pairs.viewmodel.mainViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.mapper.toPairItemByTicker
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

                        val sameTickerSymbols =
                            symbolsById.values.filter {
                                it.ticker?.uppercase().orEmpty() == normalizedTicker
                            }

                        Log.d(
                            "SocketDebug",
                            "sameTickerSymbols for $normalizedTicker = ${
                                sameTickerSymbols.joinToString {
                                    "[id=${it.id}, provider=${it.providerId}, buy=${it.priceBuy}, sell=${it.priceSell}]"
                                }
                            }",
                        )

                        val updatedItem =
                            symbolsById.values.toPairItemByTicker(normalizedTicker)
                                ?: return@collect

                        Log.d("SocketDebug", "updatedItem=$updatedItem")

                        _uiState.update { state ->
                            val currentPairs = state.pairs.toMutableList()
                            val index = currentPairs.indexOfFirst { it.ticker == normalizedTicker }

                            Log.d(
                                "SocketDebug",
                                "pair index for $normalizedTicker = $index, currentUiSize=${currentPairs.size}",
                            )

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
