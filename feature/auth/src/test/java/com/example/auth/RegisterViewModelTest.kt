package com.example.auth

import com.example.auth.viewmodel.registrationviewmodel.RegistrationViewModel
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
class RegisterViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository: AuthRepository = mockk(relaxed = true)

    @Test
    fun `signUp with invalid email sets error`() =
        runTest {
            val viewModel = RegistrationViewModel(authRepository)

            viewModel.onEmailChange("bad")
            viewModel.onPasswordChange("Password1")
            viewModel.onConfirmPasswordChange("Password1")

            viewModel.signUpWithEmail()

            assertEquals("Incorrect email was entered", viewModel.uiState.value.errorMessage)
            coVerify(exactly = 0) { authRepository.signUpWithEmail(any(), any()) }
        }

    @Test
    fun `signUp with weak password sets error`() =
        runTest {
            val viewModel = RegistrationViewModel(authRepository)

            viewModel.onEmailChange("user@example.com")
            viewModel.onPasswordChange("short")
            viewModel.onConfirmPasswordChange("short")

            viewModel.signUpWithEmail()

            assertEquals("Password must be stronger", viewModel.uiState.value.errorMessage)
            coVerify(exactly = 0) { authRepository.signUpWithEmail(any(), any()) }
        }

    @Test
    fun `signUp with mismatched password sets error`() =
        runTest {
            val viewModel = RegistrationViewModel(authRepository)

            viewModel.onEmailChange("user@example.com")
            viewModel.onPasswordChange("Password1")
            viewModel.onConfirmPasswordChange("Password2")

            viewModel.signUpWithEmail()

            assertEquals("Passwords don't match", viewModel.uiState.value.errorMessage)
            coVerify(exactly = 0) { authRepository.signUpWithEmail(any(), any()) }
        }

    @Test
    fun `signUp success clears loading`() =
        runTest {
            coEvery { authRepository.signUpWithEmail(any(), any()) } returns Result.success(mockk())
            val viewModel = RegistrationViewModel(authRepository)

            viewModel.onEmailChange("user@example.com")
            viewModel.onPasswordChange("Password1")
            viewModel.onConfirmPasswordChange("Password1")

            viewModel.signUpWithEmail()
            assertEquals(true, viewModel.uiState.value.isLoading)

            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.isLoading)
            assertNull(viewModel.uiState.value.errorMessage)
        }

    @Test
    fun `signUp failure sets error`() =
        runTest {
            coEvery { authRepository.signUpWithEmail(any(), any()) } returns
                Result.failure(IllegalStateException("fail"))
            val viewModel = RegistrationViewModel(authRepository)

            viewModel.onEmailChange("user@example.com")
            viewModel.onPasswordChange("Password1")
            viewModel.onConfirmPasswordChange("Password1")

            viewModel.signUpWithEmail()
            advanceUntilIdle()

            assertEquals("fail", viewModel.uiState.value.errorMessage)
            assertFalse(viewModel.uiState.value.isLoading)
        }

    @Test
    fun `signUpWithGoogle blank token sets error`() =
        runTest {
            val viewModel = RegistrationViewModel(authRepository)

            viewModel.signUpWithGoogle("")

            assertEquals("Google token not found", viewModel.uiState.value.errorMessage)
            coVerify(exactly = 0) { authRepository.signInWithGoogle(any()) }
        }

    @Test
    fun `signUpWithGoogle success clears loading`() =
        runTest {
            coEvery { authRepository.signInWithGoogle(any()) } returns Result.success(mockk())
            val viewModel = RegistrationViewModel(authRepository)

            viewModel.signUpWithGoogle("token")
            assertEquals(true, viewModel.uiState.value.isLoading)

            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.isLoading)
            assertNull(viewModel.uiState.value.errorMessage)
        }
}
