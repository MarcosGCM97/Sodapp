package com.example.sodappcomposse.Ventas

import com.example.sodappcomposse.Cliente.Cliente
import com.example.sodappcomposse.Producto.ProductoVenta
import com.google.gson.annotations.SerializedName

data class Venta(
    @SerializedName("vt_cli")
    val cliente: Cliente? = null,
    @SerializedName("vt_pro")
    val producto: String = "",
    @SerializedName("pr_val")
    val precio: Double = 0.0,
    @SerializedName("vt_can")
    val cantidad: Int = 0,
    @SerializedName("vt_fec")
    val fecha: String = "",
    @SerializedName("vt_ide")
    val idVenta: Int = 0,
    @SerializedName("vt_mon")
    val monto: Double = 0.0,
    @SerializedName("vt_emp")
    val empresa: String = ""
)

data class VentaRequest(
    @SerializedName("clienteId")
    val clienteId: Int,
    @SerializedName("productos")
    val productos: List<ProductoVenta>,
    @SerializedName("usuarioId")
    val usuarioId: String
)

data class VentaAgrupada(
    val cliente: Cliente? = null,
    val fecha: String,
    val productos: List<ProductoVenta>,
    val cantidadTotalVenta: Int,
    val montoTotalVenta: Double = 0.0
)

data class VentaApiResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("ventas")
    val ventas: List<Venta>
)

data class VentaIdEditar(
    val idVenta: Int
)

data class VentaApiResponseById(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("ventas")
    val ventas: List<Venta>
)