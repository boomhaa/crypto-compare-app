package com.example.auth.viewmodel.splashviewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.repository.AuthRepository
import com.example.helpers.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel
    @Inject
    constructor(
        private val authRepository: AuthRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(SplashUiState())
        val uiState = _uiState.asStateFlow()

        init {
            checkAuthentification()
        }

        fun checkAuthentification() {
            viewModelScope.launch {
                _uiState.update { uiState -> uiState.copy(isCheckAuth = true, errorMessage = null) }
                delay(Constants.SPLASH_DURATION_MS)
                runCatching { authRepository.currentUser }
                    .onSuccess { user ->
                        _uiState.update { uiState ->
                            uiState.copy(
                                isCheckAuth = false,
                                isAuthenticated = user != null,
                            )
                        }
                    }.onFailure { error ->
                        _uiState.update { uiState ->
                            uiState.copy(
                                isCheckAuth = false,
                                errorMessage = error.message ?: "Network error. Please try again!",
                            )
                        }
                    }
            }
        }
    }
