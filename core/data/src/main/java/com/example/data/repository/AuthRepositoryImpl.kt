package com.example.data.repository

import com.example.data.mapper.toAuthUser
import com.example.domain.repository.AuthRepository
import com.example.model.AuthUser
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.error

class AuthRepositoryImpl
    @Inject
    constructor(
        private val auth: FirebaseAuth,
    ) : AuthRepository {
        override val currentUser: AuthUser?
            get() = auth.currentUser?.toAuthUser()

        override fun observeAuthState(): Flow<AuthUser?> =
            callbackFlow {
                val listener =
                    FirebaseAuth.AuthStateListener { firebaseAuth ->
                        trySend(firebaseAuth.currentUser?.toAuthUser())
                    }
                auth.addAuthStateListener(listener)
                awaitClose {
                    auth.removeAuthStateListener(listener)
                }
            }

        override suspend fun signUpWithEmail(
            email: String,
            password: String,
        ): Result<AuthUser> =
            runCatching {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val user = result.user ?: error("Firebase returned null user")
                user.toAuthUser()
            }

        override suspend fun singInWithEmail(
            email: String,
            password: String,
        ): Result<AuthUser> =
            runCatching {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                val user = result.user ?: error("Firebase returned null user")
                user.toAuthUser()
            }
    }
