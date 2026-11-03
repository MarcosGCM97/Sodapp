package com.example.sodappcomposse.Ruta

import com.example.sodappcomposse.API.ApiServices
import com.example.sodappcomposse.Producto.ProductoResult
import com.example.sodappcomposse.R
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.IOException

class RutaRepositoryImplTest {

    private lateinit var apiServices: ApiServices
    private lateinit var repository: RutaRepositoryImpl

    @Before
    fun setup() {
        apiServices = mockk()
        repository = RutaRepositoryImpl(apiServices)
    }

    @Test
    fun `getRuta returns the route of the reparto`() = runTest {
        val ruta = Ruta(repartoId = 7, paradas = listOf(RutaParada(paradaId = 40, orden = 1, estado = "pendiente")))
        coEvery { apiServices.getRuta(7) } returns Response.success(RutaResponse(success = true, ruta = ruta))

        assertEquals(ProductoResult.Success(ruta), repository.getRuta(7))
    }

    @Test
    fun `getPlanta returns the plant or null when not configured`() = runTest {
        val planta = Planta("Ruta 20 km 6", "La Calera", -31.35, -64.33)
        coEvery { apiServices.getPlanta() } returns Response.success(RutaResponse(success = true, planta = planta))
        assertEquals(ProductoResult.Success(planta), repository.getPlanta())

        coEvery { apiServices.getPlanta() } returns Response.success(RutaResponse(success = true, planta = null))
        assertEquals(ProductoResult.Success(null), repository.getPlanta())
    }

    @Test
    fun `server error message is surfaced`() = runTest {
        coEvery { apiServices.accionRuta(any()) } returns
            Response.error(409, """{"success":false,"error":"Quedan paradas pendientes"}""".toResponseBody())

        assertEquals(ProductoResult.Error(R.string.error_format, arrayOf("Quedan paradas pendientes")), repository.finalizar(7))
    }

    @Test
    fun `network failure maps to the network error`() = runTest {
        coEvery { apiServices.getRuta(any()) } throws IOException("offline")

        assertEquals(ProductoResult.Error(R.string.error_red_verificar), repository.getRuta(7))
    }

    @Test
    fun `each action sends its accion and fields`() = runTest {
        coEvery { apiServices.accionRuta(any()) } returns Response.success(RutaResponse(success = true))

        repository.iniciar(7)
        repository.finalizar(7)
        repository.guardarPlanta(Planta("Ruta 20 km 6", "La Calera", null, null))

        coVerify {
            apiServices.accionRuta(RutaAccionRequest(accion = "iniciar", repartoId = 7))
            apiServices.accionRuta(RutaAccionRequest(accion = "finalizar", repartoId = 7))
            apiServices.accionRuta(RutaAccionRequest(accion = "guardar_planta", direccion = "Ruta 20 km 6", localidad = "La Calera"))
        }
    }
}
