package com.cryptocompare.pairs.viewmodel.mainViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cryptocompare.domain.usecase.pairs.ApplyTickerPriceChangesUseCase
import com.cryptocompare.domain.usecase.pairs.LoadPairsUseCase
import com.cryptocompare.domain.usecase.pairs.ObserveTickerEventUseCase
import com.cryptocompare.domain.usecase.pairs.StreamDisconnectUseCase
import com.cryptocompare.domain.usecase.pairs.SyncVisibleTickersUseCase
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
        private val loadPairsUseCase: LoadPairsUseCase,
        private val syncVisibleTickersUseCase: SyncVisibleTickersUseCase,
        private val streamDisconnectUseCase: StreamDisconnectUseCase,
        private val observeTickerEventUseCase: ObserveTickerEventUseCase,
        private val applyTickerPriceChangesUseCase: ApplyTickerPriceChangesUseCase,
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
                    loadPairsUseCase(symbolsById).collect { symbols ->
                        _uiState.update { state ->
                            state.copy(
                                pairs = symbols,
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
            val updatedSubscribedTickers = syncVisibleTickersUseCase(visibleTickers, subscribedTickers)

            subscribedTickers.clear()
            subscribedTickers.addAll(updatedSubscribedTickers)

            _uiState.update { it.copy(subscribedTickers = updatedSubscribedTickers) }
        }

        private fun observeSocket() {
            viewModelScope.launch {
                try {
                    observeTickerEventUseCase().collect { event ->
                        if (event is TickerStreamEvent.TickerPriceChange) {
                            _uiState.update { state ->
                                val updatedPairs =
                                    applyTickerPriceChangesUseCase(
                                        event = event,
                                        symbolsById = symbolsById,
                                        currentPairs = state.pairs.toMutableList(),
                                    )
                                state.copy(pairs = updatedPairs)
                            }
                        }
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    _uiState.update { it.copy(error = e.message ?: "Socket error") }
                }
            }
        }

        override fun onCleared() {
            streamDisconnectUseCase()
            super.onCleared()
        }
    }
