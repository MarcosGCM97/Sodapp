package com.example.sodappcomposse.Cliente

import com.example.sodappcomposse.API.ApiServices
import com.example.sodappcomposse.API.PostResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class ClienteRepositoryImplTest {

    private lateinit var apiServices: ApiServices
    private lateinit var repository: ClienteRepositoryImpl

    @Before
    fun setup() {
        apiServices = mockk()
        repository = ClienteRepositoryImpl(apiServices)
    }

    @Test
    fun `getClienteById rethrows cancellation instead of mapping it to Error`() {
        coEvery { apiServices.getClienteById(25) } throws CancellationException("cancelled")

        assertThrows(CancellationException::class.java) {
            runBlocking { repository.getClienteById(25) }
        }
    }

    @Test
    fun `updateDeudaCliente rethrows cancellation instead of mapping it to Error`() {
        coEvery { apiServices.updateDeudaCliente(25, 500.0) } throws CancellationException("cancelled")

        assertThrows(CancellationException::class.java) {
            runBlocking { repository.updateDeudaCliente(25, 500.0) }
        }
    }

    @Test
    fun `getClientes rethrows cancellation instead of caching an Error`() {
        coEvery { apiServices.getClientes() } throws CancellationException("cancelled")

        assertThrows(CancellationException::class.java) {
            runBlocking { repository.getClientes() }
        }
    }
    @Test
    fun `updateDeudaCliente sends the payment method`() {
        coEvery { apiServices.updateDeudaCliente(25, 500.0, 3) } returns Response.success(PostResponse(true, "ok"))

        val result = runBlocking { repository.updateDeudaCliente(25, 500.0, 3) }

        assertEquals(ClienteResult.Success(PostResponse(true, "ok")), result)
        coVerify(exactly = 1) { apiServices.updateDeudaCliente(25, 500.0, 3) }
    }

    @Test
    fun `getFormasPago returns the methods from the backend`() {
        val formas = FormasPagoResponse(true, listOf(FormaPago(2, "Efectivo")))
        coEvery { apiServices.getFormasPago(1) } returns Response.success(formas)

        assertEquals(ClienteResult.Success(formas), runBlocking { repository.getFormasPago() })
    }
}
