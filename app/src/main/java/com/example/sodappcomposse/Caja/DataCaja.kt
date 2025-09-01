package com.example.sodappcomposse.Caja

import com.example.sodappcomposse.Ventas.VentaCompleta
import com.google.gson.annotations.SerializedName

data class DataCaja(
    @SerializedName("success")
    val success: Boolean? =  null,

    @SerializedName("caja")
    val caja: List<VentaCompleta>? = null
)

data class catidadDeVentasPorProducto(
    val producto: String,
    val cantidad: Int,
    val precio: Double
)