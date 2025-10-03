package com.example.sodappcomposse.Cliente

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class Cliente (
    @SerializedName("cl_ide")
    var idCl: Int = 0,

    @SerializedName("cl_nom") // Esto mapea "cl_nom" del JSON a tu propiedad "nombreCl"
    var nombreCl: String = "",

    @SerializedName("cl_tel")
    var numTelCl: String = "",

    @SerializedName("cl_dir")
    var direccionCl: String = "",

    @SerializedName("cl_deb")
    val deudaCl: Int = 0

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

data class DiasEntrega(
    var id: Int = 0,
    var dias: List<String> = emptyList()
)

data class DiasEntregaByid(
    var success: Boolean = false,
    var diasEntrega: List<String> = emptyList()
)

data class TodosLosDias(
    var success: Boolean = false,
    var clientes: List<DiasCliente> = emptyList()
)

data class DiasCliente(
    var nombre: String = "",
    var direccion: String = "",
    var diasEntrega: List<String> = emptyList()
)