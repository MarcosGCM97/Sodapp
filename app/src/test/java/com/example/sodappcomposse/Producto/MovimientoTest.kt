package com.example.sodappcomposse.Producto

import com.example.sodappcomposse.R
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MovimientoTest {

    private val gson = Gson()

    @Test
    fun `the history response maps every movement field`() {
        val json = """{"success":true,"movimientos":[
            {"ms_ide":7,"ms_tip":"venta","ms_can":-3,"ms_stk":4,"ms_vta":524,"ms_fec":"2026-10-30 12:05:00"},
            {"ms_ide":6,"ms_tip":"alta","ms_can":10,"ms_stk":10,"ms_vta":null,"ms_fec":"2026-10-30 11:00:00"}]}"""

        val response = gson.fromJson(json, MovimientosResponse::class.java)

        assertEquals(2, response.movimientos.size)
        val venta = response.movimientos[0]
        assertEquals(7, venta.id)
        assertEquals("venta", venta.tipo)
        assertEquals(-3, venta.cantidad)
        assertEquals(4, venta.stock)
        assertEquals(524, venta.ventaId)
        assertEquals("2026-10-30 12:05:00", venta.fecha)
        assertNull(response.movimientos[1].ventaId)
    }

    @Test
    fun `quantities show their sign`() {
        assertEquals("+2", Movimiento(cantidad = 2).cantidadConSigno)
        assertEquals("-3", Movimiento(cantidad = -3).cantidadConSigno)
        assertEquals("0", Movimiento(cantidad = 0).cantidadConSigno)
    }

    @Test
    fun `each movement type has its label and unknown types fall back to a generic one`() {
        assertEquals(R.string.movimiento_venta, Movimiento(tipo = "venta").tipoLabel)
        assertEquals(R.string.movimiento_venta_eliminada, Movimiento(tipo = "venta_eliminada").tipoLabel)
        assertEquals(R.string.movimiento_llenado, Movimiento(tipo = "llenado").tipoLabel)
        assertEquals(R.string.movimiento_ajuste, Movimiento(tipo = "ajuste").tipoLabel)
        assertEquals(R.string.movimiento_alta, Movimiento(tipo = "alta").tipoLabel)
        assertEquals(R.string.movimiento_otro, Movimiento(tipo = "otro").tipoLabel)
    }
}
