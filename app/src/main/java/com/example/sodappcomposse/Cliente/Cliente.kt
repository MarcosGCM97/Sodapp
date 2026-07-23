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