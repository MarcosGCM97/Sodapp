package com.example.sodappcomposse.Empresa

import com.google.gson.annotations.SerializedName

/** Company data (empresa.php): the real business name, phone and address. */
data class Empresa(
    val nombre: String,
    val telefono: String?,
    val direccion: String?
)

data class EmpresaDto(
    @SerializedName("em_nom")
    val nombre: String?,
    @SerializedName("em_tel")
    val telefono: String?,
    @SerializedName("em_dir")
    val direccion: String?
)

/** GET and PUT empresa.php: `empresa` is null until the owner saves the company data. */
data class EmpresaResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("empresa")
    val empresa: EmpresaDto?
)

/** PUT contrasena.php body: the owner's current password and the new one, sent as typed. */
data class CambiarContrasenaRequest(
    @SerializedName("actual")
    val actual: String,
    @SerializedName("nueva")
    val nueva: String
)

/** PUT contrasena.php answer. */
data class CambiarContrasenaResponse(
    @SerializedName("success")
    val success: Boolean
)

/** PUT empresa.php body; a null telefono or direccion is stored empty. */
data class EmpresaRequest(
    @SerializedName("nombre")
    val nombre: String,
    @SerializedName("telefono")
    val telefono: String?,
    @SerializedName("direccion")
    val direccion: String?
)
