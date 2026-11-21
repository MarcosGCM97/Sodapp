package com.example.sodappcomposse.Componentes

import com.example.sodappcomposse.R
import org.junit.Assert.assertEquals
import org.junit.Test

class HeaderOverflowMenuTest {

    @Test
    fun `the owner gets the theme switch and the company data`() {
        assertEquals(
            listOf(OpcionMenuEncabezado.TEMA, OpcionMenuEncabezado.DATOS_EMPRESA),
            opcionesMenuEncabezado(puedeEditarEmpresa = true)
        )
    }

    @Test
    fun `other users get only the theme switch`() {
        assertEquals(listOf(OpcionMenuEncabezado.TEMA), opcionesMenuEncabezado(puedeEditarEmpresa = false))
    }

    @Test
    fun `the theme option shows the mode it switches to`() {
        assertEquals(R.string.tema_claro_desc, OpcionMenuEncabezado.TEMA.textoRes(isDark = true))
        assertEquals(R.drawable.outline_light_mode_24, OpcionMenuEncabezado.TEMA.iconoRes(isDark = true))
        assertEquals(R.string.tema_oscuro_desc, OpcionMenuEncabezado.TEMA.textoRes(isDark = false))
        assertEquals(R.drawable.outline_dark_mode_24, OpcionMenuEncabezado.TEMA.iconoRes(isDark = false))
    }

    @Test
    fun `the owner gets the WhatsApp messages after the company data`() {
        assertEquals(
            listOf(OpcionMenuEncabezado.TEMA, OpcionMenuEncabezado.DATOS_EMPRESA, OpcionMenuEncabezado.MENSAJES),
            opcionesMenuEncabezado(puedeEditarEmpresa = true, puedeEditarMensajes = true)
        )
    }

    @Test
    fun `the messages screen hides only its own entry and other screens can hide the company data alone`() {
        assertEquals(
            listOf(OpcionMenuEncabezado.TEMA, OpcionMenuEncabezado.DATOS_EMPRESA),
            opcionesMenuEncabezado(puedeEditarEmpresa = true, puedeEditarMensajes = false)
        )
        assertEquals(
            listOf(OpcionMenuEncabezado.TEMA, OpcionMenuEncabezado.MENSAJES),
            opcionesMenuEncabezado(puedeEditarEmpresa = false, puedeEditarMensajes = true)
        )
    }

    @Test
    fun `the messages option keeps its label and uses a Material icon`() {
        assertEquals(R.string.mensajes_titulo, OpcionMenuEncabezado.MENSAJES.textoRes(isDark = true))
        assertEquals(R.string.mensajes_titulo, OpcionMenuEncabezado.MENSAJES.textoRes(isDark = false))
        assertEquals(null, OpcionMenuEncabezado.MENSAJES.iconoRes(isDark = false))
    }

    @Test
    fun `the company data option keeps its label in both modes`() {
        assertEquals(R.string.empresa_titulo, OpcionMenuEncabezado.DATOS_EMPRESA.textoRes(isDark = true))
        assertEquals(R.string.empresa_titulo, OpcionMenuEncabezado.DATOS_EMPRESA.textoRes(isDark = false))
    }
}
