package com.example.providers

import com.example.domain.repository.CryptoCompareRepository
import com.example.model.Provider
import com.example.model.ProviderStatus
import com.example.providers.viewmodel.providersviewmodel.ProvidersViewModel
import com.example.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProviderViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `init triggers loadProviders and sets loading true immediately`() =
        runTest {
            val repo = mockk<CryptoCompareRepository>()
            val gate = CompletableDeferred<Result<List<Provider>>>()
            coEvery { repo.getProviders() } coAnswers { gate.await() }

            val vm = ProvidersViewModel(repo)

            assertEquals(true, vm.uiState.value.loading)
            assertNull(vm.uiState.value.error)

            gate.complete(Result.success(emptyList()))
            yield()

            coVerify(exactly = 1) { repo.getProviders() }
        }

    @Test
    fun `loadProviders success updates providers and clears loading and error`() =
        runTest {
            val repo = mockk<CryptoCompareRepository>()
            val gate = CompletableDeferred<Result<List<Provider>>>()
            coEvery { repo.getProviders() } coAnswers { gate.await() }

            val vm = ProvidersViewModel(repo)

            assertEquals(true, vm.uiState.value.loading)
            assertEquals(emptyList<Provider>(), vm.uiState.value.providers)

            val providers =
                listOf(
                    Provider(
                        id = 1,
                        name = "Binance",
                        webSite = "https://binance.com",
                        status = ProviderStatus.Enabled,
                    ),
                    Provider(
                        id = 2,
                        name = "OKX",
                        webSite = null,
                        status = ProviderStatus.Disables,
                    ),
                )
            gate.complete(Result.success(providers))
            yield()

            assertEquals(false, vm.uiState.value.loading)
            assertNull(vm.uiState.value.error)
            assertEquals(providers, vm.uiState.value.providers)
        }

    @Test
    fun `loadProviders failure sets error and clears loading`() =
        runTest {
            val repo = mockk<CryptoCompareRepository>()
            val gate = CompletableDeferred<Result<List<Provider>>>()
            coEvery { repo.getProviders() } coAnswers { gate.await() }

            val vm = ProvidersViewModel(repo)

            gate.complete(Result.failure(IllegalStateException("boom")))
            yield()

            assertEquals(false, vm.uiState.value.loading)
            assertEquals("boom", vm.uiState.value.error)
            assertEquals(emptyList<Provider>(), vm.uiState.value.providers)
        }

    @Test
    fun `loadProviders resets previous error`() =
        runTest {
            val repo = mockk<CryptoCompareRepository>()

            coEvery { repo.getProviders() } returnsMany
                listOf(
                    Result.failure(RuntimeException("first")),
                    Result.success(emptyList()),
                )

            val vm = ProvidersViewModel(repo)

            yield()
            assertEquals("first", vm.uiState.value.error)
            assertEquals(false, vm.uiState.value.loading)

            vm.loadProviders()
            assertNull(vm.uiState.value.error)
        }
}
