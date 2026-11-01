package com.example.sodappcomposse.Reparto

import com.google.gson.annotations.SerializedName

/** Deliveries of one date (`repartos.php` GET): stops in route order, each with planned lines. */
data class Reparto(
    @SerializedName("rp_ide")
    val id: Int = 0,
    @SerializedName("rp_fec")
    val fecha: String = "",
    // ISO weekday: 1 = Lunes ... 7 = Domingo
    @SerializedName("rp_dia")
    val dia: Int = 0,
    @SerializedName("paradas")
    val paradas: List<ParadaReparto> = emptyList()
)

data class ParadaReparto(
    @SerializedName("rs_ide")
    val id: Int = 0,
    @SerializedName("rs_ord")
    val orden: Int = 0,
    @SerializedName("rs_est")
    val estado: String = ESTADO_PENDIENTE,
    @SerializedName("cl_ide")
    val clienteId: Int = 0,
    @SerializedName("cl_nom")
    val nombreCliente: String = "",
    @SerializedName("cl_dir")
    val direccion: String? = null,
    @SerializedName("cl_tel")
    val telefono: String? = null,
    @SerializedName("lineas")
    val lineas: List<LineaReparto> = emptyList()
) {
    val esPendiente: Boolean
        get() = estado == ESTADO_PENDIENTE

    companion object {
        const val ESTADO_PENDIENTE = "pendiente"
        const val ESTADO_ENTREGADA = "entregada"
        const val ESTADO_NO_ENTREGADA = "no_entregada"
    }
}

data class LineaReparto(
    @SerializedName("rl_ide")
    val id: Int = 0,
    @SerializedName("pr_ide")
    val productoId: Int = 0,
    @SerializedName("pr_nom")
    val nombreProducto: String = "",
    // TINYINT(1): 1 = returnable container
    @SerializedName("pr_ret")
    val retornable: Int = 0,
    @SerializedName("rl_cpl")
    val cantidadPlanificada: Int = 0,
    // Null until the stop is delivered
    @SerializedName("rl_cen")
    val cantidadEntregada: Int? = null,
    @SerializedName("rl_pre")
    val precio: Double = 0.0,
    @SerializedName("rl_dev")
    val devueltos: Int? = null,
    @SerializedName("rl_vt")
    val ventaId: Int? = null
)

/** Response of every `repartos.php` call; only the fields of that call are present. */
data class RepartoResponse(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("reparto")
    val reparto: Reparto? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("paradaId")
    val paradaId: Int? = null
)

/** Planned line of a stop; a null [precio] makes the backend take the product price. */
data class LineaRepartoRequest(
    @SerializedName("productoId")
    val productoId: Int,
    @SerializedName("cantidad")
    val cantidad: Int,
    @SerializedName("precio")
    val precio: Double? = null
)

/** Body of `repartos.php` POST; null fields are not sent. */
data class RepartoAccionRequest(
    @SerializedName("accion")
    val accion: String,
    @SerializedName("fecha")
    val fecha: String? = null,
    @SerializedName("origen")
    val origen: String? = null,
    @SerializedName("repartoId")
    val repartoId: Int? = null,
    @SerializedName("clienteId")
    val clienteId: Int? = null,
    @SerializedName("paradaId")
    val paradaId: Int? = null,
    @SerializedName("lineas")
    val lineas: List<LineaRepartoRequest>? = null,
    @SerializedName("paradas")
    val paradas: List<Int>? = null
) {
    companion object {
        const val CREAR = "crear"
        const val GUARDAR_PLANTILLA = "guardar_plantilla"
        const val AGREGAR_PARADA = "agregar_parada"
        const val QUITAR_PARADA = "quitar_parada"
        const val GUARDAR_LINEAS = "guardar_lineas"
        const val ORDENAR = "ordenar"
    }
}

/** Starting point of a new reparto (`crear` origen). */
enum class OrigenReparto(val valor: String) {
    VACIO("vacio"),
    DIAS("dias"),
    PLANTILLA("plantilla"),
    ANTERIOR("anterior")
}
