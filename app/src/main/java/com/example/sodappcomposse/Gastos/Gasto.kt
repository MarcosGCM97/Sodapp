package com.example.sodappcomposse.Gastos

import androidx.annotation.StringRes
import com.example.sodappcomposse.R
import com.google.gson.annotations.SerializedName

/** Expense categories of gastos.php; purchases and refills move the product stock. */
enum class CategoriaGasto(val codigo: String, @StringRes val etiqueta: Int, val mueveStock: Boolean) {
    COMPRA_PRODUCTO("compra_producto", R.string.gasto_categoria_compra_producto, true),
    RECARGA_ENVASES("recarga_envases", R.string.gasto_categoria_recarga_envases, true),
    VEHICULO("vehiculo", R.string.gasto_categoria_vehiculo, false),
    COMBUSTIBLE("combustible", R.string.gasto_categoria_combustible, false),
    OTRO("otro", R.string.gasto_categoria_otro, false);

    companion object {
        fun desdeCodigo(codigo: String?): CategoriaGasto? = entries.firstOrNull { it.codigo == codigo }
    }
}

/** State of the returnable containers bought: full ones add stock, empty ones add empties. */
enum class EstadoEnvase(val codigo: String, @StringRes val etiqueta: Int) {
    LLENO("lleno", R.string.gasto_estado_lleno),
    VACIO("vacio", R.string.gasto_estado_vacio);

    companion object {
        fun desdeCodigo(codigo: String?): EstadoEnvase? = entries.firstOrNull { it.codigo == codigo }
    }
}

/** One expense as gastos.php returns it. */
data class Gasto(
    @SerializedName("gs_ide") val id: Int = 0,
    @SerializedName("gs_cat") val categoria: String = "",
    @SerializedName("gs_mon") val monto: Double = 0.0,
    // yyyy-MM-dd
    @SerializedName("gs_fec") val fecha: String = "",
    @SerializedName("gs_des") val descripcion: String? = null,
    @SerializedName("gs_rep") val repartoId: Int? = null,
    @SerializedName("gs_pro") val productoId: Int? = null,
    @SerializedName("pr_nom") val productoNombre: String? = null,
    @SerializedName("gs_can") val cantidad: Int? = null,
    @SerializedName("gs_est") val estado: String? = null,
    // User who loaded it
    @SerializedName("gs_us") val usuarioId: Int = 0,
    @SerializedName("us_nom") val usuarioNombre: String? = null
)

/** Expenses of an inclusive period with their total. */
data class GastosPeriodo(
    val desde: String,
    val hasta: String,
    val gastos: List<Gasto>,
    val total: Double
)

/**
 * An expense to create ([id] null) or replace. [GastoRepository.guardar] only sends the
 * product fields that its [categoria] uses.
 */
data class NuevoGasto(
    val id: Int? = null,
    val categoria: CategoriaGasto,
    val monto: Double,
    val fecha: String,
    val descripcion: String? = null,
    val repartoId: Int? = null,
    val productoId: Int? = null,
    val cantidad: Int? = null,
    val estado: EstadoEnvase? = null
)

data class GastosResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("desde") val desde: String? = null,
    @SerializedName("hasta") val hasta: String? = null,
    @SerializedName("gastos") val gastos: List<Gasto>? = null,
    @SerializedName("total") val total: Double = 0.0
)

data class GastoResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("gasto") val gasto: Gasto? = null
)

// Gson leaves null fields out, so the backend only receives the fields of the category
data class GastoRequest(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("categoria") val categoria: String,
    @SerializedName("monto") val monto: Double,
    @SerializedName("fecha") val fecha: String,
    @SerializedName("descripcion") val descripcion: String? = null,
    @SerializedName("repartoId") val repartoId: Int? = null,
    @SerializedName("productoId") val productoId: Int? = null,
    @SerializedName("cantidad") val cantidad: Int? = null,
    @SerializedName("estado") val estado: String? = null
)
