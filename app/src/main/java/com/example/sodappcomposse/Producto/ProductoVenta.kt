package com.example.sodappcomposse.Producto

data class ProductoVenta (
    val nombre: String,
    val cantidad: Int = 1,
    val precio: Double? = 0.0
)