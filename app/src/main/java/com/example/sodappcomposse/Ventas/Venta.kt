package com.example.sodappcomposse.Ventas

import com.example.sodappcomposse.Cliente.Cliente
import com.example.sodappcomposse.Producto.Producto
import com.example.sodappcomposse.Producto.ProductoVenta
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

data class Ventas(
    val success: Boolean,
    val ventas: List<DataVenta>
)

data class DataVenta(
    @SerializedName("vt_cli")
    val cliente: Cliente,
    @SerializedName("vt_pro")
    val producto: String,
    @SerializedName("vt_can")
    val cantidad: String,
    @SerializedName("vt_fec")
    val fecha: String,
    @SerializedName("vt_emp")
    val empresa: String,
    @SerializedName("vt_mon")
    val monto: Double
)

/*cl, pr.pr_nom, pr.pr_val, vt.vt_can, vt.vt_fec, vt.vt_ide, vt.vt_mon*/
data class VentaCompleta(
    @SerializedName("vt_cli")
    val cliente: Cliente,
    @SerializedName("vt_pro")
    val producto: String = "",
    @SerializedName("pr_val")
    val precio: Double = 0.0,
    @SerializedName("vt_can")
    var cantidad: String = "",
    @SerializedName("vt_fec")
    val fecha: String = "",
    @SerializedName("vt_ide")
    val idVenta: String = "",
    @SerializedName("vt_mon")
    val monto: Double = 0.0
)

data class VentaRequest(
    val clienteId: Int,
    val productos: List<ProductoVenta>,
    val usuarioId: String
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

data class VentaApiResponseById(
    val success: Boolean,
    val ventas: List<VentaByClientId> // La lista real de ventas
)

/*cl, pr.pr_nom, pr.pr_val, vt.vt_can, vt.vt_fec, vt.vt_ide, vt.vt_mon*/
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
    var idVenta: Int = 0,

    @SerializedName("vt_mon")
    var monto: Double = 0.0
)