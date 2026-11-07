package com.example.sodappcomposse.API

import com.example.sodappcomposse.IngresoUsuario.Usuario
import com.example.sodappcomposse.auth.SessionPermissions
import com.google.gson.annotations.SerializedName

data class PostResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String
)

data class UsuarioResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("usuario")
    val usuario: Usuario?,
    @SerializedName("token")
    val token: String?,
    // Absent in older backends: Gson then leaves false / null, meaning no permissions
    @SerializedName("es_dueno")
    val esDueno: Boolean = false,
    @SerializedName("permisos")
    val permisos: List<String>? = null
) {
    fun sessionPermissions() = SessionPermissions(esDueno, permisos.orEmpty().toSet())
}

/** sesion.php GET: the current user's permissions, re-read from the database. */
data class SesionResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("us_ide")
    val idUs: Int = 0,
    @SerializedName("us_nom")
    val nombreUs: String? = null,
    @SerializedName("es_dueno")
    val esDueno: Boolean = false,
    @SerializedName("permisos")
    val permisos: List<String>? = null
) {
    fun sessionPermissions() = SessionPermissions(esDueno, permisos.orEmpty().toSet())
}


