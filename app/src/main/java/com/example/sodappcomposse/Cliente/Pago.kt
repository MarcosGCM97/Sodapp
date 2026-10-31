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

// Client with a pending balance, as listed by GET deudaCliente.php
data class Deudor(
    @SerializedName("cl_ide")
    val id: Int,
    @SerializedName("cl_nom")
    val nombre: String,
    @SerializedName("cl_deb")
    val saldo: Double
)

data class DeudoresResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("deudores")
    val deudores: List<Deudor> = emptyList()
)
