package com.cryptocompare.pairs

import com.cryptocompare.domain.usecase.pairs.ApplyTickerPriceChangesUseCase
import com.cryptocompare.domain.usecase.pairs.LoadPairsUseCase
import com.cryptocompare.domain.usecase.pairs.ObserveTickerEventUseCase
import com.cryptocompare.domain.usecase.pairs.StreamDisconnectUseCase
import com.cryptocompare.domain.usecase.pairs.SyncVisibleTickersUseCase
import com.cryptocompare.model.PairUiItem
import com.cryptocompare.model.Symbol
import com.cryptocompare.model.TickerPrice
import com.cryptocompare.model.TickerStreamEvent
import com.cryptocompare.pairs.viewmodel.mainViewModel.MainViewModel
import com.cryptocompare.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun loadPairsUseCaseMock(
        block: suspend (MutableMap<Long, Symbol>) -> Flow<List<PairUiItem>>,
    ): LoadPairsUseCase =
        mockk {
            coEvery { this@mockk.invoke(any()) } coAnswers {
                block(firstArg())
            }
        }

    private fun observeTickerEventUseCaseMock(flow: Flow<TickerStreamEvent>): ObserveTickerEventUseCase =
        mockk {
            every { this@mockk.invoke() } returns flow
        }

    private fun syncVisibleTickersUseCaseMock(
        block: (List<String>, Set<String>) -> Set<String>,
    ): SyncVisibleTickersUseCase =
        mockk {
            every { this@mockk.invoke(any(), any()) } answers {
                block(firstArg(), secondArg())
            }
        }

    private fun streamDisconnectUseCaseMock(): StreamDisconnectUseCase =
        mockk {
            every { this@mockk.invoke() } just runs
        }

    private fun applyTickerPriceChangesUseCaseMock(
        block: (
            TickerStreamEvent.TickerPriceChange,
            MutableMap<Long, Symbol>,
            MutableList<PairUiItem>,
        ) -> List<PairUiItem>,
    ): ApplyTickerPriceChangesUseCase =
        mockk {
            every { this@mockk.invoke(any(), any(), any()) } answers {
                block(
                    firstArg(),
                    secondArg(),
                    thirdArg(),
                ) as MutableList<PairUiItem>
            }
        }

    @Test
    fun `init triggers loadPairs and sets loading true immediately`() =
        runTest {
            val gate = MutableSharedFlow<List<PairUiItem>>(extraBufferCapacity = 1)

            val loadPairsUseCase = loadPairsUseCaseMock { gate }
            val observeTickerEventUseCase = observeTickerEventUseCaseMock(emptyFlow())
            val syncVisibleTickersUseCase =
                syncVisibleTickersUseCaseMock { visible, _ ->
                    visible.map { it.trim().lowercase() }.filter { it.isNotBlank() }.toSet()
                }
            val streamDisconnectUseCase = streamDisconnectUseCaseMock()
            val applyTickerPriceChangesUseCase =
                applyTickerPriceChangesUseCaseMock { _, _, pairs ->
                    pairs
                }

            val vm =
                MainViewModel(
                    loadPairsUseCase = loadPairsUseCase,
                    syncVisibleTickersUseCase = syncVisibleTickersUseCase,
                    streamDisconnectUseCase = streamDisconnectUseCase,
                    observeTickerEventUseCase = observeTickerEventUseCase,
                    applyTickerPriceChangesUseCase = applyTickerPriceChangesUseCase,
                )

            assertEquals(true, vm.uiState.value.loading)
            assertNull(vm.uiState.value.error)
        }

    @Test
    fun `loadPairs success updates ui state`() =
        runTest {
            val pairs =
                listOf(
                    PairUiItem(
                        ticker = "BTCUSDT",
                        symbolIds = listOf(1, 3),
                        providerIds = listOf(1, 2),
                        minPrice = 98.5,
                        maxPrice = 102.0,
                    ),
                    PairUiItem(
                        ticker = "ETHUSDT",
                        symbolIds = listOf(2),
                        providerIds = listOf(1),
                        minPrice = 10.0,
                        maxPrice = 11.0,
                    ),
                )

            val loadPairsUseCase =
                loadPairsUseCaseMock { symbolsById ->
                    symbolsById[1L] = Symbol(1L, "btcusdt", "BTC/USDT", 1, 101.0, 99.0)
                    symbolsById[2L] = Symbol(2L, "ethusdt", "ETH/USDT", 1, 11.0, 10.0)
                    symbolsById[3L] = Symbol(3L, "btcusdt", "BTC/USDT", 2, 102.0, 98.5)
                    flowOf(pairs)
                }

            val observeTickerEventUseCase = observeTickerEventUseCaseMock(emptyFlow())
            val syncVisibleTickersUseCase =
                syncVisibleTickersUseCaseMock { visible, _ ->
                    visible.map { it.trim().lowercase() }.filter { it.isNotBlank() }.toSet()
                }
            val streamDisconnectUseCase = streamDisconnectUseCaseMock()
            val applyTickerPriceChangesUseCase =
                applyTickerPriceChangesUseCaseMock { _, _, currentPairs ->
                    currentPairs
                }

            val vm =
                MainViewModel(
                    loadPairsUseCase = loadPairsUseCase,
                    syncVisibleTickersUseCase = syncVisibleTickersUseCase,
                    streamDisconnectUseCase = streamDisconnectUseCase,
                    observeTickerEventUseCase = observeTickerEventUseCase,
                    applyTickerPriceChangesUseCase = applyTickerPriceChangesUseCase,
                )

            yield()

            assertEquals(false, vm.uiState.value.loading)
            assertNull(vm.uiState.value.error)
            assertEquals(2, vm.uiState.value.pairs.size)

            val btc =
                vm.uiState.value.pairs
                    .first { it.ticker == "BTCUSDT" }
            assertEquals(98.5, btc.minPrice, 0.0)
            assertEquals(102.0, btc.maxPrice, 0.0)
            assertEquals(listOf(1L, 3L), btc.symbolIds)
        }

    @Test
    fun `ticker event updates prices through apply use case`() =
        runTest {
            val events = MutableSharedFlow<TickerStreamEvent>(extraBufferCapacity = 8)

            val initialPairs =
                listOf(
                    PairUiItem(
                        ticker = "BTCUSDT",
                        symbolIds = listOf(1L, 2L),
                        providerIds = listOf(1, 2),
                        minPrice = 98.0,
                        maxPrice = 103.0,
                    ),
                )

            val updatedPairs =
                listOf(
                    PairUiItem(
                        ticker = "BTCUSDT",
                        symbolIds = listOf(1L, 2L),
                        providerIds = listOf(1, 2),
                        minPrice = 98.0,
                        maxPrice = 150.0,
                    ),
                )

            val loadPairsUseCase =
                loadPairsUseCaseMock { symbolsById ->
                    symbolsById[1L] = Symbol(1L, "btcusdt", "BTC/USDT", 1, 101.0, 99.0)
                    symbolsById[2L] = Symbol(2L, "btcusdt", "BTC/USDT", 2, 103.0, 98.0)
                    flowOf(initialPairs)
                }

            val observeTickerEventUseCase = observeTickerEventUseCaseMock(events)
            val syncVisibleTickersUseCase =
                syncVisibleTickersUseCaseMock { visible, _ ->
                    visible.map { it.trim().lowercase() }.filter { it.isNotBlank() }.toSet()
                }
            val streamDisconnectUseCase = streamDisconnectUseCaseMock()
            val applyTickerPriceChangesUseCase =
                applyTickerPriceChangesUseCaseMock { event, symbolsById, currentPairs ->
                    assertEquals(1L, event.data.symbolId.toLong())
                    assertTrue(symbolsById.containsKey(1L))
                    assertEquals(1, currentPairs.size)
                    updatedPairs
                }

            val vm =
                MainViewModel(
                    loadPairsUseCase = loadPairsUseCase,
                    syncVisibleTickersUseCase = syncVisibleTickersUseCase,
                    streamDisconnectUseCase = streamDisconnectUseCase,
                    observeTickerEventUseCase = observeTickerEventUseCase,
                    applyTickerPriceChangesUseCase = applyTickerPriceChangesUseCase,
                )

            yield()

            events.tryEmit(
                TickerStreamEvent.TickerPriceChange(
                    id = "evt1",
                    data =
                        TickerPrice(
                            ticker = "btcusdt",
                            symbolId = 1,
                            providerId = 1,
                            priceSell = 150.0,
                            priceBuy = 140.0,
                        ),
                ),
            )

            yield()

            val btc =
                vm.uiState.value.pairs
                    .first { it.ticker == "BTCUSDT" }
            assertEquals(98.0, btc.minPrice, 0.0)
            assertEquals(150.0, btc.maxPrice, 0.0)
        }

    @Test
    fun `loadPairs failure sets error and clears loading`() =
        runTest {
            val loadPairsUseCase =
                loadPairsUseCaseMock {
                    flow {
                        throw IllegalStateException("boom")
                    }
                }

            val observeTickerEventUseCase = observeTickerEventUseCaseMock(emptyFlow())
            val syncVisibleTickersUseCase =
                syncVisibleTickersUseCaseMock { visible, _ ->
                    visible.map { it.trim().lowercase() }.filter { it.isNotBlank() }.toSet()
                }
            val streamDisconnectUseCase = streamDisconnectUseCaseMock()
            val applyTickerPriceChangesUseCase =
                applyTickerPriceChangesUseCaseMock { _, _, currentPairs ->
                    currentPairs
                }

            val vm =
                MainViewModel(
                    loadPairsUseCase = loadPairsUseCase,
                    syncVisibleTickersUseCase = syncVisibleTickersUseCase,
                    streamDisconnectUseCase = streamDisconnectUseCase,
                    observeTickerEventUseCase = observeTickerEventUseCase,
                    applyTickerPriceChangesUseCase = applyTickerPriceChangesUseCase,
                )

            yield()

            assertEquals(false, vm.uiState.value.loading)
            assertEquals("boom", vm.uiState.value.error)
            assertTrue(
                vm.uiState.value.pairs
                    .isEmpty(),
            )
        }

    @Test
    fun `onVisibleTickersChange updates subscribed tickers from use case result`() =
        runTest {
            val loadPairsUseCase = loadPairsUseCaseMock { emptyFlow() }
            val observeTickerEventUseCase = observeTickerEventUseCaseMock(emptyFlow())

            val syncVisibleTickersUseCase =
                syncVisibleTickersUseCaseMock { visible, _ ->
                    visible.map { it.trim().lowercase() }.filter { it.isNotBlank() }.toSet()
                }

            val streamDisconnectUseCase = streamDisconnectUseCaseMock()
            val applyTickerPriceChangesUseCase =
                applyTickerPriceChangesUseCaseMock { _, _, currentPairs ->
                    currentPairs
                }

            val vm =
                MainViewModel(
                    loadPairsUseCase = loadPairsUseCase,
                    syncVisibleTickersUseCase = syncVisibleTickersUseCase,
                    streamDisconnectUseCase = streamDisconnectUseCase,
                    observeTickerEventUseCase = observeTickerEventUseCase,
                    applyTickerPriceChangesUseCase = applyTickerPriceChangesUseCase,
                )

            yield()

            vm.onVisibleTickersChange(listOf("BTCUSDT", "ethusdt", ""))

            assertEquals(setOf("btcusdt", "ethusdt"), vm.uiState.value.subscribedTickers)
        }

    @Test
    fun `onVisibleTickersChange uses latest use case result`() =
        runTest {
            val loadPairsUseCase = loadPairsUseCaseMock { emptyFlow() }
            val observeTickerEventUseCase = observeTickerEventUseCaseMock(emptyFlow())

            val syncVisibleTickersUseCase =
                syncVisibleTickersUseCaseMock { visible, _ ->
                    visible.map { it.trim().lowercase() }.filter { it.isNotBlank() }.toSet()
                }

            val streamDisconnectUseCase = streamDisconnectUseCaseMock()
            val applyTickerPriceChangesUseCase =
                applyTickerPriceChangesUseCaseMock { _, _, currentPairs ->
                    currentPairs
                }

            val vm =
                MainViewModel(
                    loadPairsUseCase = loadPairsUseCase,
                    syncVisibleTickersUseCase = syncVisibleTickersUseCase,
                    streamDisconnectUseCase = streamDisconnectUseCase,
                    observeTickerEventUseCase = observeTickerEventUseCase,
                    applyTickerPriceChangesUseCase = applyTickerPriceChangesUseCase,
                )

            yield()

            vm.onVisibleTickersChange(listOf("BTCUSDT", "ethusdt"))
            vm.onVisibleTickersChange(listOf("ETHUSDT"))

            assertEquals(setOf("ethusdt"), vm.uiState.value.subscribedTickers)
        }
}
