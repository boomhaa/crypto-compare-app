package com.cryptocompare.pairs

import com.cryptocompare.domain.usecase.pairs.ApplyTickerPriceChangesUseCase
import com.cryptocompare.domain.usecase.pairs.LoadPairsUseCase
import com.cryptocompare.domain.usecase.pairs.ObserveFavouriteTickersUseCase
import com.cryptocompare.domain.usecase.pairs.ObserveTickerEventUseCase
import com.cryptocompare.domain.usecase.pairs.StreamDisconnectUseCase
import com.cryptocompare.domain.usecase.pairs.SyncFavouriteTickersUseCase
import com.cryptocompare.domain.usecase.pairs.SyncVisibleTickersUseCase
import com.cryptocompare.domain.usecase.pairs.ToggleFavouriteTickerUseCase
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
            coEvery { this@mockk.invoke(any()) } coAnswers { block(firstArg()) }
        }

    private fun observeTickerEventUseCaseMock(flow: Flow<TickerStreamEvent>): ObserveTickerEventUseCase =
        mockk { every { this@mockk.invoke() } returns flow }

    private fun syncVisibleTickersUseCaseMock(
        block: (List<String>, Set<String>) -> Set<String>,
    ): SyncVisibleTickersUseCase =
        mockk {
            every { this@mockk.invoke(any(), any()) } answers { block(firstArg(), secondArg()) }
        }

    private fun streamDisconnectUseCaseMock(): StreamDisconnectUseCase =
        mockk { every { this@mockk.invoke() } just runs }

    private fun applyTickerPriceChangesUseCaseMock(
        block: (
            TickerStreamEvent.TickerPriceChange,
            MutableMap<Long, Symbol>,
            MutableList<PairUiItem>,
        ) -> List<PairUiItem>,
    ): ApplyTickerPriceChangesUseCase =
        mockk {
            every { this@mockk.invoke(any(), any(), any()) } answers {
                block(firstArg(), secondArg(), thirdArg()) as MutableList<PairUiItem>
            }
        }

    private fun observeFavoriteTickersUseCaseMock(flow: Flow<Set<String>>): ObserveFavouriteTickersUseCase =
        mockk { every { this@mockk.invoke() } returns flow }

    private fun toggleFavoriteTickerUseCaseMock(
        result: Result<Boolean> = Result.success(true),
    ): ToggleFavouriteTickerUseCase = mockk { coEvery { this@mockk.invoke(any()) } returns result }

    private fun syncFavoriteTickersUseCaseMock(
        result: Result<Unit> = Result.success(Unit),
    ): SyncFavouriteTickersUseCase = mockk { coEvery { this@mockk.invoke() } returns result }

    private fun makeVm(
        loadPairsUseCase: LoadPairsUseCase = loadPairsUseCaseMock { emptyFlow() },
        observeTickerEventUseCase: ObserveTickerEventUseCase = observeTickerEventUseCaseMock(emptyFlow()),
        syncVisibleTickersUseCase: SyncVisibleTickersUseCase =
            syncVisibleTickersUseCaseMock { v, _ ->
                v.map { it.trim().lowercase() }.filter { it.isNotBlank() }.toSet()
            },
        streamDisconnectUseCase: StreamDisconnectUseCase = streamDisconnectUseCaseMock(),
        applyTickerPriceChangesUseCase: ApplyTickerPriceChangesUseCase =
            applyTickerPriceChangesUseCaseMock { _, _, p -> p },
        observeFavouriteTickersUseCase: ObserveFavouriteTickersUseCase =
            observeFavoriteTickersUseCaseMock(flowOf(emptySet())),
        toggleFavouriteTickerUseCase: ToggleFavouriteTickerUseCase = toggleFavoriteTickerUseCaseMock(),
        syncFavouriteTickersUseCase: SyncFavouriteTickersUseCase = syncFavoriteTickersUseCaseMock(),
    ): MainViewModel =
        MainViewModel(
            loadPairsUseCase = loadPairsUseCase,
            syncVisibleTickersUseCase = syncVisibleTickersUseCase,
            streamDisconnectUseCase = streamDisconnectUseCase,
            observeTickerEventUseCase = observeTickerEventUseCase,
            applyTickerPriceChangesUseCase = applyTickerPriceChangesUseCase,
            observeFavouriteTickersUseCase = observeFavouriteTickersUseCase,
            toggleFavouriteTickerUseCase = toggleFavouriteTickerUseCase,
            syncFavouriteTickersUseCase = syncFavouriteTickersUseCase,
        )

    @Test
    fun `init triggers loadPairs and sets loading true immediately`() =
        runTest {
            val gate = MutableSharedFlow<List<PairUiItem>>(extraBufferCapacity = 1)
            val vm = makeVm(loadPairsUseCase = loadPairsUseCaseMock { gate })

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

            val vm =
                makeVm(
                    loadPairsUseCase =
                        loadPairsUseCaseMock { symbolsById ->
                            symbolsById[1L] = Symbol(1L, "btcusdt", "BTC/USDT", 1, 101.0, 1, 99.0)
                            symbolsById[2L] = Symbol(2L, "ethusdt", "ETH/USDT", 1, 11.0, 1, 10.0)
                            symbolsById[3L] = Symbol(3L, "btcusdt", "BTC/USDT", 2, 102.0, 2, 98.5)
                            flowOf(pairs)
                        },
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

            val vm =
                makeVm(
                    loadPairsUseCase =
                        loadPairsUseCaseMock { symbolsById ->
                            symbolsById[1L] = Symbol(1L, "btcusdt", "BTC/USDT", 1, 101.0, 1, 99.0)
                            symbolsById[2L] = Symbol(2L, "btcusdt", "BTC/USDT", 2, 103.0, 2, 98.0)
                            flowOf(initialPairs)
                        },
                    observeTickerEventUseCase = observeTickerEventUseCaseMock(events),
                    applyTickerPriceChangesUseCase =
                        applyTickerPriceChangesUseCaseMock { event, symbolsById, currentPairs ->
                            assertEquals(1L, event.data.symbolId.toLong())
                            assertTrue(symbolsById.containsKey(1L))
                            assertEquals(1, currentPairs.size)
                            updatedPairs
                        },
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
            assertEquals(150.0, btc.maxPrice, 0.0)
        }

    @Test
    fun `loadPairs failure sets error and clears loading`() =
        runTest {
            val vm =
                makeVm(
                    loadPairsUseCase = loadPairsUseCaseMock { flow { throw IllegalStateException("boom") } },
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
    fun `socket flow failure sets error and does not crash`() =
        runTest {
            val vm =
                makeVm(
                    observeTickerEventUseCase =
                        observeTickerEventUseCaseMock(flow { throw IllegalStateException("socket disconnected") }),
                )

            yield()

            assertEquals("socket disconnected", vm.uiState.value.error)
        }

    @Test
    fun `onVisibleTickersChange updates subscribed tickers from use case result`() =
        runTest {
            val vm = makeVm()

            yield()
            vm.onVisibleTickersChange(listOf("BTCUSDT", "ethusdt", ""))

            assertEquals(setOf("btcusdt", "ethusdt"), vm.uiState.value.subscribedTickers)
        }

    @Test
    fun `onVisibleTickersChange uses latest use case result`() =
        runTest {
            val vm = makeVm()

            yield()
            vm.onVisibleTickersChange(listOf("BTCUSDT", "ethusdt"))
            vm.onVisibleTickersChange(listOf("ETHUSDT"))

            assertEquals(setOf("ethusdt"), vm.uiState.value.subscribedTickers)
        }

    // -------------------------------------------------------------------------
    // Favourite tickers — new tests
    // -------------------------------------------------------------------------

    @Test
    fun `init observes favourites and updates favouriteTickers in uiState`() =
        runTest {
            val vm =
                makeVm(
                    observeFavouriteTickersUseCase =
                        observeFavoriteTickersUseCaseMock(flowOf(setOf("BTCUSDT", "ETHUSDT"))),
                )

            yield()

            assertEquals(setOf("BTCUSDT", "ETHUSDT"), vm.uiState.value.favouriteTickers)
        }

    @Test
    fun `favouriteTickers updates when flow emits new set`() =
        runTest {
            val favouritesFlow = MutableSharedFlow<Set<String>>(extraBufferCapacity = 1)
            val vm =
                makeVm(
                    observeFavouriteTickersUseCase = observeFavoriteTickersUseCaseMock(favouritesFlow),
                )

            yield()
            assertEquals(emptySet<String>(), vm.uiState.value.favouriteTickers)

            favouritesFlow.emit(setOf("BTCUSDT"))
            yield()
            assertEquals(setOf("BTCUSDT"), vm.uiState.value.favouriteTickers)

            favouritesFlow.emit(setOf("BTCUSDT", "ETHUSDT"))
            yield()
            assertEquals(setOf("BTCUSDT", "ETHUSDT"), vm.uiState.value.favouriteTickers)
        }

    @Test
    fun `onFavouriteClick success does not set error`() =
        runTest {
            val vm =
                makeVm(
                    toggleFavouriteTickerUseCase = toggleFavoriteTickerUseCaseMock(Result.success(true)),
                )

            yield()
            vm.onFavouriteClick("BTCUSDT")
            yield()

            assertNull(vm.uiState.value.error)
        }

    @Test
    fun `onFavouriteClick failure sets error message`() =
        runTest {
            val vm =
                makeVm(
                    toggleFavouriteTickerUseCase =
                        toggleFavoriteTickerUseCaseMock(Result.failure(IllegalStateException("toggle failed"))),
                )

            yield()
            vm.onFavouriteClick("BTCUSDT")
            yield()

            assertEquals("toggle failed", vm.uiState.value.error)
        }

    @Test
    fun `onFavouriteClick failure with null message falls back to default error`() =
        runTest {
            val vm =
                makeVm(
                    toggleFavouriteTickerUseCase =
                        toggleFavoriteTickerUseCaseMock(Result.failure(IllegalStateException(null as String?))),
                )

            yield()
            vm.onFavouriteClick("BTCUSDT")
            yield()

            assertEquals("Favourite toggle error", vm.uiState.value.error)
        }

    @Test
    fun `onOnlyFavouriteChange true sets onlyFavourite flag`() =
        runTest {
            val vm = makeVm()

            vm.onOnlyFavouriteChange(true)

            assertEquals(true, vm.uiState.value.onlyFavourite)
        }

    @Test
    fun `onOnlyFavouriteChange false clears onlyFavourite flag`() =
        runTest {
            val vm = makeVm()

            vm.onOnlyFavouriteChange(true)
            vm.onOnlyFavouriteChange(false)

            assertEquals(false, vm.uiState.value.onlyFavourite)
        }

    @Test
    fun `syncFavouriteTickers success does not set error`() =
        runTest {
            val vm =
                makeVm(
                    syncFavouriteTickersUseCase = syncFavoriteTickersUseCaseMock(Result.success(Unit)),
                )

            yield()

            assertNull(vm.uiState.value.error)
        }

    @Test
    fun `syncFavouriteTickers failure sets error message`() =
        runTest {
            val vm =
                makeVm(
                    syncFavouriteTickersUseCase =
                        syncFavoriteTickersUseCaseMock(Result.failure(IllegalStateException("sync failed"))),
                )

            yield()

            assertEquals("sync failed", vm.uiState.value.error)
        }

    @Test
    fun `syncFavouriteTickers failure with null message falls back to default error`() =
        runTest {
            val vm =
                makeVm(
                    syncFavouriteTickersUseCase =
                        syncFavoriteTickersUseCaseMock(Result.failure(IllegalStateException(null as String?))),
                )

            yield()

            assertEquals("Couldn't sync favourite tickers", vm.uiState.value.error)
        }
}
