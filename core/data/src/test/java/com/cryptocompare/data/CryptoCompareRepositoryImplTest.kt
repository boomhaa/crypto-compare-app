package com.cryptocompare.data

import com.cryptocompare.data.local.dao.ProviderDao
import com.cryptocompare.data.local.dao.SymbolDao
import com.cryptocompare.data.local.entity.ProviderEntity
import com.cryptocompare.data.local.entity.SymbolEntity
import com.cryptocompare.data.repository.CryptoCompareRepositoryImpl
import com.cryptocompare.model.Provider
import com.cryptocompare.model.ProviderStatus
import com.cryptocompare.network.api.CryptoCompareApi
import com.cryptocompare.network.dto.apiDTO.GetProvidersResponse
import com.cryptocompare.network.dto.apiDTO.GetSymbolsResponse
import com.cryptocompare.network.dto.apiDTO.ProviderDto
import com.cryptocompare.network.dto.apiDTO.SymbolDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CryptoCompareRepositoryImplTest {
    private val dispatcher = StandardTestDispatcher()

    private fun providerDto(
        id: Int,
        name: String? = "Provider$id",
        status: ProviderStatus = ProviderStatus.Enabled,
    ) = ProviderDto(
        id = id,
        name = name,
        webSite = "https://p$id.example",
        baseUrl = "https://api.p$id.example",
        status = status,
    )

    private fun createRepo(
        api: CryptoCompareApi = mockk(),
        symbolDao: SymbolDao = mockk(),
        providerDao: ProviderDao = mockk(),
    ): CryptoCompareRepositoryImpl =
        CryptoCompareRepositoryImpl(
            cryptoCompareApi = api,
            symbolDao = symbolDao,
            providerDao = providerDao,
            ioDispatcher = dispatcher,
        )

    @Test
    fun `getProviders returns success mapped providers when errorCode=0`() =
        runTest(dispatcher) {
            val api = mockk<CryptoCompareApi>()
            val symbolDao = mockk<SymbolDao>(relaxed = true)
            val providerDao = mockk<ProviderDao>()
            val repo = createRepo(api, symbolDao, providerDao)

            coEvery { providerDao.getAll() } returns
                listOf(
                    ProviderEntity(1, "Binance", "https://p1.example", ProviderStatus.Enabled.name, 100L),
                    ProviderEntity(2, null, "https://p2.example", ProviderStatus.Enabled.name, 100L),
                )
            coEvery { providerDao.getLastUpdate() } returns 0L
            coEvery { providerDao.syncProviders(any()) } returns Unit

            coEvery { api.getProviders() } returns
                GetProvidersResponse(
                    errorCode = 0,
                    errorMsgs = null,
                    providers = listOf(providerDto(1, name = "Binance"), providerDto(2, name = null)),
                )

            val result = repo.getProviders()

            assertTrue(result.isSuccess)
            val providers = result.getOrThrow()
            assertEquals(2, providers.size)
            assertEquals(1, providers[0].id)
            assertEquals("Binance", providers[0].name)
            assertEquals(ProviderStatus.Enabled, providers[0].status)
            assertEquals(2, providers[1].id)
            assertEquals(null, providers[1].name)
            coVerify(exactly = 1) { api.getProviders() }
        }

    @Test
    fun `getProviders returns cached providers when cache is fresh`() =
        runTest(dispatcher) {
            val api = mockk<CryptoCompareApi>()
            val symbolDao = mockk<SymbolDao>(relaxed = true)
            val providerDao = mockk<ProviderDao>()
            val repo = createRepo(api, symbolDao, providerDao)
            val now = System.currentTimeMillis()

            coEvery { providerDao.getAll() } returns
                listOf(ProviderEntity(1, "Binance", "https://p1.example", ProviderStatus.Enabled.name, now))
            coEvery { providerDao.getLastUpdate() } returns now

            val result = repo.getProviders()

            assertTrue(result.isSuccess)
            assertEquals(1, result.getOrThrow().size)
            coVerify(exactly = 0) { api.getProviders() }
        }

    @Test
    fun `getProviders returns success empty list when providers is null`() =
        runTest(dispatcher) {
            val api = mockk<CryptoCompareApi>()
            val symbolDao = mockk<SymbolDao>(relaxed = true)
            val providerDao = mockk<ProviderDao>()
            val repo = createRepo(api, symbolDao, providerDao)

            coEvery { providerDao.getAll() } returnsMany listOf(emptyList(), emptyList())
            coEvery { providerDao.getLastUpdate() } returns 0L
            coEvery { providerDao.syncProviders(any()) } returns Unit

            coEvery { api.getProviders() } returns
                GetProvidersResponse(
                    errorCode = 0,
                    errorMsgs = null,
                    providers = null,
                )

            val result = repo.getProviders()

            assertTrue(result.isSuccess)
            assertEquals(emptyList<Provider>(), result.getOrThrow())
        }

    @Test
    fun `getProviders returns failure with joined error messages when errorCode != 0`() =
        runTest(dispatcher) {
            val api = mockk<CryptoCompareApi>()
            val symbolDao = mockk<SymbolDao>(relaxed = true)
            val providerDao = mockk<ProviderDao>()
            val repo = createRepo(api, symbolDao, providerDao)

            coEvery { providerDao.getAll() } returnsMany listOf(emptyList(), emptyList())
            coEvery { providerDao.getLastUpdate() } returns 0L

            coEvery { api.getProviders() } returns
                GetProvidersResponse(
                    errorCode = 10,
                    errorMsgs = listOf("E1", "E2"),
                    providers = null,
                )

            val result = repo.getProviders()

            assertTrue(result.isFailure)
            val ex = result.exceptionOrNull()
            assertNotNull(ex)
            assertEquals("E1\nE2", ex!!.message)
        }

    @Test
    fun `getProviders returns failure with Unknown error when errorCode != 0 and errorMsgs null`() =
        runTest(dispatcher) {
            val api = mockk<CryptoCompareApi>()
            val symbolDao = mockk<SymbolDao>(relaxed = true)
            val providerDao = mockk<ProviderDao>()
            val repo = createRepo(api, symbolDao, providerDao)

            coEvery { providerDao.getAll() } returnsMany listOf(emptyList(), emptyList())
            coEvery { providerDao.getLastUpdate() } returns 0L

            coEvery { api.getProviders() } returns
                GetProvidersResponse(
                    errorCode = 1,
                    errorMsgs = null,
                    providers = null,
                )

            val result = repo.getProviders()

            assertTrue(result.isFailure)
            assertEquals("Unknown error", result.exceptionOrNull()!!.message)
        }

    @Test
    fun `getProviders returns cached providers when api throws exception`() =
        runTest(dispatcher) {
            val api = mockk<CryptoCompareApi>()
            val symbolDao = mockk<SymbolDao>(relaxed = true)
            val providerDao = mockk<ProviderDao>()
            val repo = createRepo(api, symbolDao, providerDao)

            coEvery { providerDao.getAll() } returnsMany
                listOf(
                    emptyList(),
                    listOf(ProviderEntity(1, "Binance", "https://p1.example", ProviderStatus.Enabled.name, 100L)),
                    listOf(ProviderEntity(1, "Binance", "https://p1.example", ProviderStatus.Enabled.name, 100L)),
                )
            coEvery { providerDao.getLastUpdate() } returns 0L

            coEvery { api.getProviders() } throws IllegalStateException("boom")

            val result = repo.getProviders()

            assertTrue(result.isSuccess)
            assertEquals(1, result.getOrThrow().size)
        }

    @Test(expected = CancellationException::class)
    fun `getProviders rethrows CancellationException`() =
        runTest(dispatcher) {
            val api = mockk<CryptoCompareApi>()
            val symbolDao = mockk<SymbolDao>(relaxed = true)
            val providerDao = mockk<ProviderDao>()
            val repo = createRepo(api, symbolDao, providerDao)

            coEvery { providerDao.getAll() } returns emptyList()
            coEvery { providerDao.getLastUpdate() } returns 0L
            coEvery { api.getProviders() } throws CancellationException("cancel")

            repo.getProviders()
        }

    @Test
    fun `getSymbols returns mapped symbols from database flow`() =
        runTest(dispatcher) {
            val api = mockk<CryptoCompareApi>(relaxed = true)
            val symbolDao = mockk<SymbolDao>()
            val providerDao = mockk<ProviderDao>(relaxed = true)
            val repo = createRepo(api, symbolDao, providerDao)

            coEvery { symbolDao.getAll() } returns
                listOf(SymbolEntity(11L, "btcusdt", "BTC/USDT", 1, 101.0, 1, 99.0, "", 100L))
            coEvery { symbolDao.getLastUpdate() } returns System.currentTimeMillis()
            coEvery { symbolDao.observeAll() } returns
                flowOf(listOf(SymbolEntity(11L, "btcusdt", "BTC/USDT", 1, 101.0, 1, 99.0, "", 100L)))

            val pages = mutableListOf<List<com.cryptocompare.model.Symbol>>()
            repo.getSymbols().collect { pages.add(it) }

            assertEquals(1, pages.size)
            assertEquals("btcusdt", pages[0].single().ticker)
            coVerify(exactly = 0) { api.getSymbols(any(), any()) }
        }

    @Test
    fun `refreshCatalog fetches symbol pages and writes merged result`() =
        runTest(dispatcher) {
            val api = mockk<CryptoCompareApi>()
            val symbolDao = mockk<SymbolDao>(relaxed = true)
            val providerDao = mockk<ProviderDao>()
            val repo = createRepo(api, symbolDao, providerDao)

            coEvery { providerDao.getAll() } returnsMany
                listOf(
                    emptyList(),
                    listOf(ProviderEntity(1, "Provider1", "https://p1.example", ProviderStatus.Enabled.name, 100L)),
                    listOf(ProviderEntity(2, "Provider2", "https://p2.example", ProviderStatus.Enabled.name, 100L)),
                )
            coEvery { providerDao.getLastUpdate() } returns 0L
            coEvery { providerDao.syncProviders(any()) } returns Unit

            coEvery { api.getProviders() } returns
                GetProvidersResponse(
                    errorCode = 0,
                    errorMsgs = null,
                    providers = listOf(providerDto(1), providerDto(2)),
                )

            coEvery { api.getSymbols(skip = 0, rows = 25) } returns
                GetSymbolsResponse(
                    errorCode = 0,
                    errorMsgs = null,
                    symbols = listOf(SymbolDto(11L, "btcusdt", "BTC/USDT", 1, 101.0, 2, 99.0, "")),
                )

            coEvery { api.getSymbols(skip = 25, rows = 25) } returns
                GetSymbolsResponse(
                    errorCode = 0,
                    errorMsgs = null,
                    symbols = listOf(SymbolDto(21L, "ethusdt", "ETH/USDT", 1, 11.0, 2, 10.5, "")),
                )

            coEvery { api.getSymbols(skip = 50, rows = 25) } returns
                GetSymbolsResponse(
                    errorCode = 0,
                    errorMsgs = null,
                    symbols = emptyList(),
                )

            val result = repo.refreshCatalog()

            assertTrue(result.isSuccess)
            coVerify(exactly = 1) { symbolDao.syncSymbols(withArg { assertEquals(2, it.size) }) }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `getSymbols does not crash when background refresh throws`() =
        runTest(dispatcher) {
            val api = mockk<CryptoCompareApi>()
            val symbolDao = mockk<SymbolDao>()
            val providerDao = mockk<ProviderDao>()
            val repo = createRepo(api, symbolDao, providerDao)

            val cached =
                listOf(SymbolEntity(11L, "btcusdt", "BTC/USDT", 1, 101.0, 1, 99.0, "", 100L))

            coEvery { symbolDao.getAll() } returns cached
            coEvery { symbolDao.getLastUpdate() } returns 0L
            coEvery { symbolDao.observeAll() } returns flowOf(cached)
            coEvery { providerDao.getAll() } returns emptyList()
            coEvery { providerDao.getLastUpdate() } returns 0L
            coEvery { api.getProviders() } throws java.net.SocketTimeoutException("timeout")

            val emitted = mutableListOf<List<com.cryptocompare.model.Symbol>>()
            val collectJob =
                launch {
                    repo.getSymbols().collect { page ->
                        emitted += page
                    }
                }

            advanceUntilIdle()
            collectJob.cancel()

            assertEquals(1, emitted.size)
            assertEquals("btcusdt", emitted.first().first().ticker)
            coVerify(exactly = 1) { api.getProviders() }
        }
}
