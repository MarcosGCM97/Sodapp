package com.example.sodappcomposse.auth

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AuthInterceptorTest {

    private class FakeTokenSource(var token: String?) : SessionTokenSource {
        val rejectedTokens = mutableListOf<String?>()
        override fun currentToken(): String? = token
        override fun onUnauthorized(rejectedToken: String?) {
            rejectedTokens += rejectedToken
        }
    }

    private lateinit var server: MockWebServer
    private lateinit var tokenSource: FakeTokenSource
    private lateinit var client: OkHttpClient

    @Before
    fun setup() {
        server = MockWebServer()
        server.start()
        tokenSource = FakeTokenSource("jwt-123")
        client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenSource))
            .build()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun call(path: String) {
        client.newCall(Request.Builder().url(server.url("/api_2/$path")).build()).execute().close()
    }

    @Test
    fun `adds bearer token to protected requests`() {
        server.enqueue(MockResponse().setResponseCode(200))

        call("clientes.php")

        assertEquals("Bearer jwt-123", server.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun `does not add the header to login requests`() {
        server.enqueue(MockResponse().setResponseCode(200))

        call("login.php")

        assertNull(server.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun `does not add the header when there is no token`() {
        tokenSource.token = null
        server.enqueue(MockResponse().setResponseCode(200))

        call("clientes.php")

        assertNull(server.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun `401 on a protected request reports the rejected token`() {
        server.enqueue(MockResponse().setResponseCode(401))

        call("ventas.php")

        assertEquals(listOf<String?>("jwt-123"), tokenSource.rejectedTokens)
    }

    @Test
    fun `401 on login does not report an expired session`() {
        server.enqueue(MockResponse().setResponseCode(401))

        call("login.php")

        assertTrue(tokenSource.rejectedTokens.isEmpty())
    }

    @Test
    fun `successful responses do not report an expired session`() {
        server.enqueue(MockResponse().setResponseCode(200))

        call("productos.php")

        assertTrue(tokenSource.rejectedTokens.isEmpty())
    }
}
