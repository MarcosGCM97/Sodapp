package com.example.sodappcomposse.Producto

import com.google.gson.annotations.SerializedName

data class Producto(
    @SerializedName("pr_ide")
    var id: Int = 0,

    @SerializedName("pr_nom")
    var nombrePr: String = "",

    @SerializedName("pr_val")
    var precioUni: Double = 0.0,

    @SerializedName("pr_stk")
    var stock: Int = 0,

    @SerializedName("pr_emp")
    var empresa: String = "",

    @SerializedName("created_at")
    var fechaCreacion: String = "",

    @SerializedName("updated_at")
    var fechaActualizacion: String = ""
)

data class ProductoRequest(
    @SerializedName("nombrePr")
    var nombrePr: String = "",

    @SerializedName("precioUni")
    var precioUni: Double = 0.0,

    @SerializedName("stock")
    var stock: Int = 0
)

data class ProductoResponse(
    @SerializedName("success")
    var success: Boolean = false,
    @SerializedName("productos")
    var productos: List<Producto> = emptyList()
)

data class ProductoResponseByName(
    @SerializedName("success")
    var success: Boolean = false,
    @SerializedName("producto")
    var producto: Producto = Producto()
)