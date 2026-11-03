package com.example.sodappcomposse.Producto

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductoVentaTest {

    @Test
    fun `subtotal is quantity times price, 0 without price`() {
        assertEquals(1500.0, ProductoVenta("Agua", cantidad = 3, precio = 500.0).subtotal, 0.0)
        assertEquals(0.0, ProductoVenta("Agua", cantidad = 3, precio = null).subtotal, 0.0)
    }

    @Test
    fun `total adds every line subtotal`() {
        val lineas = listOf(
            ProductoVenta("Agua", cantidad = 2, precio = 500.0),
            ProductoVenta("Soda", cantidad = 1, precio = 350.5)
        )
        assertEquals(1350.5, totalVenta(lineas), 0.0)
        assertEquals(0.0, totalVenta(emptyList()), 0.0)
    }

    @Test
    fun `a sale is complete only with products, every quantity above 0 and every price set and not negative`() {
        assertTrue(ventaCompleta(listOf(ProductoVenta("Agua", cantidad = 1, precio = 0.0))))
        assertFalse(ventaCompleta(emptyList()))
        // A quantity left blank while typing is 0: the line stays, but the sale cannot be sent yet
        assertFalse(ventaCompleta(listOf(ProductoVenta("Agua", cantidad = 0, precio = 500.0))))
        assertFalse(ventaCompleta(listOf(ProductoVenta("Agua", cantidad = 1, precio = null))))
        assertFalse(ventaCompleta(listOf(ProductoVenta("Agua", cantidad = 1, precio = -1.0))))
    }

    @Test
    fun `products already in the sale are not offered again`() {
        val catalogo = listOf(Producto(id = 1, nombrePr = "Agua"), Producto(id = 2, nombrePr = "Soda"))
        val disponibles = productosDisponibles(catalogo, listOf(ProductoVenta("Agua")))
        assertEquals(listOf("Soda"), disponibles.map { it.nombrePr })
    }

    @Test
    fun `price text accepts comma or dot and is null when blank or invalid`() {
        assertEquals(450.5, parsearPrecio("450,5")!!, 0.0)
        assertEquals(450.5, parsearPrecio("450.5")!!, 0.0)
        assertEquals(null, parsearPrecio(""))
        assertEquals(null, parsearPrecio("abc"))
    }
}
