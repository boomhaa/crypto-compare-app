package com.example.auth.viewmodel.registrationviewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegistrationViewModel
    @Inject
    constructor(
        private val authRepository: AuthRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(RegistrationUiState())
        val uiState = _uiState.asStateFlow()

        fun onEmailChange(email: String) {
            _uiState.update { uiState -> uiState.copy(email = email) }
        }

        fun onPasswordChange(password: String) {
            _uiState.update { uiState -> uiState.copy(password = password) }
        }

        fun onConfirmPasswordChange(confirmPassword: String) {
            _uiState.update { uiState -> uiState.copy(confirmPassword = confirmPassword) }
        }

        fun signUpWithEmail() {
            val email = _uiState.value.email.trim()
            val password = _uiState.value.password
            val confirmPassword = _uiState.value.confirmPassword

            if (!isValidEmail(email)) {
                _uiState.update { uiState -> uiState.copy(errorMessage = "Incorrect email was entered") }
                return
            }

            if (password.length < 6 || !password.any { it.isLetter() } || !password.any { it.isDigit() }) {
                _uiState.update { it.copy(errorMessage = "Password must be stronger") }
                return
            }
            if (password != confirmPassword) {
                _uiState.update { it.copy(errorMessage = "Passwords don't match") }
                return
            }

            _uiState.update { uiState -> uiState.copy(isLoading = true, errorMessage = null) }

            viewModelScope.launch {
                authRepository
                    .signUpWithEmail(email, password)
                    .onSuccess { _uiState.update { uiState -> uiState.copy(isLoading = false) } }
                    .onFailure { error ->
                        _uiState.update { uiState ->
                            uiState.copy(
                                isLoading = false,
                                errorMessage = error.message ?: "Sign up error",
                            )
                        }
                    }
            }
        }

        fun signUpWithGoogle(idToken: String) {
            _uiState.update { uiState -> uiState.copy(isLoading = true, errorMessage = null) }

            viewModelScope.launch {
                authRepository
                    .signInWithGoogle(idToken)
                    .onSuccess { _uiState.update { uiState -> uiState.copy(isLoading = false) } }
                    .onFailure { error ->
                        _uiState.update { uiState ->
                            uiState.copy(
                                isLoading = false,
                                errorMessage = error.message ?: "Login error",
                            )
                        }
                    }
            }
        }

        fun onGoogleError(message: String) {
            _uiState.update { it.copy(errorMessage = message) }
        }

        private fun isValidEmail(email: String): Boolean = email.isNotBlank() && EMAIL_REGEX.matches(email)

        private companion object {
            val EMAIL_REGEX = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
        }
    }
