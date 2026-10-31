package com.example.sodappcomposse.Producto

import androidx.annotation.StringRes
import com.example.sodappcomposse.R
import com.google.gson.annotations.SerializedName

/** One stock change of a product (historial de stock), as listed by movimientos.php. */
data class Movimiento(
    @SerializedName("ms_ide")
    val id: Int = 0,

    // venta, venta_eliminada, llenado, ajuste or alta
    @SerializedName("ms_tip")
    val tipo: String = "",

    // Signed change: negative for a sale, positive for a refill
    @SerializedName("ms_can")
    val cantidad: Int = 0,

    // Stock left by this movement
    @SerializedName("ms_stk")
    val stock: Int = 0,

    @SerializedName("ms_vta")
    val ventaId: Int? = null,

    @SerializedName("ms_fec")
    val fecha: String = ""
) {
    val cantidadConSigno: String
        get() = if (cantidad > 0) "+$cantidad" else cantidad.toString()

    @get:StringRes
    val tipoLabel: Int
        get() = when (tipo) {
            "venta" -> R.string.movimiento_venta
            "venta_eliminada" -> R.string.movimiento_venta_eliminada
            "llenado" -> R.string.movimiento_llenado
            "ajuste" -> R.string.movimiento_ajuste
            "alta" -> R.string.movimiento_alta
            else -> R.string.movimiento_otro
        }
}

data class MovimientosResponse(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("movimientos")
    val movimientos: List<Movimiento> = emptyList()
)
