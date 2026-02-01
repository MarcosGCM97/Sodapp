package com.example.sodappcomposse.Producto

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

data class Producto(
    var nombrePr: String = "",

    var precioUni: String = "",

    var stock: String = "",
)

data class ProductoRequest(
    var nombrePr: String = "",

    var precioUni: String = "",

    var stock: String = "",
)

@Serializable
data class ProductoCompleto(
    @SerializedName("pr_ide")
    var id: Int = 0,

    @SerializedName("pr_nom")
    var nombrePr: String = "",

    @SerializedName("pr_val")
    var precioUni: String = "",

    @SerializedName("pr_stk")
    var stock: String = "",

    @SerializedName("pr_emp")
    var empresa: String = "",

    @SerializedName("created_at")
    var fechaCreacion: String = "",

    @SerializedName("updated_at")
    var fechaActualizacion: String = ""
)

data class ProductoResponse(
    var success: Boolean = false,
    var productos: List<ProductoCompleto> = emptyList()
)

data class ProductoResponseByName(
    var success: Boolean = false,
    var producto: ProductoCompleto = ProductoCompleto()
)