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
import com.google.gson.Gson
import com.google.gson.JsonParser
import io.mockk.slot
import okhttp3.RequestBody
import okio.Buffer
import org.junit.Assert.assertTrue

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

    @Test
    fun `updateCliente sends locality and explicit null coordinates so a removed location is cleared`() {
        val body = slot<RequestBody>()
        coEvery { apiServices.updateCliente(capture(body)) } returns Response.success(PostResponse(true, "ok"))

        runBlocking {
            repository.updateCliente(Cliente(idCl = 7, nombreCl = "Ana", numTelCl = "123", direccionCl = "Calle 1", localidadCl = "La Calera"))
        }

        val json = JsonParser.parseString(Buffer().also { body.captured.writeTo(it) }.readUtf8()).asJsonObject
        assertEquals(7, json.get("cl_ide").asInt)
        assertEquals("La Calera", json.get("cl_loc").asString)
        assertTrue(json.has("cl_lat") && json.get("cl_lat").isJsonNull)
        assertTrue(json.has("cl_lng") && json.get("cl_lng").isJsonNull)
        assertEquals("application/json; charset=utf-8", body.captured.contentType().toString())
    }

    @Test
    fun `cliente reads locality and coordinates sent as strings by the list`() {
        val cliente = Gson().fromJson(
            """{"cl_ide":"7","cl_nom":"Ana","cl_dir":"Calle 1","cl_tel":"1","cl_loc":"La Calera","cl_lat":"-31.343512","cl_lng":"-64.335871"}""",
            Cliente::class.java
        )
        assertEquals("La Calera", cliente.localidadCl)
        assertEquals(-31.343512, cliente.latitudCl!!, 0.0)
        assertEquals(-64.335871, cliente.longitudCl!!, 0.0)
    }
}
