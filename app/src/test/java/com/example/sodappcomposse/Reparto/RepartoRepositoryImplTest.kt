package com.example.sodappcomposse.Reparto

import com.example.sodappcomposse.API.ApiServices
import com.example.sodappcomposse.Producto.ProductoResult
import com.example.sodappcomposse.R
import com.google.gson.Gson
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.IOException

class RepartoRepositoryImplTest {

    private lateinit var apiServices: ApiServices
    private lateinit var repository: RepartoRepositoryImpl

    @Before
    fun setup() {
        apiServices = mockk()
        repository = RepartoRepositoryImpl(apiServices)
    }

    @Test
    fun `getReparto returns the reparto of the date`() = runTest {
        val reparto = Reparto(id = 7, fecha = "2026-11-02", dia = 1)
        coEvery { apiServices.getReparto("2026-11-02") } returns Response.success(RepartoResponse(success = true, reparto = reparto))

        assertEquals(ProductoResult.Success(reparto), repository.getReparto("2026-11-02"))
    }

    @Test
    fun `getReparto maps 404 to no reparto for that date`() = runTest {
        coEvery { apiServices.getReparto(any()) } returns
            Response.error(404, """{"success":false,"error":"Reparto no encontrado"}""".toResponseBody())

        assertEquals(ProductoResult.Success(null), repository.getReparto("2026-11-02"))
    }

    @Test
    fun `server error message is surfaced`() = runTest {
        coEvery { apiServices.accionReparto(any()) } returns
            Response.error(404, """{"success":false,"error":"No hay un reparto anterior de ese día"}""".toResponseBody())

        val result = repository.crearReparto("2026-11-02", OrigenReparto.ANTERIOR)

        assertEquals(ProductoResult.Error(R.string.error_format, arrayOf("No hay un reparto anterior de ese día")), result)
    }

    @Test
    fun `network failure maps to the network error`() = runTest {
        coEvery { apiServices.getReparto(any()) } throws IOException("offline")

        assertEquals(ProductoResult.Error(R.string.error_red_verificar), repository.getReparto("2026-11-02"))
    }

    @Test
    fun `each action sends its accion and fields`() = runTest {
        coEvery { apiServices.accionReparto(any()) } returns Response.success(RepartoResponse(success = true))
        val lineas = listOf(LineaRepartoRequest(productoId = 3, cantidad = 2, precio = 450.0))

        repository.crearReparto("2026-11-02", OrigenReparto.PLANTILLA)
        repository.guardarPlantilla(7)
        repository.agregarParada(7, 12)
        repository.quitarParada(40)
        repository.guardarLineas(40, lineas)
        repository.ordenarParadas(7, listOf(41, 40))

        coVerify { apiServices.accionReparto(RepartoAccionRequest(accion = "crear", fecha = "2026-11-02", origen = "plantilla")) }
        coVerify { apiServices.accionReparto(RepartoAccionRequest(accion = "guardar_plantilla", repartoId = 7)) }
        coVerify { apiServices.accionReparto(RepartoAccionRequest(accion = "agregar_parada", repartoId = 7, clienteId = 12)) }
        coVerify { apiServices.accionReparto(RepartoAccionRequest(accion = "quitar_parada", paradaId = 40)) }
        coVerify { apiServices.accionReparto(RepartoAccionRequest(accion = "guardar_lineas", paradaId = 40, lineas = lineas)) }
        coVerify { apiServices.accionReparto(RepartoAccionRequest(accion = "ordenar", repartoId = 7, paradas = listOf(41, 40))) }
    }

    @Test
    fun `delivery actions send the stop and the delivered lines`() = runTest {
        coEvery { apiServices.accionReparto(any()) } returns Response.success(RepartoResponse(success = true, monto = 900.0, ventas = listOf(81)))
        val lineas = listOf(LineaRepartoRequest(productoId = 3, cantidad = 2, devueltos = 1))

        val entrega = repository.entregarParada(40, lineas)
        repository.noEntregarParada(41)
        repository.reabrirParada(41)

        assertEquals(900.0, (entrega as ProductoResult.Success).data.monto)
        coVerify { apiServices.accionReparto(RepartoAccionRequest(accion = "entregar", paradaId = 40, lineas = lineas)) }
        coVerify { apiServices.accionReparto(RepartoAccionRequest(accion = "no_entregar", paradaId = 41)) }
        coVerify { apiServices.accionReparto(RepartoAccionRequest(accion = "reabrir", paradaId = 41)) }
    }

    @Test
    fun `request omits the fields of other actions`() {
        val json = Gson().toJson(RepartoAccionRequest(accion = "quitar_parada", paradaId = 40))

        assertEquals("""{"accion":"quitar_parada","paradaId":40}""", json)
        assertFalse(json.contains("null"))
    }

    @Test
    fun `parses the GET detail including string numbers and nulls`() {
        val json = """{"success":true,"reparto":{"rp_ide":"7","rp_fec":"2026-11-02","rp_dia":"1","rp_ini":null,"rp_fin":null,
            "paradas":[{"rs_ide":"40","rs_ord":"1","rs_est":"pendiente","cl_ide":"12","cl_nom":"Ana","cl_dir":"San Martín 10","cl_tel":null,
            "lineas":[{"rl_ide":"5","pr_ide":"3","pr_nom":"Bidón 20L","pr_ret":"1","rl_cpl":"2","rl_cen":null,"rl_pre":"450.00","rl_dev":null,"rl_vt":null}]}]}}"""

        val parada = Gson().fromJson(json, RepartoResponse::class.java).reparto!!.paradas.single()

        assertEquals("San Martín 10", parada.direccion)
        assertEquals(true, parada.esPendiente)
        assertEquals(LineaReparto(id = 5, productoId = 3, nombreProducto = "Bidón 20L", retornable = 1, cantidadPlanificada = 2, precio = 450.0), parada.lineas.single())
    }

    @Test
    fun `403 shows the missing permission message instead of the backend text`() = runTest {
        coEvery { apiServices.accionReparto(any()) } returns Response.error(
            403,
            """{"success":false,"error":"Forbidden: missing permission","permiso":"repartos.gestionar"}""".toResponseBody()
        )

        assertEquals(ProductoResult.Error(R.string.error_sin_permiso), repository.quitarParada(3))
    }
}
