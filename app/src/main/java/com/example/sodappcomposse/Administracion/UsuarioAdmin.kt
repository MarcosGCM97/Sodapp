package com.example.sodappcomposse.Administracion

import com.google.gson.annotations.SerializedName

/** A user of the company as listed by usuarios.php GET (the owner included). */
data class UsuarioAdmin(
    @SerializedName("us_ide")
    val id: Int = 0,
    @SerializedName("us_nom")
    val nombre: String = "",
    @SerializedName("us_rol")
    val rolId: Int? = null,
    @SerializedName("ro_nom")
    val rolNombre: String? = null,
    @SerializedName("us_act")
    val activo: Int = 1,
    @SerializedName("es_dueno")
    val esDueno: Boolean = false
) {
    val estaActivo: Boolean get() = activo == 1
}

data class UsuariosResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("usuarios")
    val usuarios: List<UsuarioAdmin>? = null
)

/** usuarios.php POST body. */
data class CrearUsuarioRequest(
    @SerializedName("nombre")
    val nombre: String,
    @SerializedName("contrasena")
    val contrasena: String,
    @SerializedName("rol")
    val rol: Int
)

/** usuarios.php PUT body: only the non-null fields are sent and changed. */
data class ActualizarUsuarioRequest(
    @SerializedName("id")
    val id: Int,
    @SerializedName("rol")
    val rol: Int? = null,
    @SerializedName("contrasena")
    val contrasena: String? = null,
    @SerializedName("activo")
    val activo: Boolean? = null
)

/** Answer of the admin write endpoints; [error] carries the backend message on failure. */
data class AdminResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("error")
    val error: String? = null
)
