package com.example.sodappcomposse.Producto

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductoParsingTest {

    private val gson = Gson()

    @Test
    fun `a returnable product maps pr_ret and pr_vac sent as strings`() {
        val json = """{"pr_ide":"1","pr_nom":"Bidon 20L","pr_val":"4500.00","pr_stk":"3","pr_ret":"1","pr_vac":"5"}"""

        val producto = gson.fromJson(json, Producto::class.java)

        assertTrue(producto.esRetornable)
        assertEquals(5, producto.vacios)
    }

    @Test
    fun `a returnable product maps pr_ret and pr_vac sent as numbers`() {
        val json = """{"pr_ide":1,"pr_nom":"Bidon 20L","pr_val":4500,"pr_stk":3,"pr_ret":1,"pr_vac":2}"""

        val producto = gson.fromJson(json, Producto::class.java)

        assertTrue(producto.esRetornable)
        assertEquals(2, producto.vacios)
    }

    @Test
    fun `a product without the container fields keeps parsing as non returnable`() {
        val json = """{"pr_ide":"1","pr_nom":"Soda","pr_val":"900.00","pr_stk":"7"}"""

        val producto = gson.fromJson(json, Producto::class.java)

        assertFalse(producto.esRetornable)
        assertEquals(0, producto.vacios)
        assertEquals(7, producto.stock)
        assertEquals(900.0, producto.precioUni, 0.0)
    }
}
