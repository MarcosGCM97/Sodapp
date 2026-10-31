package com.example.sodappcomposse.Cliente

import org.junit.Assert.assertEquals
import org.junit.Test

class CobroClienteTest {

    private val deudores = listOf(
        Deudor(1, "Ana Pérez", 4500.0),
        Deudor(2, "Juan Gómez", 1200.0),
        Deudor(3, "ana maría", 300.0)
    )

    @Test
    fun `an empty search keeps every debtor`() {
        assertEquals(deudores, filtrarDeudores(deudores, "  "))
    }

    @Test
    fun `search matches part of the name ignoring case and spaces`() {
        assertEquals(listOf(deudores[0], deudores[2]), filtrarDeudores(deudores, " ANA "))
    }

    @Test
    fun `cobrar todo fills the whole balance without a decimal part when there is none`() {
        assertEquals("4500", montoCobrarTodo(4500.0))
        assertEquals("300.5", montoCobrarTodo(300.5))
    }
}
