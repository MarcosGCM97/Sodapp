package com.example.sodappcomposse.Ruta

import com.google.gson.annotations.SerializedName

/** Plant of the user (`ruta.php`): every route starts and ends there. */
data class Planta(
    @SerializedName("direccion")
    val direccion: String = "",
    @SerializedName("localidad")
    val localidad: String = "",
    @SerializedName("lat")
    val lat: Double? = null,
    @SerializedName("lng")
    val lng: Double? = null
)

/** One stop of the route, in reparto order. */
data class RutaParada(
    @SerializedName("paradaId")
    val paradaId: Int = 0,
    @SerializedName("orden")
    val orden: Int = 0,
    @SerializedName("estado")
    val estado: String = "",
    @SerializedName("clienteId")
    val clienteId: Int = 0,
    @SerializedName("nombre")
    val nombre: String = "",
    @SerializedName("direccion")
    val direccion: String? = null,
    @SerializedName("localidad")
    val localidad: String? = null,
    @SerializedName("lat")
    val lat: Double? = null,
    @SerializedName("lng")
    val lng: Double? = null
)

/** Route of a reparto (`ruta.php?repartoId=N`); inicio / fin are "yyyy-MM-dd HH:mm:ss" or null. */
data class Ruta(
    @SerializedName("repartoId")
    val repartoId: Int = 0,
    @SerializedName("fecha")
    val fecha: String = "",
    @SerializedName("inicio")
    val inicio: String? = null,
    @SerializedName("fin")
    val fin: String? = null,
    @SerializedName("planta")
    val planta: Planta? = null,
    @SerializedName("paradas")
    val paradas: List<RutaParada> = emptyList()
)

/** Response of every `ruta.php` call; only the fields of that call are present. */
data class RutaResponse(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("ruta")
    val ruta: Ruta? = null,
    @SerializedName("planta")
    val planta: Planta? = null,
    @SerializedName("inicio")
    val inicio: String? = null,
    @SerializedName("fin")
    val fin: String? = null,
    @SerializedName("error")
    val error: String? = null
)

/** POST body of `ruta.php`; null fields are left out of the JSON. */
data class RutaAccionRequest(
    @SerializedName("accion")
    val accion: String,
    @SerializedName("repartoId")
    val repartoId: Int? = null,
    @SerializedName("direccion")
    val direccion: String? = null,
    @SerializedName("localidad")
    val localidad: String? = null,
    @SerializedName("lat")
    val lat: Double? = null,
    @SerializedName("lng")
    val lng: Double? = null
) {
    companion object {
        const val GUARDAR_PLANTA = "guardar_planta"
        const val INICIAR = "iniciar"
        const val FINALIZAR = "finalizar"
    }
}
