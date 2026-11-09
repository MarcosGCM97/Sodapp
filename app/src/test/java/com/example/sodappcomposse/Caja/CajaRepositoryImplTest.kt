package com.example.sodappcomposse.Caja

import com.example.sodappcomposse.API.ApiServices
import com.example.sodappcomposse.R
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.IOException
import java.time.LocalDate
import java.time.LocalDateTime

class CajaRepositoryImplTest {

    private lateinit var apiServices: ApiServices
    private lateinit var repository: CajaRepositoryImpl

    private val semana = Periodo.de(TipoPeriodo.SEMANA, LocalDate.of(2026, 3, 4))

    private fun respuesta(
        desde: String = "2026-03-02",
        hasta: String = "2026-03-08",
        success: Boolean = true,
        resumen: ResumenReporte? = ResumenReporte(vendido = 900.25, cobrado = 450.0, diferencia = 450.25, ventas = 2, clientes = 2),
        primerCobro: String? = "2025-11-01 09:30:00"
    ) = ReporteResponse(
        success = success, desde = desde, hasta = hasta, resumen = resumen,
        anterior = ResumenAnterior("2026-02-23", "2026-03-01", 200.0, 70.0, 1),
        cobros = listOf(CobroForma("Efectivo", 400.0)), productos = null, clientes = null, dias = null,
        detalle = null, primerCobro = primerCobro
    )

    @Before
    fun setup() {
        apiServices = mockk()
        repository = CajaRepositoryImpl(apiServices)
    }

    @Test
    fun `getReporte asks for the period and the previous one of the same kind`() = runTest {
        coEvery { apiServices.getReporte(any(), any(), any(), any()) } returns Response.success(respuesta())

        repository.getReporte(semana)

        coVerify { apiServices.getReporte("2026-03-02", "2026-03-08", "2026-02-23", "2026-03-01") }
    }

    @Test
    fun `getReporte of a month compares with the whole previous calendar month`() = runTest {
        coEvery { apiServices.getReporte(any(), any(), any(), any()) } returns
            Response.success(respuesta(desde = "2026-03-01", hasta = "2026-03-31"))

        repository.getReporte(Periodo.de(TipoPeriodo.MES, LocalDate.of(2026, 3, 15)))

        coVerify { apiServices.getReporte("2026-03-01", "2026-03-31", "2026-02-01", "2026-02-28") }
    }

    @Test
    fun `getReporte maps the response and turns missing lists into empty ones`() = runTest {
        coEvery { apiServices.getReporte(any(), any(), any(), any()) } returns Response.success(respuesta())

        val result = repository.getReporte(semana) as ReporteResult.Success

        assertEquals(semana, result.reporte.periodo)
        assertEquals(900.25, result.reporte.resumen.vendido, 0.0)
        assertEquals(listOf(CobroForma("Efectivo", 400.0)), result.reporte.cobros)
        assertEquals(emptyList<ProductoReporte>(), result.reporte.productos)
        assertEquals(emptyList<LineaReporte>(), result.reporte.detalle)
        assertEquals(LocalDateTime.of(2025, 11, 1, 9, 30), result.reporte.primerCobro)
    }

    @Test
    fun `getReporte rejects a response of another range`() = runTest {
        coEvery { apiServices.getReporte(any(), any(), any(), any()) } returns
            Response.success(respuesta(desde = "2026-03-09", hasta = "2026-03-15"))

        assertEquals(ReporteResult.Error(R.string.reporte_respuesta_invalida), repository.getReporte(semana))
    }

    @Test
    fun `getReporte rejects an unsuccessful or incomplete body`() = runTest {
        coEvery { apiServices.getReporte(any(), any(), any(), any()) } returns Response.success(respuesta(success = false))
        assertEquals(ReporteResult.Error(R.string.reporte_respuesta_invalida), repository.getReporte(semana))

        coEvery { apiServices.getReporte(any(), any(), any(), any()) } returns Response.success(respuesta(resumen = null))
        assertEquals(ReporteResult.Error(R.string.reporte_respuesta_invalida), repository.getReporte(semana))
    }

    @Test
    fun `getReporte returns Error with the HTTP code`() = runTest {
        coEvery { apiServices.getReporte(any(), any(), any(), any()) } returns
            Response.error(400, "{}".toResponseBody(null))

        assertEquals(ReporteResult.Error(R.string.error_api_format, listOf(400)), repository.getReporte(semana))
    }

    @Test
    fun `getReporte returns Error on network failure`() = runTest {
        coEvery { apiServices.getReporte(any(), any(), any(), any()) } throws IOException("sin red")

        assertEquals(ReporteResult.Error(R.string.error_red_format, listOf("sin red")), repository.getReporte(semana))
    }

    @Test
    fun `getReporte rethrows cancellation`() {
        coEvery { apiServices.getReporte(any(), any(), any(), any()) } throws CancellationException("cancelado")

        assertThrows(CancellationException::class.java) { runTest { repository.getReporte(semana) } }
    }
}
