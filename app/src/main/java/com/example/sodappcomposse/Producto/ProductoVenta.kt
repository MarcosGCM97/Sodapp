package com.example.sodappcomposse.Producto

data class ProductoVenta (
    val nombre: String,
    val cantidad: Int = 1,
    val precio: Double? = 0.0,
    // Containers the client returns with this item; always 0 for non-returnable products
    val devueltos: Int = 0
)

fun ProductoVenta.conDevueltos(cantidad: Int): ProductoVenta = copy(devueltos = cantidad.coerceAtLeast(0))
