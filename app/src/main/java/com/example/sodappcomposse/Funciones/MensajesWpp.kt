package com.example.sodappcomposse.Funciones

import android.content.res.Resources
import com.example.sodappcomposse.R
import kotlin.math.abs

data class ProductoMensaje(val cantidad: Int, val nombre: String, val precio: Double?)

// Message sent after a sale: greeting, products, total, balance and the business signature
fun armarMensajeVenta(
    resources: Resources,
    nombreCliente: String?,
    productos: List<ProductoMensaje>,
    total: Double,
    saldo: Double?,
    nombreEmpresa: String?
): String = listOfNotNull(
    saludo(resources, nombreCliente),
    resources.getString(R.string.wpp_gracias_compra),
    productos.joinToString("\n") {
        resources.getString(R.string.wpp_producto_format, it.cantidad, it.nombre, formatearPrecio(it.precio))
    },
    resources.getString(R.string.wpp_total_format, formatearPrecio(total)),
    lineaSaldo(resources, saldo),
    resources.getString(R.string.wpp_despedida),
    firma(resources, nombreEmpresa)
).joinToString("\n\n")

// Message with the client's account balance, sent from the client screen
fun armarMensajeSaldo(
    resources: Resources,
    nombreCliente: String?,
    saldo: Double?,
    nombreEmpresa: String?
): String = listOfNotNull(
    saludo(resources, nombreCliente),
    resources.getString(R.string.wpp_estado_cuenta),
    lineaSaldo(resources, saldo),
    resources.getString(R.string.wpp_consultas),
    firma(resources, nombreEmpresa)
).joinToString("\n\n")

private fun saludo(resources: Resources, nombreCliente: String?): String =
    if (nombreCliente.isNullOrBlank()) resources.getString(R.string.wpp_saludo)
    else resources.getString(R.string.wpp_saludo_nombre, nombreCliente.trim())

private fun lineaSaldo(resources: Resources, saldo: Double?): String {
    val valor = saldo ?: 0.0
    return when {
        valor > 0 -> resources.getString(R.string.wpp_saldo_pendiente_format, formatearPrecio(valor))
        valor < 0 -> resources.getString(R.string.wpp_saldo_a_favor_format, formatearPrecio(abs(valor)))
        else -> resources.getString(R.string.wpp_al_dia)
    }
}

private fun firma(resources: Resources, nombreEmpresa: String?): String? =
    nombreEmpresa?.takeIf { it.isNotBlank() }?.let { resources.getString(R.string.wpp_firma_format, it.trim()) }
