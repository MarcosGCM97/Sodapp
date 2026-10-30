package com.example.sodappcomposse.Funciones

import android.content.res.Resources
import com.example.sodappcomposse.R
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

class FormatUtilsTest {

    private val resources = mockk<Resources> {
        every { getString(R.string.deuda_format, any()) } answers { "Deuda: $" + (args[1] as Array<*>)[0] }
        every { getString(R.string.saldo_a_favor_format, any()) } answers { "Saldo a favor: $" + (args[1] as Array<*>)[0] }
    }

    @Test
    fun `a negative debt is shown as a positive credit balance`() {
        assertEquals("Saldo a favor: $700,00", textoSaldoCliente(resources, -700.0))
    }

    @Test
    fun `a positive or zero debt is still shown as debt`() {
        assertEquals("Deuda: $300,00", textoSaldoCliente(resources, 300.0))
        assertEquals("Deuda: $0,00", textoSaldoCliente(resources, 0.0))
        assertEquals("Deuda: $0,00", textoSaldoCliente(resources, null))
    }
}
