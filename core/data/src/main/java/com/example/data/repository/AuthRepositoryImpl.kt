package com.example.data.repository

import com.example.data.mapper.toAuthUser
import com.example.domain.repository.AuthRepository
import com.example.model.AuthUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
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
                try {
                    val result = auth.createUserWithEmailAndPassword(email, password).await()
                    val user = result.user ?: error("Firebase returned null user")
                    user.toAuthUser()
                } catch (e: Exception) {
                    error(e.message ?: "Sign up with email error")
                }
            }

        override suspend fun signInWithEmail(
            email: String,
            password: String,
        ): Result<AuthUser> =
            runCatching {
                try {
                    val result = auth.signInWithEmailAndPassword(email, password).await()
                    val user = result.user ?: error("Firebase returned null user")
                    user.toAuthUser()
                } catch (e: Exception) {
                    error(e.message ?: "Sign in with email error")
                }
            }

        override suspend fun signInWithGoogle(idToken: String): Result<AuthUser> =
            runCatching {
                try {
                    val credential = GoogleAuthProvider.getCredential(idToken, null)
                    val result = auth.signInWithCredential(credential).await()
                    val user = result.user ?: error("Firebase returned null user")
                    user.toAuthUser()
                } catch (e: Exception) {
                    error(e.message ?: "Sign in with google error")
                }
            }

        override suspend fun signOut() {
            auth.signOut()
        }
    }
