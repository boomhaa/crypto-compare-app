package com.example.pairs

import com.example.domain.repository.CryptoCompareRepository
import com.example.domain.repository.TickerStreamRepository
import com.example.model.Symbol
import com.example.model.TickerConnectionState
import com.example.model.TickerPrice
import com.example.model.TickerStreamEvent
import com.example.pairs.viewmodel.mainViewModel.MainViewModel
import com.example.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

    private fun tickerRepoMock(
        events: MutableSharedFlow<TickerStreamEvent> = MutableSharedFlow(extraBufferCapacity = 8),
    ): TickerStreamRepository =
        mockk {
            every { event } returns events
            every { connectionState } returns flowOf(TickerConnectionState.Disconnected)
            every { connect() } just runs
            every { disconnect() } just runs
            every { subscribe(any()) } just runs
            every { unsubscribe(any()) } just runs
        }

    @Test
    fun `init triggers loadPairs and sets loading true immediately`() =
        runTest {
            val repo = mockk<CryptoCompareRepository>()
            val tickerRepo = tickerRepoMock()
            val gate = MutableSharedFlow<List<Symbol>>(extraBufferCapacity = 1)
            coEvery { repo.getSymbols() } returns gate

            val vm = MainViewModel(repo, tickerRepo)

            assertEquals(true, vm.uiState.value.loading)
            assertNull(vm.uiState.value.error)

            yield()
            verify(exactly = 1) { tickerRepo.connect() }
        }

    @Test
    fun `loadPairs success aggregates pages and updates ui state`() =
        runTest {
            val repo = mockk<CryptoCompareRepository>()
            val tickerRepo = tickerRepoMock()

            val page1 =
                listOf(
                    Symbol(1L, "btcusdt", "BTC/USDT", 1, 101.0, 99.0),
                    Symbol(2L, "ethusdt", "ETH/USDT", 1, 11.0, 10.0),
                )
            val page2 = listOf(Symbol(3L, "btcusdt", "BTC/USDT", 2, 102.0, 98.5))

            coEvery { repo.getSymbols() } returns flowOf(page1, page2)

            val vm = MainViewModel(repo, tickerRepo)

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
    fun `ticker event updates prices for existing symbol id`() =
        runTest {
            val repo = mockk<CryptoCompareRepository>()
            val events = MutableSharedFlow<TickerStreamEvent>(extraBufferCapacity = 8)
            val tickerRepo = tickerRepoMock(events)

            coEvery { repo.getSymbols() } returns
                flowOf(
                    listOf(
                        Symbol(1L, "btcusdt", "BTC/USDT", 1, 101.0, 99.0),
                        Symbol(2L, "btcusdt", "BTC/USDT", 2, 103.0, 98.0),
                    ),
                )

            val vm = MainViewModel(repo, tickerRepo)
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
            val repo = mockk<CryptoCompareRepository>()
            val tickerRepo = tickerRepoMock()
            coEvery { repo.getSymbols() } returns
                flow {
                    throw IllegalStateException("boom")
                }
            val vm = MainViewModel(repo, tickerRepo)
            yield()

            assertEquals(false, vm.uiState.value.loading)
            assertEquals("boom", vm.uiState.value.error)
            assertTrue(
                vm.uiState.value.pairs
                    .isEmpty(),
            )
        }

    @Test
    fun `onVisibleTickersChange subscribes and unsubscribes normalized tickers`() =
        runTest {
            val repo = mockk<CryptoCompareRepository>()
            val tickerRepo = tickerRepoMock()
            coEvery { repo.getSymbols() } returns emptyFlow()

            val vm = MainViewModel(repo, tickerRepo)

            yield()
            vm.onVisibleTickersChange(listOf("BTCUSDT", "ethusdt", ""))
            vm.onVisibleTickersChange(listOf("ETHUSDT"))

            verify { tickerRepo.subscribe("btcusdt") }
            verify { tickerRepo.subscribe("ethusdt") }
            verify { tickerRepo.unsubscribe("btcusdt") }
        }
}
