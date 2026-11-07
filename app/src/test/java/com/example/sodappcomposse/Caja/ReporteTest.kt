package com.example.sodappcomposse.Caja

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class ReporteTest {

    private fun reporte(
        periodo: Periodo = Periodo.de(TipoPeriodo.MES, LocalDate.of(2026, 3, 1)),
        resumen: ResumenReporte = ResumenReporte(),
        detalle: List<LineaReporte> = emptyList(),
        primerCobro: LocalDateTime? = null
    ) = Reporte(periodo, resumen, ResumenAnterior(), emptyList(), emptyList(), emptyList(), emptyList(), detalle, primerCobro)

    @Test
    fun `week runs Monday to Sunday, also across months and years`() {
        assertEquals(Periodo(TipoPeriodo.SEMANA, LocalDate.of(2025, 12, 29), LocalDate.of(2026, 1, 4)),
            Periodo.de(TipoPeriodo.SEMANA, LocalDate.of(2026, 1, 1)))
        assertEquals(Periodo(TipoPeriodo.SEMANA, LocalDate.of(2026, 3, 16), LocalDate.of(2026, 3, 22)),
            Periodo.de(TipoPeriodo.SEMANA, LocalDate.of(2026, 3, 22)))
    }

    @Test
    fun `month covers every day, leap February included`() {
        assertEquals(Periodo(TipoPeriodo.MES, LocalDate.of(2028, 2, 1), LocalDate.of(2028, 2, 29)),
            Periodo.de(TipoPeriodo.MES, LocalDate.of(2028, 2, 15)))
        assertEquals(Periodo(TipoPeriodo.MES, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28)),
            Periodo.de(TipoPeriodo.MES, LocalDate.of(2026, 3, 31)).anterior())
    }

    @Test
    fun `day, next and previous periods`() {
        val dia = Periodo.de(TipoPeriodo.DIA, LocalDate.of(2026, 3, 1))
        assertEquals(LocalDate.of(2026, 2, 28), dia.anterior().desde)
        assertEquals(LocalDate.of(2026, 3, 2), dia.siguiente().hasta)
    }

    @Test
    fun `ticket promedio is vendido over ventas rounded half-up, null without sales`() {
        assertEquals(450.13, reporte(resumen = ResumenReporte(vendido = 900.25, ventas = 2)).ticketPromedio!!, 0.0)
        assertNull(reporte().ticketPromedio)
    }

    @Test
    fun `cobros are partial only when the first payment is after the period start`() {
        assertTrue(reporte(primerCobro = LocalDateTime.of(2026, 3, 10, 8, 0)).cobrosParciales)
        assertFalse(reporte(primerCobro = LocalDateTime.of(2026, 3, 1, 8, 0)).cobrosParciales)
        assertFalse(reporte(primerCobro = null).cobrosParciales)
    }

    @Test
    fun `porcentaje and variacion are null when the base is zero`() {
        assertEquals(25.0, porcentaje(50.0, 200.0)!!, 0.0)
        assertNull(porcentaje(1.0, 0.0))
        assertEquals(-50.0, variacion(100.0, 200.0)!!, 0.0)
        assertNull(variacion(100.0, 0.0))
    }

    @Test
    fun `detail groups lines of the same cliente and timestamp with an exact total`() {
        val l1 = LineaReporte("2026-03-02 10:00:00", 7, "Ana", "Soda", 1, 0.1)
        val l2 = LineaReporte("2026-03-02 10:00:00", 7, "Ana", "Agua", 1, 0.2)
        val l3 = LineaReporte("2026-03-02 10:00:00", 8, "Beto", "Soda", 2, 300.0)

        val ventas = reporte(detalle = listOf(l1, l2, l3)).ventasDetalle

        assertEquals(2, ventas.size)
        assertEquals(listOf(l1, l2), ventas[0].lineas)
        assertEquals(0.3, ventas[0].total, 0.0) // 0.1 + 0.2 would be 0.30000000000000004 in Double
        assertEquals("Beto", ventas[1].cliente)
    }
}
