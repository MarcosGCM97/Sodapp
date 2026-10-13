package com.example.sodappcomposse.auth

import com.example.sodappcomposse.UserPreferencesRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AuthSessionTest {

    private lateinit var preferences: UserPreferencesRepository
    private lateinit var session: AuthSession

    @Before
    fun setup() {
        preferences = mockk(relaxUnitFun = true)
        every { preferences.token } returns flowOf("stored-token")
        session = AuthSession(preferences)
    }

    @Test
    fun `current token is loaded from preferences`() {
        assertEquals("stored-token", session.currentToken())
    }

    @Test
    fun `unauthorized with the current token marks the session as expired`() {
        session.onUnauthorized(session.currentToken())

        assertTrue(session.sessionExpired.value)
    }

    @Test
    fun `unauthorized with a stale token is ignored`() {
        session.currentToken()

        session.onUnauthorized("old-token")

        assertFalse(session.sessionExpired.value)
    }

    @Test
    fun `starting a session persists the token and clears the expired flag`() = runTest {
        session.onUnauthorized(session.currentToken())

        session.startSession(userId = "5", userName = "marcos", token = "new-token")

        coVerify { preferences.saveUserData("5", "marcos", "new-token") }
        assertEquals("new-token", session.currentToken())
        assertFalse(session.sessionExpired.value)
    }

    @Test
    fun `ending a session forgets the token and clears stored user data`() = runTest {
        coEvery { preferences.clearUserData() } returns Unit
        session.currentToken()

        session.endSession()

        assertNull(session.currentToken())
        coVerify { preferences.clearUserData() }
    }
}
