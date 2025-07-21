package com.example.sodappcomposse.Cliente

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.google.gson.annotations.SerializedName

data class Cliente (
    @SerializedName("cl_ide")
    var idCl: Int = 0,

    @SerializedName("cl_nom") // Esto mapea "cl_nom" del JSON a tu propiedad "nombreCl"
    var nombreCl: String = "",

    @SerializedName("cl_tel")
    var numTelCl: String = "",

    @SerializedName("cl_dir")
    var direccionCl: String = "",

    // Si tienes "cl_ide" en el JSON y lo necesitas, añádelo:
    // @SerializedName("cl_ide")
    // var idCl: String? = null, // Puede ser nullable si el JSON lo permite

    //var productos: MutableList<String> = mutableListOf()
)

data class ClienteRequest(
    var nombreCl: String = "",
    var numTelCl: String = "",
    var direccionCl: String = ""
)