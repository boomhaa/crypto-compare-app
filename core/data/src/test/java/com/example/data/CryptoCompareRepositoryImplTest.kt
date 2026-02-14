package com.example.data

import com.example.data.repository.CryptoCompareRepositoryImpl
import com.example.model.Provider
import com.example.model.ProviderStatus
import com.example.network.api.CryptoCompareApi
import com.example.network.dto.apiDTO.GetProvidersResponse
import com.example.network.dto.apiDTO.GetSymbolsResponse
import com.example.network.dto.apiDTO.ProviderDto
import com.example.network.dto.apiDTO.SymbolDto
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CryptoCompareRepositoryImplTest {
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

    @Test
    fun `getProviders returns success mapped providers when errorCode=0`() =
        runTest {
            val api = mockk<CryptoCompareApi>()
            val repo = CryptoCompareRepositoryImpl(api)

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
            assertEquals("", providers[1].name)
        }

    @Test
    fun `getProviders returns success empty list when providers is null`() =
        runTest {
            val api = mockk<CryptoCompareApi>()
            val repo = CryptoCompareRepositoryImpl(api)

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
        runTest {
            val api = mockk<CryptoCompareApi>()
            val repo = CryptoCompareRepositoryImpl(api)

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
        runTest {
            val api = mockk<CryptoCompareApi>()
            val repo = CryptoCompareRepositoryImpl(api)

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
    fun `getProviders returns failure when api throws exception`() =
        runTest {
            val api = mockk<CryptoCompareApi>()
            val repo = CryptoCompareRepositoryImpl(api)

            coEvery { api.getProviders() } throws IllegalStateException("boom")

            val result = repo.getProviders()

            assertTrue(result.isFailure)
            assertEquals("boom", result.exceptionOrNull()!!.message)
        }

    @Test(expected = CancellationException::class)
    fun `getProviders rethrows CancellationException`() =
        runTest {
            val api = mockk<CryptoCompareApi>()
            val repo = CryptoCompareRepositoryImpl(api)

            coEvery { api.getProviders() } throws CancellationException("cancel")

            repo.getProviders()
        }

    @Test
    fun `getAllSymbols returns flattened symbols for all providers`() =
        runTest {
            val api = mockk<CryptoCompareApi>()
            val repo = CryptoCompareRepositoryImpl(api)

            coEvery { api.getProviders() } returns
                GetProvidersResponse(
                    errorCode = 0,
                    errorMsgs = null,
                    providers = listOf(providerDto(1), providerDto(2)),
                )

            coEvery { api.getSymbolsByProvider(1, any(), any()) } returns
                GetSymbolsResponse(
                    errorCode = 0,
                    errorMsgs = null,
                    symbols = listOf(SymbolDto(11L, "btcusdt", "BTC/USDT", 1, 101.0, 99.0, "")),
                )

            coEvery { api.getSymbolsByProvider(2, null, null) } returns
                GetSymbolsResponse(
                    errorCode = 0,
                    errorMsgs = null,
                    symbols = listOf(SymbolDto(21L, "ethusdt", "ETH/USDT", 2, 11.0, 10.5, "")),
                )

            val result = repo.getSymbols()

            assertTrue(result.isSuccess)
            assertEquals(2, result.getOrThrow().size)
        }
}
