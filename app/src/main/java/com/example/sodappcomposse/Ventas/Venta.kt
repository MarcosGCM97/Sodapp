package com.example.sodappcomposse.Ventas

import com.example.sodappcomposse.Cliente.Cliente
import com.example.sodappcomposse.Producto.ProductoVenta
import com.google.gson.annotations.SerializedName

data class DataVenta(
    @SerializedName("vt_cli")
    val cliente: Cliente,
    @SerializedName("vt_pro")
    val producto: String,
    @SerializedName("vt_can")
    val cantidad: String,
    @SerializedName("vt_fec")
    val fecha: String
)

data class VentaRequest(
    val clienteId: Int,
    val productos: List<ProductoVenta> // Note: This might need to be a different structure than your UI's ProductoVenta
)

data class VentaAgrupada(
    val cliente: Cliente, // Keep the full client info
    val fecha: String,
    val productos: List<ProductoVenta>, // List of products with their total quantities for this client/date
    val cantidadTotalVenta: Int // Optional: total quantity of all items in this grouped sale
)