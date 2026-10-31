package com.example.sodappcomposse.Caja

import com.google.gson.annotations.SerializedName

// Amount collected in a month with one payment method
data class CobroForma(
    @SerializedName("fp_nom")
    val formaPago: String,
    @SerializedName("total")
    val total: Double
)

data class CobrosMesResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("totales")
    val totales: List<CobroForma>? = emptyList()
)
