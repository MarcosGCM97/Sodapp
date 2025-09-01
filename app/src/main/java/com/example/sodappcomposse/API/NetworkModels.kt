package com.example.sodappcomposse.API

import com.example.sodappcomposse.IngresoUsuario.Usuario
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
    val token: Usuario
)


