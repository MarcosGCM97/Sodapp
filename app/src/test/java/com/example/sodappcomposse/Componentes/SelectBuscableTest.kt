package com.example.sodappcomposse.Componentes

import org.junit.Assert.assertEquals
import org.junit.Test

class SelectBuscableTest {

    private val clientes = listOf("José Pérez", "Ana López", "MARIANA Gómez")

    @Test
    fun `blank text keeps every option`() {
        assertEquals(clientes, filtrarOpciones(clientes, "  ") { it })
    }

    @Test
    fun `filters by any part of the label ignoring case`() {
        assertEquals(listOf("Ana López", "MARIANA Gómez"), filtrarOpciones(clientes, "ana") { it })
    }

    @Test
    fun `ignores accents in both the text and the label`() {
        assertEquals(listOf("José Pérez"), filtrarOpciones(clientes, "jose perez") { it })
        assertEquals(listOf("Ana López"), filtrarOpciones(clientes, "lópez") { it })
    }

    @Test
    fun `no match returns an empty list`() {
        assertEquals(emptyList<String>(), filtrarOpciones(clientes, "zzz") { it })
    }
}
