package com.example.sodappcomposse.Caja

import com.example.sodappcomposse.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class ReportePdfTest {

    // Resource ids rendered as "<id>[args]" so every block can be asserted exactly
    private val textos: Textos = { id, args -> "$id" + args.joinToString(",", "[", "]") }
    private fun t(id: Int, vararg args: Any) = textos(id, args)

    private val semana = Periodo.de(TipoPeriodo.SEMANA, LocalDate.of(2026, 3, 2))

    private fun reporte(
        resumen: ResumenReporte = ResumenReporte(vendido = 900.25, cobrado = 450.0, diferencia = 450.25, ventas = 2, clientes = 2),
        detalle: List<LineaReporte> = listOf(
            LineaReporte("2026-03-02 10:00:00", 7, "Ana", "Soda", 2, 500.0),
            LineaReporte("2026-03-02 10:00:00", 7, "Ana", "Agua", 1, 100.25),
            LineaReporte("2026-03-04 23:30:00", 8, null, "Soda", 1, 300.0, estimado = true)
        ),
        primerCobro: LocalDateTime? = null
    ) = Reporte(
        semana, resumen, ResumenAnterior("2026-02-23", "2026-03-01", 200.0, 70.0, 1),
        listOf(CobroForma("Efectivo", 400.0), CobroForma("Transferencia", 50.0)),
        listOf(ProductoReporte("Soda", 3, 800.0), ProductoReporte("Agua", 1, 100.25)),
        listOf(ClienteReporte(7, "Ana", 1, 600.25), ClienteReporte(8, null, 1, 300.0)),
        listOf(DiaReporte("2026-03-02", 600.25), DiaReporte("2026-03-04", 300.0)),
        detalle, primerCobro
    )

    private fun pdf(r: Reporte = reporte(), empresa: String? = "Ariza") =
        contenidoPdf(r, empresa, LocalDateTime.of(2026, 3, 9, 8, 5), textos)

    @Test
    fun `header names the report, the company, the period and when it was generated`() {
        val bloques = pdf()
        assertEquals(BloquePdf.Titulo(t(R.string.pdf_titulo)), bloques[0])
        assertEquals(BloquePdf.Texto("Ariza"), bloques[1])
        assertEquals(BloquePdf.Texto(t(R.string.pdf_periodo, "02/03 al 08/03/2026", "02/03/2026", "08/03/2026")), bloques[2])
        assertEquals(BloquePdf.Texto(t(R.string.pdf_generado, "09/03/2026 08:05")), bloques[3])
    }

    @Test
    fun `summary carries the same figures as the screen`() {
        val filas = pdf().filterIsInstance<BloquePdf.Fila>()
        assertTrue(filas.contains(BloquePdf.Fila(t(R.string.reporte_vendido),
            "\$900,25  (${t(R.string.reporte_vs_anterior, "+350,1%")})", negrita = true)))
        assertTrue(filas.contains(BloquePdf.Fila(t(R.string.reporte_diferencia), "\$450,25", negrita = true)))
        assertTrue(filas.contains(BloquePdf.Fila(t(R.string.reporte_ticket), "\$450,13")))
        assertTrue(filas.contains(BloquePdf.Fila("Efectivo", "\$400,00  (88,9%)")))
        assertTrue(filas.contains(BloquePdf.Fila("Soda · ${t(R.string.reporte_unidades_format, 3)}", "\$800,00  (88,9%)")))
        assertTrue(filas.contains(BloquePdf.Fila("02/03/2026", "\$600,25")))
    }

    @Test
    fun `detail lists every sale with its lines and marks estimated amounts`() {
        val bloques = pdf()
        val i = bloques.indexOf(BloquePdf.Subtitulo(t(R.string.reporte_detalle_titulo)))
        assertEquals(
            listOf(
                BloquePdf.Fila("02/03 10:00 · Ana", "\$600,25", negrita = true),
                BloquePdf.Fila(t(R.string.reporte_linea_format, 2, "Soda"), "\$500,00", sangria = true),
                BloquePdf.Fila(t(R.string.reporte_linea_format, 1, "Agua"), "\$100,25", sangria = true),
                BloquePdf.Fila("04/03 23:30 · ${t(R.string.reporte_cliente_desconocido)}", "\$300,00", negrita = true),
                BloquePdf.Fila(t(R.string.reporte_linea_format, 1, "Soda"), "\$300,00 *", sangria = true)
            ),
            bloques.subList(i + 1, i + 6)
        )
    }

    @Test
    fun `warnings appear only when they apply`() {
        val conAvisos = pdf(reporte(
            resumen = ResumenReporte(vendido = 1.0, ventas = 1, lineasEstimadas = 1),
            primerCobro = LocalDateTime.of(2026, 3, 5, 9, 0)
        ))
        assertTrue(conAvisos.contains(BloquePdf.Texto(t(R.string.reporte_aviso_estimadas, 1))))
        assertTrue(conAvisos.contains(BloquePdf.Texto(t(R.string.reporte_aviso_cobros_desde, "05/03/2026"))))

        val sinAvisos = pdf(reporte(detalle = emptyList()), empresa = null)
        assertFalse(sinAvisos.any { it is BloquePdf.Texto && it.texto.startsWith("${R.string.reporte_aviso_estimadas}") })
        assertFalse(sinAvisos.contains(BloquePdf.Texto("Ariza")))
        assertFalse(sinAvisos.contains(BloquePdf.Subtitulo(t(R.string.reporte_detalle_titulo))))
    }

    @Test
    fun `cobrado comparison says it is incomplete instead of a percentage`() {
        val filas = pdf(reporte(primerCobro = LocalDateTime.of(2026, 2, 26, 8, 0))).filterIsInstance<BloquePdf.Fila>()
        assertTrue(filas.contains(BloquePdf.Fila(t(R.string.reporte_cobrado),
            "\$450,00  (${t(R.string.reporte_comparacion_incompleta)})", negrita = true)))
    }

    @Test
    fun `file name carries the period`() {
        assertEquals("reporte_2026-03-02_2026-03-08.pdf", nombreArchivoPdf(semana))
    }
}
