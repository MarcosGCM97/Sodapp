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
