package com.example.sodappcomposse.Cliente

import com.google.gson.annotations.SerializedName

data class FormaPago(
    @SerializedName("fp_ide")
    val id: Int,
    @SerializedName("fp_nom")
    val nombre: String
)

data class FormasPagoResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("formas")
    val formas: List<FormaPago> = emptyList()
)

data class Pago(
    @SerializedName("pg_ide")
    val id: Int,
    @SerializedName("pg_mon")
    val monto: Double,
    @SerializedName("fp_nom")
    val formaPago: String,
    @SerializedName("pg_fec")
    val fecha: String
)

data class PagosResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("pagos")
    val pagos: List<Pago> = emptyList()
)
