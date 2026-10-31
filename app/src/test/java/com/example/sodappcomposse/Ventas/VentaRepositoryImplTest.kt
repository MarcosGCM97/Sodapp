package com.example.sodappcomposse.Ventas

import com.example.sodappcomposse.API.ApiServices
import com.example.sodappcomposse.API.PostResponse
import com.example.sodappcomposse.R
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class VentaRepositoryImplTest {

    private lateinit var apiServices: ApiServices
    private lateinit var repository: VentaRepositoryImpl
    private val request = VentaRequest(1, emptyList(), "7")

    @Before
    fun setup() {
        apiServices = mockk()
        repository = VentaRepositoryImpl(apiServices)
    }

    @Test
    fun `a 409 reports the status code like any other server error`() = runTest {
        val body = """{"success":false,"error":"Stock insuficiente: Bidon 20L"}""".toResponseBody()
        coEvery { apiServices.postVenta(request) } returns Response.error<PostResponse>(409, body)

        val result = repository.postVenta(request)

        assertEquals(VentaResult.Error(R.string.error_servidor_format, arrayOf(409)), result)
    }

    @Test
    fun `other server errors keep reporting the status code`() = runTest {
        val body = """{"success":false,"error":"boom"}""".toResponseBody()
        coEvery { apiServices.postVenta(request) } returns Response.error<PostResponse>(500, body)

        val result = repository.postVenta(request)

        assertEquals(VentaResult.Error(R.string.error_servidor_format, arrayOf(500)), result)
    }
}
