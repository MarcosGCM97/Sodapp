package com.example.sodappcomposse.Producto

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class Producto(
    @SerializedName("pr_nom")
    var nombrePr: String = "",

    @SerializedName("pr_val")
    var precioUni: String = "",

    @SerializedName("pr_stk")
    var stock: String = "",
)

data class ProductoRequest(
    var nombrePr: String = "",
    var precioPr: String = "",
    var cantidadPr: Int = 1,
)
