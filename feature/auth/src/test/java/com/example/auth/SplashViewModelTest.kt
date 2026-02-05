package com.example.auth

import com.example.auth.viewmodel.splashviewmodel.SplashViewModel
import com.example.domain.repository.AuthRepository
import com.example.helpers.util.Constants.SPLASH_DURATION_MS
import com.example.model.AuthUser
import com.example.testing.MainDispatcherRule
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository: AuthRepository = mockk(relaxed = true)

    @Before
    fun setUp() {
        clearMocks(authRepository)
    }

    @Test
    fun `init with authorized user sets authenticated true after splash delay`() =
        runTest {
            every { authRepository.currentUser } returns TEST_USER

            val viewModel = SplashViewModel(authRepository)

            assertTrue(viewModel.uiState.value.isCheckAuth)

            advanceTimeBy(SPLASH_DURATION_MS)
            runCurrent()

            assertFalse(viewModel.uiState.value.isCheckAuth)
            assertEquals(true, viewModel.uiState.value.isAuthenticated)
            assertNull(viewModel.uiState.value.errorMessage)
        }

    @Test
    fun `init with no user sets authenticated false after splash delay`() =
        runTest {
            every { authRepository.currentUser } returns null

            val viewModel = SplashViewModel(authRepository)

            advanceTimeBy(SPLASH_DURATION_MS)
            runCurrent()

            assertFalse(viewModel.uiState.value.isCheckAuth)
            assertEquals(false, viewModel.uiState.value.isAuthenticated)
            assertNull(viewModel.uiState.value.errorMessage)
        }

    @Test
    fun `init auth check failure exposes error and unauthenticated state`() =
        runTest {
            every { authRepository.currentUser } throws IllegalStateException("network down")

            val viewModel = SplashViewModel(authRepository)

            advanceTimeBy(SPLASH_DURATION_MS)
            runCurrent()

            assertFalse(viewModel.uiState.value.isCheckAuth)
            assertEquals(false, viewModel.uiState.value.isAuthenticated)
            assertEquals("network down", viewModel.uiState.value.errorMessage)
        }

    @Test
    fun `retry after failure succeeds and clears error`() =
        runTest {
            every { authRepository.currentUser } throws IllegalStateException("network down") andThen TEST_USER

            val viewModel = SplashViewModel(authRepository)

            advanceTimeBy(SPLASH_DURATION_MS)
            runCurrent()
            assertEquals("network down", viewModel.uiState.value.errorMessage)

            viewModel.checkAuthentification()

            advanceTimeBy(SPLASH_DURATION_MS)
            runCurrent()

            assertFalse(viewModel.uiState.value.isCheckAuth)
            assertEquals(true, viewModel.uiState.value.isAuthenticated)
            assertNull(viewModel.uiState.value.errorMessage)
        }

    private companion object {
        val TEST_USER =
            AuthUser(
                uid = "uid",
                email = "test@example.com",
                displayName = "Test",
                photoUrl = null,
            )
    }
}
