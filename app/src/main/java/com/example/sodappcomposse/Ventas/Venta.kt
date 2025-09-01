package com.example.sodappcomposse.Ventas

import com.example.sodappcomposse.Cliente.Cliente
import com.example.sodappcomposse.Producto.Producto
import com.example.sodappcomposse.Producto.ProductoVenta
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

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

data class VentaCompleta(
    @SerializedName("cl_nom")
    val cliente: String,
    @SerializedName("cl_dir")
    val direccion: String,
    @SerializedName("cl_tel")
    val telefono: String,
    @SerializedName("pr_nom")
    val producto: String,
    @SerializedName("pr_val")
    val precio: Double,
    @SerializedName("vt_can")
    var cantidad: String,
    @SerializedName("vt_fec")
    val fecha: String,
    @SerializedName("vt_ide")
    val idVenta: String
)

data class VentaRequest(
    val clienteId: Int,
    val productos: List<ProductoVenta>
)

data class VentaAgrupada(
    val cliente: Cliente,
    val fecha: String,
    val productos: List<ProductoVenta>,
    val cantidadTotalVenta: Int,
    val montoTotalVenta: Double = 0.0
)

data class VentaApiResponse(
    val success: Boolean,
    val ventas: List<VentaCompleta> // La lista real de ventas
)

data class VentaIdEditar(
    val idVenta: Boolean
)

