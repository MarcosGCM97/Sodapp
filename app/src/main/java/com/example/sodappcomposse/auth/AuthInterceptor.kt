package com.example.sodappcomposse.auth

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

/**
 * Source of the session token used by [AuthInterceptor].
 */
interface SessionTokenSource {
    /** Token to send with the next request, or null when there is no session. */
    fun currentToken(): String?

    /** Called when the server rejected a request that was sent with [rejectedToken]. */
    fun onUnauthorized(rejectedToken: String?)
}

/**
 * Adds `Authorization: Bearer <token>` to every request except the login one,
 * and reports 401 answers so the app can end the session.
 */
class AuthInterceptor(
    private val tokenSource: SessionTokenSource
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (isLoginRequest(request)) {
            return chain.proceed(request)
        }

        val token = tokenSource.currentToken()
        val authorizedRequest = if (token.isNullOrBlank()) {
            request
        } else {
            request.newBuilder()
                .header(AUTHORIZATION_HEADER, "Bearer $token")
                .build()
        }

        val response = chain.proceed(authorizedRequest)
        if (response.code == HTTP_UNAUTHORIZED) {
            tokenSource.onUnauthorized(token)
        }
        return response
    }

    private fun isLoginRequest(request: Request): Boolean =
        request.url.pathSegments.lastOrNull() == LOGIN_PATH

    private companion object {
        const val AUTHORIZATION_HEADER = "Authorization"
        const val LOGIN_PATH = "login.php"
        const val HTTP_UNAUTHORIZED = 401
    }
}
