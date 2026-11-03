package com.example.sodappcomposse.Producto

data class ProductoVenta (
    val nombre: String,
    val cantidad: Int = 1,
    val precio: Double? = 0.0,
    // Containers the client returns with this item; always 0 for non-returnable products
    val devueltos: Int = 0
)

fun ProductoVenta.conDevueltos(cantidad: Int): ProductoVenta = copy(devueltos = cantidad.coerceAtLeast(0))

val ProductoVenta.subtotal: Double get() = (precio ?: 0.0) * cantidad

fun totalVenta(productos: List<ProductoVenta>): Double = productos.sumOf { it.subtotal }

// A quantity left blank while typing is 0: the line stays, but the sale cannot be sent yet
fun ventaCompleta(productos: List<ProductoVenta>): Boolean =
    productos.isNotEmpty() && productos.all { it.cantidad > 0 && it.precio != null && it.precio >= 0 }

/** Catalog products not yet in the sale, so the selector never offers one twice. */
fun productosDisponibles(catalogo: List<Producto>, elegidos: List<ProductoVenta>): List<Producto> {
    val nombres = elegidos.map { it.nombre }.toSet()
    return catalogo.filter { it.nombrePr !in nombres }
}

// The numeric keyboard may type a comma as decimal separator
fun parsearPrecio(texto: String): Double? = texto.trim().replace(',', '.').toDoubleOrNull()
