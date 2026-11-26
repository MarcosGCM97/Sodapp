package com.example.sodappcomposse.Componentes

import org.junit.Assert.assertEquals
import org.junit.Test

class EncabezadoTest {

    @Test
    fun `a fresh login shows the name it brings`() {
        assertEquals("Ariza", nombreParaEncabezado(nombreRuta = "Ariza", nombreGuardado = "Otro"))
    }

    @Test
    fun `a restored session shows the stored name`() {
        assertEquals("Ariza", nombreParaEncabezado(nombreRuta = "", nombreGuardado = "Ariza"))
    }

    @Test
    fun `a restored session without a stored name shows nothing`() {
        assertEquals("", nombreParaEncabezado(nombreRuta = "", nombreGuardado = null))
    }
}
