package com.example.sodappcomposse.Envases

import com.example.sodappcomposse.API.ApiServices
import com.example.sodappcomposse.Producto.ProductoResult
import com.example.sodappcomposse.R
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class EnvaseRepositoryImplTest {

    private lateinit var apiServices: ApiServices
    private lateinit var repository: EnvaseRepositoryImpl

    @Before
    fun setup() {
        apiServices = mockk()
        repository = EnvaseRepositoryImpl(apiServices)
    }

    @Test
    fun `llenar sends the refill action with product name and quantity`() = runTest {
        coEvery { apiServices.llenarEnvases(any()) } returns Response.success(EnvaseResponse(success = true))

        val result = repository.llenarEnvases("Bidon 20L", 3)

        assertTrue(result is ProductoResult.Success)
        coVerify(exactly = 1) { apiServices.llenarEnvases(LlenarEnvasesRequest(accion = "llenar", producto = "Bidon 20L", cantidad = 3)) }
    }

    @Test
    fun `a 409 refill surfaces the server message`() = runTest {
        val body = """{"success":false,"error":"No hay suficientes envases vacíos"}""".toResponseBody()
        coEvery { apiServices.llenarEnvases(any()) } returns Response.error(409, body)

        val result = repository.llenarEnvases("Bidon 20L", 30)

        assertEquals(ProductoResult.Error(R.string.error_format, arrayOf("No hay suficientes envases vacíos")), result)
    }

    @Test
    fun `an error without a readable message falls back to the status code`() = runTest {
        coEvery { apiServices.llenarEnvases(any()) } returns Response.error(500, "boom".toResponseBody())

        val result = repository.llenarEnvases("Bidon 20L", 1)

        assertEquals(ProductoResult.Error(R.string.error_servidor_format, arrayOf(500)), result)
    }

    @Test
    fun `getEnvasesCliente returns the client balance per product`() = runTest {
        val envases = listOf(EnvaseCliente(productoId = 1, nombreProducto = "Bidon 20L", cantidad = 4))
        coEvery { apiServices.getEnvasesCliente(25) } returns Response.success(EnvasesClienteResponse(true, envases))

        val result = repository.getEnvasesCliente(25)

        assertEquals(ProductoResult.Success(EnvasesClienteResponse(true, envases)), result)
    }

    @Test
    fun `setSaldoEnvases sends client, product and absolute quantity`() = runTest {
        coEvery { apiServices.setSaldoEnvases(any()) } returns Response.success(EnvaseResponse(success = true))

        repository.setSaldoEnvases(clienteId = 25, productoId = 1, cantidad = 6)

        coVerify(exactly = 1) { apiServices.setSaldoEnvases(SaldoEnvasesRequest(clienteId = 25, productoId = 1, cantidad = 6)) }
    }

    @Test
    fun `cancellation is rethrown instead of mapped to Error`() {
        coEvery { apiServices.getEnvasesCliente(25) } throws CancellationException("cancelled")

        assertThrows(CancellationException::class.java) {
            runBlocking { repository.getEnvasesCliente(25) }
        }
    }
}
