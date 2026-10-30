package com.example.sodappcomposse.Caja

import androidx.annotation.StringRes
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

sealed class CobrosResult {
    data class Success(val totales: List<CobroForma>) : CobrosResult()
    data class Error(@StringRes val messageRes: Int, val args: List<Any> = emptyList()) : CobrosResult()
}

sealed class CobrosUiState {
    object Idle : CobrosUiState()
    object Loading : CobrosUiState()
    data class Success(val totales: List<CobroForma>) : CobrosUiState() {
        val total: Double get() = totales.sumOf { it.total }
    }
    data class Error(@StringRes val messageRes: Int, val args: List<Any> = emptyList()) : CobrosUiState()
}
