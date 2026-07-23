package com.example.sodappcomposse.Caja

import com.example.sodappcomposse.Ventas.Venta
import com.google.gson.annotations.SerializedName

data class DataCajaResponse(
    @SerializedName("success")
    val success: Boolean? =  null,

    @SerializedName("caja")
    val caja: List<Venta>? = null
)

data class CantidadDeVentasPorProducto(
    val producto: String,
    val cantidad: Int,
    val precio: Double
)