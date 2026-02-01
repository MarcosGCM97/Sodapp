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

/*cl.cl_nom, cl.cl_dir, cl.cl_tel, pr.pr_nom, pr.pr_val, vt.vt_can, vt.vt_fec, vt.vt_ide*/
data class VentaCompleta(
    @SerializedName("cl_nom")
    val cliente: String = "",
    @SerializedName("cl_dir")
    val direccion: String = "",
    @SerializedName("cl_tel")
    val telefono: String = "",
    @SerializedName("pr_nom")
    val producto: String = "",
    @SerializedName("pr_val")
    val precio: Double = 0.0,
    @SerializedName("vt_can")
    var cantidad: String = "",
    @SerializedName("vt_fec")
    val fecha: String = "",
    @SerializedName("vt_ide")
    val idVenta: String = ""
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

data class VentaResponse(
    val success: Boolean,
    val ventas: List<DataVenta> // La lista real de ventas
)

data class VentaResponseByClientId(
    val success: Boolean,
    val ventas: List<VentaByClientId> // La lista real de ventas
)

//cl.cl_nom, cl.cl_dir, cl.cl_tel, pr.pr_nom, pr.pr_val, vt.vt_can, vt.vt_fec, vt.vt_ide
@Serializable
data class VentaByClientId(
    @SerializedName("vt_cli")
    val cliente: Cliente,

    @SerializedName("pr_nom")
    var producto: String = "",

    @SerializedName("pr_val")
    var precio: Double = 0.0,

    @SerializedName("vt_can")
    var cantidad: Int = 0,

    @SerializedName("vt_fec")
    var fecha: String = "",

    @SerializedName("vt_ide")
    var idVenta: Int = 0
)