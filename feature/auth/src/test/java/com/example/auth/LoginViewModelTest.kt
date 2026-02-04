package com.example.auth

import com.example.auth.viewmodel.loginviewmodel.LoginViewModel
import com.example.domain.repository.AuthRepository
import com.example.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository: AuthRepository = mockk(relaxed = true)

    @Test
    fun `signIn with invalid email sets error`() =
        runTest {
            val viewModel = LoginViewModel(authRepository)

            viewModel.onEmailChange("bad")
            viewModel.onPasswordChange("secret123")

            viewModel.signInWithEmail()

            assertEquals("Incorrect email was entered", viewModel.uiState.value.errorMessage)
            coVerify(exactly = 0) { authRepository.signInWithEmail(any(), any()) }
        }

    @Test
    fun `signIn with short password sets error`() =
        runTest {
            val viewModel = LoginViewModel(authRepository)

            viewModel.onEmailChange("user@example.com")
            viewModel.onPasswordChange("123")

            viewModel.signInWithEmail()

            assertEquals("Password must have more than 6 symbols", viewModel.uiState.value.errorMessage)
            coVerify(exactly = 0) { authRepository.signInWithEmail(any(), any()) }
        }

    @Test
    fun `signIn success clears loading`() =
        runTest {
            coEvery { authRepository.signInWithEmail(any(), any()) } returns Result.success(mockk())
            val viewModel = LoginViewModel(authRepository)

            viewModel.onEmailChange("user@example.com")
            viewModel.onPasswordChange("secret123")

            viewModel.signInWithEmail()
            assertEquals(true, viewModel.uiState.value.isLoading)

            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.isLoading)
            assertNull(viewModel.uiState.value.errorMessage)
        }

    @Test
    fun `signIn failure sets error`() =
        runTest {
            coEvery { authRepository.signInWithEmail(any(), any()) } returns
                Result.failure(IllegalStateException("fail"))
            val viewModel = LoginViewModel(authRepository)

            viewModel.onEmailChange("user@example.com")
            viewModel.onPasswordChange("secret123")

            viewModel.signInWithEmail()
            advanceUntilIdle()

            assertEquals("fail", viewModel.uiState.value.errorMessage)
            assertFalse(viewModel.uiState.value.isLoading)
        }

    @Test
    fun `signInWithGoogle blank token sets error`() =
        runTest {
            val viewModel = LoginViewModel(authRepository)

            viewModel.signInWithGoogle("")

            assertEquals("Google token not found", viewModel.uiState.value.errorMessage)
            coVerify(exactly = 0) { authRepository.signInWithGoogle(any()) }
        }

    @Test
    fun `signInWithGoogle success clears loading`() =
        runTest {
            coEvery { authRepository.signInWithGoogle(any()) } returns Result.success(mockk())
            val viewModel = LoginViewModel(authRepository)

            viewModel.signInWithGoogle("token")
            assertEquals(true, viewModel.uiState.value.isLoading)

            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.isLoading)
            assertNull(viewModel.uiState.value.errorMessage)
        }
}
