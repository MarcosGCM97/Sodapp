package com.example.sodappcomposse.Envases

import com.example.sodappcomposse.Producto.Producto
import com.google.gson.annotations.SerializedName

/** Container balance of one client for one returnable product (`envases.php` GET). */
data class EnvaseCliente(
    @SerializedName("pr_ide")
    val productoId: Int = 0,
    @SerializedName("pr_nom")
    val nombreProducto: String = "",
    @SerializedName("ce_can")
    val cantidad: Int = 0
)

data class EnvasesClienteResponse(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("envases")
    val envases: List<EnvaseCliente> = emptyList()
)

/** Body of `envases.php` POST: turns [cantidad] empty containers into full stock. */
data class LlenarEnvasesRequest(
    @SerializedName("accion")
    val accion: String = ACCION_LLENAR,
    @SerializedName("producto")
    val producto: String,
    @SerializedName("cantidad")
    val cantidad: Int
) {
    companion object {
        const val ACCION_LLENAR = "llenar"
    }
}

/** Body of `envases.php` PUT: sets the client's absolute balance for a product. */
data class SaldoEnvasesRequest(
    @SerializedName("clienteId")
    val clienteId: Int,
    @SerializedName("productoId")
    val productoId: Int,
    @SerializedName("cantidad")
    val cantidad: Int
)

data class EnvaseResponse(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("producto")
    val producto: Producto? = null
)
