package com.example.sodappcomposse

import com.google.gson.annotations.SerializedName

data class Usuario (
    @SerializedName("us_ide")
    var idUs: Int = 0,
    @SerializedName("us_nom")
    var nombreUs: String = "",
    //@SerializedName("us_ape")
    //var apellidoUs: String = "",
    //@SerializedName("us_ema")
    //var emailUs: String = "",
    @SerializedName("us_pas")
    var contrasenaUs: String = ""
)

data class UsuarioRequest(
    var nombreUs: String = "",
    var contrasenaUs: String = ""
)