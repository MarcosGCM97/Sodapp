package com.example.sodappcomposse.Administracion

import com.google.gson.annotations.SerializedName

/** A role of the company as listed by roles.php GET. */
data class RolAdmin(
    @SerializedName("ro_ide")
    val id: Int = 0,
    @SerializedName("ro_nom")
    val nombre: String = "",
    @SerializedName("permisos")
    val permisos: List<String>? = null,
    // Number of users that have this role
    @SerializedName("usuarios")
    val usuarios: Int = 0
)

/** An entry of the permission catalog: [codigo] is sent, [descripcion] is shown. */
data class PermisoDisponible(
    @SerializedName("pm_cod")
    val codigo: String = "",
    @SerializedName("pm_des")
    val descripcion: String = ""
)

data class RolesResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("roles")
    val roles: List<RolAdmin>? = null,
    @SerializedName("permisos_disponibles")
    val permisosDisponibles: List<PermisoDisponible>? = null
)

/** Roles of the company together with the permissions a role can be given. */
data class RolesAdmin(
    val roles: List<RolAdmin>,
    val permisosDisponibles: List<PermisoDisponible>
)

/** roles.php POST (without [id]) and PUT body. */
data class RolRequest(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("nombre")
    val nombre: String,
    @SerializedName("permisos")
    val permisos: List<String>
)
