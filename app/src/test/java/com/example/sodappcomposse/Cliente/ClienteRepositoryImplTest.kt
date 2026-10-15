package com.example.sodappcomposse.Cliente

import com.example.sodappcomposse.API.ApiServices
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

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
}
