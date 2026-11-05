package com.example.sodappcomposse.Cliente

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

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
    val deudaCl: Double = 0.0,

    // Locality (required by the form; null on clients not edited since it was added)
    @SerializedName("cl_loc")
    val localidadCl: String? = null,

    // Optional WGS84 coordinates; the route uses them instead of the address
    @SerializedName("cl_lat")
    val latitudCl: Double? = null,

    @SerializedName("cl_lng")
    val longitudCl: Double? = null
) {
    /**
     * PUT body for clientes.php. Nulls are written on purpose: the backend leaves a missing key
     * unchanged, so a removed location must travel as "cl_lat": null to be cleared.
     */
    fun toUpdateBody(): RequestBody =
        GSON_CON_NULOS.toJson(this).toRequestBody("application/json; charset=utf-8".toMediaType())

    private companion object {
        val GSON_CON_NULOS: Gson = GsonBuilder().serializeNulls().create()
    }
}

data class ClienteRequest(
    @SerializedName("nombreCl")
    var nombreCl: String = "",
    @SerializedName("numTelCl")
    var numTelCl: String = "",
    @SerializedName("direccionCl")
    var direccionCl: String = "",
    @SerializedName("localidadCl")
    val localidadCl: String? = null,
    @SerializedName("latitudCl")
    val latitudCl: Double? = null,
    @SerializedName("longitudCl")
    val longitudCl: Double? = null
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