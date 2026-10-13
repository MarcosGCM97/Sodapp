package com.example.sodappcomposse.auth

import com.example.sodappcomposse.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

/**
 * App-wide owner of the session token.
 *
 * The token is kept in memory so the network layer can read it without touching
 * DataStore on every request. It is loaded once from DataStore on first use (always
 * from an OkHttp worker thread) and then kept in sync by [startSession] and [endSession],
 * which are the only writers of the stored token.
 */
@Singleton
class AuthSession @Inject constructor(
    private val preferences: UserPreferencesRepository
) : SessionTokenSource {

    private val lock = Any()

    @Volatile
    private var cachedToken: String? = null

    @Volatile
    private var loaded = false

    private val _sessionExpired = MutableStateFlow(false)

    /**
     * True once the server rejected the current token. Stays true until a new session
     * starts, so several failing requests produce a single signal.
     */
    val sessionExpired: StateFlow<Boolean> = _sessionExpired.asStateFlow()

    override fun currentToken(): String? {
        if (!loaded) {
            synchronized(lock) {
                if (!loaded) {
                    cachedToken = runBlocking { preferences.token.first() }
                    loaded = true
                }
            }
        }
        return cachedToken
    }

    override fun onUnauthorized(rejectedToken: String?) {
        synchronized(lock) {
            // A late 401 for a token from a previous session must not end the current one
            if (loaded && rejectedToken != cachedToken) return
        }
        _sessionExpired.value = true
    }

    suspend fun startSession(userId: String, userName: String, token: String) {
        preferences.saveUserData(userId, userName, token)
        synchronized(lock) {
            cachedToken = token
            loaded = true
        }
        _sessionExpired.value = false
    }

    suspend fun endSession() {
        synchronized(lock) {
            cachedToken = null
            loaded = true
        }
        preferences.clearUserData()
    }
}
