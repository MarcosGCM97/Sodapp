package com.example.sodappcomposse.Cliente

import com.google.gson.annotations.SerializedName

data class Cliente(
    @SerializedName("cl_ide")
    var idCl: Int = 0,

    @SerializedName("cl_nom")
    var nombreCl: String = "",

    @SerializedName("cl_tel")
    var numTelCl: String = "",

    @SerializedName("cl_dir")
    var direccionCl: String = "",

    @SerializedName("cl_deb")
    val deudaCl: Double = 0.0
)

data class ClienteRequest(
    @SerializedName("nombreCl")
    var nombreCl: String = "",
    @SerializedName("numTelCl")
    var numTelCl: String = "",
    @SerializedName("direccionCl")
    var direccionCl: String = ""
)

data class ClienteResponse(
    @SerializedName("success")
    var success: Boolean = false,
    @SerializedName("clientes")
    var clientes: List<Cliente> = emptyList()
)

data class ClienteResponseById(
    @SerializedName("success")
    var success: Boolean = false,
    @SerializedName("cliente")
    var cliente: Cliente = Cliente()
)

data class DiasEntrega(
    @SerializedName("id")
    var id: Int = 0,
    @SerializedName("dias")
    var dias: List<String> = emptyList()
)

data class DiasEntregaUpdateRequest(
    @SerializedName("cl_ide") val cl_ide: Int,
    @SerializedName("cl_lun") val cl_lun: Int,
    @SerializedName("cl_mar") val cl_mar: Int,
    @SerializedName("cl_mie") val cl_mie: Int,
    @SerializedName("cl_jue") val cl_jue: Int,
    @SerializedName("cl_vie") val cl_vie: Int,
    @SerializedName("cl_sab") val cl_sab: Int,
    @SerializedName("cl_dom") val cl_dom: Int
)

data class DiasEntregaByid(
    @SerializedName("success")
    var success: Boolean = false,
    @SerializedName("diasEntrega")
    var diasEntrega: List<String> = emptyList()
)

data class TodosLosDias(
    @SerializedName("success")
    var success: Boolean = false,
    @SerializedName("days")
    var clientes: List<DiasCliente> = emptyList()
)

data class DiasCliente(
    @SerializedName("nombre")
    var nombre: String = "",
    @SerializedName("direccion")
    var direccion: String = "",
    @SerializedName("diasEntrega")
    var diasEntrega: List<String> = emptyList()
)