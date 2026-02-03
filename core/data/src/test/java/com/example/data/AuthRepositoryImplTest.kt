package com.example.data

import app.cash.turbine.test
import com.example.data.repository.AuthRepositoryImpl
import com.example.model.AuthUser
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthRepositoryImplTest {
    private val auth: FirebaseAuth = mockk(relaxed = true)
    private val repository = AuthRepositoryImpl(auth)

    @Test
    fun `currentUser maps firebase user`() {
        val firebaseUser = mockk<FirebaseUser>()
        every { firebaseUser.uid } returns "uid-1"
        every { firebaseUser.email } returns "user@example.com"
        every { firebaseUser.displayName } returns "User"
        every { firebaseUser.photoUrl } returns null
        every { auth.currentUser } returns firebaseUser

        val result = repository.currentUser

        assertEquals(
            AuthUser(
                uid = "uid-1",
                email = "user@example.com",
                displayName = "User",
                photoUrl = null,
            ),
            result,
        )
    }

    @Test
    fun `observeAuthState emits updates and unregisters listener`() = runTest {
        val listenerSlot = slot<FirebaseAuth.AuthStateListener>()
        var currentUser: FirebaseUser? = null
        every { auth.currentUser } answers { currentUser }
        every { auth.addAuthStateListener(capture(listenerSlot)) } returns Unit
        every { auth.removeAuthStateListener(any()) } returns Unit

        repository.observeAuthState().test {
            val firstUser = mockk<FirebaseUser>()
            every { firstUser.uid } returns "uid-1"
            every { firstUser.email } returns "user@example.com"
            every { firstUser.displayName } returns null
            every { firstUser.photoUrl } returns null

            currentUser = firstUser
            listenerSlot.captured.onAuthStateChanged(auth)
            assertEquals(
                AuthUser(
                    uid = "uid-1",
                    email = "user@example.com",
                    displayName = null,
                    photoUrl = null,
                ),
                awaitItem(),
            )

            currentUser = null
            listenerSlot.captured.onAuthStateChanged(auth)
            assertNull(awaitItem())

            cancelAndIgnoreRemainingEvents()
        }

        verify { auth.removeAuthStateListener(listenerSlot.captured) }
    }

    @Test
    fun `signUpWithEmail returns mapped user on success`() = runTest {
        val firebaseUser = mockk<FirebaseUser>()
        every { firebaseUser.uid } returns "uid-2"
        every { firebaseUser.email } returns "new@example.com"
        every { firebaseUser.displayName } returns "New"
        every { firebaseUser.photoUrl } returns null
        val authResult = mockk<AuthResult>()
        every { authResult.user } returns firebaseUser
        every { auth.createUserWithEmailAndPassword("new@example.com", "secret") } returns
                Tasks.forResult(authResult)

        val result = repository.signUpWithEmail("new@example.com", "secret")

        assertTrue(result.isSuccess)
        assertEquals(
            AuthUser(
                uid = "uid-2",
                email = "new@example.com",
                displayName = "New",
                photoUrl = null,
            ),
            result.getOrNull(),
        )
    }

    @Test
    fun `signUpWithEmail returns failure when firebase throws`() = runTest {
        every { auth.createUserWithEmailAndPassword("bad@example.com", "secret") } returns
                Tasks.forException(IllegalStateException("No user"))

        val result = repository.signUpWithEmail("bad@example.com", "secret")

        assertTrue(result.isFailure)
        assertNotNull(result.exceptionOrNull())
    }

    @Test
    fun `signInWithEmail returns mapped user on success`() = runTest {
        val firebaseUser = mockk<FirebaseUser>()
        every { firebaseUser.uid } returns "uid-3"
        every { firebaseUser.email } returns "login@example.com"
        every { firebaseUser.displayName } returns null
        every { firebaseUser.photoUrl } returns null
        val authResult = mockk<AuthResult>()
        every { authResult.user } returns firebaseUser
        every { auth.signInWithEmailAndPassword("login@example.com", "secret") } returns
                Tasks.forResult(authResult)

        val result = repository.signInWithEmail("login@example.com", "secret")

        assertTrue(result.isSuccess)
        assertEquals(
            AuthUser(
                uid = "uid-3",
                email = "login@example.com",
                displayName = null,
                photoUrl = null,
            ),
            result.getOrNull(),
        )
    }

    @Test
    fun `signInWithGoogle uses credential and returns mapped user`() = runTest {
        val firebaseUser = mockk<FirebaseUser>()
        every { firebaseUser.uid } returns "uid-4"
        every { firebaseUser.email } returns "google@example.com"
        every { firebaseUser.displayName } returns "Google"
        every { firebaseUser.photoUrl } returns null
        val authResult = mockk<AuthResult>()
        every { authResult.user } returns firebaseUser
        every { auth.signInWithCredential(any()) } returns Tasks.forResult(authResult)

        val result = repository.signInWithGoogle("token")

        assertTrue(result.isSuccess)
        assertEquals(
            AuthUser(
                uid = "uid-4",
                email = "google@example.com",
                displayName = "Google",
                photoUrl = null,
            ),
            result.getOrNull(),
        )
        verify(exactly = 1) { auth.signInWithCredential(any()) }
    }

    @Test
    fun `signOut delegates to firebase auth`() = runTest {
        every { auth.signOut() } returns Unit

        repository.signOut()

        verify { auth.signOut() }
    }

    @Test
    fun `signInWithEmail returns failure when firebase throws`() = runTest {
        every { auth.signInWithEmailAndPassword("login@example.com", "secret") } returns
                Tasks.forException(IllegalArgumentException("fail"))

        val result = repository.signInWithEmail("login@example.com", "secret")

        assertTrue(result.isFailure)
        assertNotNull(result.exceptionOrNull())
    }

    @Test
    fun `signInWithGoogle returns failure when firebase throws`() = runTest {
        every { auth.signInWithCredential(any()) } returns
                Tasks.forException(IllegalStateException("fail"))

        val result = repository.signInWithGoogle("token")

        assertTrue(result.isFailure)
        assertNotNull(result.exceptionOrNull())
    }
}
