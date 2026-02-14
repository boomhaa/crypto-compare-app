package com.example.pairs

import com.example.domain.repository.CryptoCompareRepository
import com.example.domain.repository.TickerStreamRepository
import com.example.model.Symbol
import com.example.model.TickerConnectionState
import com.example.pairs.viewmodel.mainViewModel.MainViewModel
import com.example.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
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

    private fun tickerRepoMock(): TickerStreamRepository =
        mockk {
            every { event } returns emptyFlow()
            every { connectionState } returns flowOf(TickerConnectionState.Disconnected)
            every { connect() } just runs
            every { disconnect() } just runs
            every { subscribe(any()) } returns true
            every { unsubscribe(any()) } returns true
        }

    @Test
    fun `init triggers loadPairs and sets loading true immediately`() =
        runTest {
            val repo = mockk<CryptoCompareRepository>()
            val gate = CompletableDeferred<Result<List<Symbol>>>()
            coEvery { repo.getSymbols() } coAnswers { gate.await() }

            val vm = MainViewModel(repo, tickerRepoMock())

            assertEquals(true, vm.uiState.value.loading)
            assertNull(vm.uiState.value.error)

            gate.complete(Result.success(emptyList()))
            yield()
        }

    @Test
    fun `loadPairs success updates pairs and clears loading and error`() =
        runTest {
            val repo = mockk<CryptoCompareRepository>()
            val gate = CompletableDeferred<Result<List<Symbol>>>()
            coEvery { repo.getSymbols() } coAnswers { gate.await() }

            val vm = MainViewModel(repo, tickerRepoMock())

            assertEquals(true, vm.uiState.value.loading)

            val symbols =
                listOf(
                    Symbol(1L, "btcusdt", "BTC/USDT", 1, 101.0, 99.0),
                    Symbol(2L, "btcusdt", "BTC/USDT", 2, 103.0, 98.0),
                )
            gate.complete(Result.success(symbols))
            yield()

            assertEquals(false, vm.uiState.value.loading)
            assertNull(vm.uiState.value.error)
            assertEquals(1, vm.uiState.value.pairs.size)
            assertEquals(
                "BTCUSDT",
                vm.uiState.value.pairs
                    .first()
                    .ticker,
            )
            assertEquals(
                98.0,
                vm.uiState.value.pairs
                    .first()
                    .minPrice,
                0.0,
            )
            assertEquals(
                103.0,
                vm.uiState.value.pairs
                    .first()
                    .maxPrice,
                0.0,
            )
        }

    @Test
    fun `loadPairs subscribes once per ticker even for duplicate symbols`() =
        runTest {
            val repo = mockk<CryptoCompareRepository>()
            val tickerRepo = tickerRepoMock()

            coEvery { repo.getSymbols() } returns
                Result.success(
                    listOf(
                        Symbol(1L, "btcusdt", "BTC/USDT", 1, 101.0, 99.0),
                        Symbol(2L, "btcusdt", "BTC/USDT", 1, 101.0, 99.0),
                    ),
                )

            MainViewModel(repo, tickerRepo)
            yield()

            verify(exactly = 1) { tickerRepo.subscribe("btcusdt") }
        }

    @Test
    fun `ticker event updates pair when symbol id differs but provider and ticker match`() =
        runTest {
            val repo = mockk<CryptoCompareRepository>()
            val events = MutableSharedFlow<com.example.model.TickerStreamEvent>(replay = 0, extraBufferCapacity = 8)
            val tickerRepo =
                mockk<TickerStreamRepository> {
                    every { event } returns events
                    every { connectionState } returns flowOf(TickerConnectionState.Disconnected)
                    every { connect() } just runs
                    every { disconnect() } just runs
                    every { subscribe(any()) } returns true
                    every { unsubscribe(any()) } returns true
                }

            coEvery { repo.getSymbols() } returns
                Result.success(
                    listOf(
                        Symbol(1L, "btcusdt", "BTC/USDT", 1, 101.0, 99.0),
                        Symbol(2L, "ethusdt", "ETH/USDT", 2, 20.0, 19.0),
                    ),
                )

            val vm = MainViewModel(repo, tickerRepo)
            yield()

            events.tryEmit(
                com.example.model.TickerStreamEvent.TickerPriceChange(
                    id = "evt1",
                    data =
                        com.example.model.TickerPrice(
                            ticker = "btcusdt",
                            symbolId = 999,
                            providerId = 1,
                            priceSell = 150.0,
                            priceBuy = 140.0,
                        ),
                ),
            )
            yield()

            val btcPair =
                vm.uiState.value.pairs
                    .first { it.ticker == "BTCUSDT" }
            assertEquals(140.0, btcPair.minPrice, 0.0)
            assertTrue(btcPair.maxPrice >= 150.0)
        }

    @Test
    fun `loadPairs failure sets error and clears loading`() =
        runTest {
            val repo = mockk<CryptoCompareRepository>()
            val gate = CompletableDeferred<Result<List<Symbol>>>()
            coEvery { repo.getSymbols() } coAnswers { gate.await() }

            val vm = MainViewModel(repo, tickerRepoMock())

            gate.complete(Result.failure(IllegalStateException("boom")))
            yield()

            assertEquals(false, vm.uiState.value.loading)
            assertEquals("boom", vm.uiState.value.error)
            assertEquals(0, vm.uiState.value.pairs.size)
        }

    @Test
    fun `loadPairs resets previous error`() =
        runTest {
            val repo = mockk<CryptoCompareRepository>()

            coEvery { repo.getSymbols() } returnsMany
                listOf(
                    Result.failure(RuntimeException("first")),
                    Result.success(emptyList()),
                )

            val vm = MainViewModel(repo, tickerRepoMock())

            yield()
            assertEquals("first", vm.uiState.value.error)
            assertEquals(false, vm.uiState.value.loading)

            vm.loadPairs()
            assertNull(vm.uiState.value.error)
        }
}
