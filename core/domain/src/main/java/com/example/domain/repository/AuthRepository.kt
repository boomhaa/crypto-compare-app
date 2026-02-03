package com.example.domain.repository

import com.example.model.AuthUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: AuthUser?

    fun observeAuthState(): Flow<AuthUser?>

    suspend fun signUpWithEmail(
        email: String,
        password: String,
    ): Result<AuthUser>

    suspend fun signInWithEmail(
        email: String,
        password: String,
    ): Result<AuthUser>

    suspend fun signInWithGoogle(idToken: String): Result<AuthUser>

    suspend fun signOut()
}
