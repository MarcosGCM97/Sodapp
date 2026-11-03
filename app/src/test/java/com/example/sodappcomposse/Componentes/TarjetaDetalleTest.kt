package com.example.sodappcomposse.Componentes

import org.junit.Assert.assertEquals
import org.junit.Test

class TarjetaDetalleTest {

    @Test
    fun `a client that owes money is an alert, credit is good and zero debt is neutral`() {
        assertEquals(TonoEtiqueta.ALERTA, tonoSaldo(1500.0))
        assertEquals(TonoEtiqueta.EXITO, tonoSaldo(-200.0))
        assertEquals(TonoEtiqueta.NEUTRO, tonoSaldo(0.0))
        assertEquals(TonoEtiqueta.NEUTRO, tonoSaldo(null))
    }

    @Test
    fun `stock at zero or below is an alert, any stock left is neutral`() {
        assertEquals(TonoEtiqueta.ALERTA, tonoStock(0))
        assertEquals(TonoEtiqueta.ALERTA, tonoStock(-3))
        assertEquals(TonoEtiqueta.NEUTRO, tonoStock(12))
    }
}
