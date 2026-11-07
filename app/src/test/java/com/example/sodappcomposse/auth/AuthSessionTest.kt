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

        session.startSession(userId = "5", userName = "marcos", token = "new-token", permissions = vendedor)

        coVerify { preferences.saveUserData("5", "marcos", "new-token", vendedor) }
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

    @Test
    fun `starting a session exposes its permissions`() = runTest {
        session.startSession(userId = "7", userName = "juan", token = "t", permissions = vendedor)

        assertEquals(vendedor, session.permissions.value)
    }

    @Test
    fun `ending a session drops the permissions`() = runTest {
        session.startSession(userId = "7", userName = "juan", token = "t", permissions = vendedor)

        session.endSession()

        assertEquals(SessionPermissions(), session.permissions.value)
    }

    @Test
    fun `permissions are unknown until loaded from preferences`() = runTest {
        every { preferences.permissions } returns flowOf(vendedor)
        assertNull(session.permissions.value)

        session.loadStoredPermissions()

        assertEquals(vendedor, session.permissions.value)
    }

    @Test
    fun `updating permissions persists and exposes them`() = runTest {
        session.currentToken()

        session.updatePermissions(dueno, "stored-token")

        coVerify { preferences.savePermissions(dueno) }
        assertEquals(dueno, session.permissions.value)
    }

    @Test
    fun `updating permissions without a session is ignored`() = runTest {
        session.endSession()

        session.updatePermissions(dueno, "stored-token")

        coVerify(exactly = 0) { preferences.savePermissions(any()) }
        assertEquals(SessionPermissions(), session.permissions.value)
    }

    @Test
    fun `a refresh requested by a previous session is ignored`() = runTest {
        session.currentToken()

        session.updatePermissions(dueno, "old-token")

        coVerify(exactly = 0) { preferences.savePermissions(any()) }
        assertEquals(null, session.permissions.value)
    }

    @Test
    fun `marking the session expired raises the expired flag`() {
        session.markExpired()

        assertTrue(session.sessionExpired.value)
    }

    private val vendedor = SessionPermissions(esDueno = false, permisos = setOf("ventas.ver", "clientes.ver"))
    private val dueno = SessionPermissions(esDueno = true, permisos = emptySet())
}
