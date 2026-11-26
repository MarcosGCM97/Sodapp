package com.example.sodappcomposse.Funciones

import android.content.res.Resources
import com.example.sodappcomposse.Mensajes.TipoPlantilla
import com.example.sodappcomposse.R
import com.example.sodappcomposse.Ventas.Venta
import kotlin.math.abs

data class ProductoMensaje(val cantidad: Int, val nombre: String, val precio: Double?)

data class EnvaseMensaje(val cantidad: Int, val nombre: String)

/** Products and total of a client's last sale, for the sale message of the client screen. */
data class UltimaVenta(val productos: List<ProductoMensaje>, val total: Double)

// Message sent after a sale: greeting, products, total, balance and the business signature.
// A non-blank [plantilla] (the owner's template) replaces the default text.
fun armarMensajeVenta(
    resources: Resources,
    nombreCliente: String?,
    productos: List<ProductoMensaje>,
    total: Double,
    saldo: Double?,
    nombreEmpresa: String?,
    plantilla: String? = null
): String {
    val lineasProductos = productos.joinToString("\n") {
        resources.getString(R.string.wpp_producto_format, it.cantidad, it.nombre, formatearPrecio(it.precio))
    }
    if (!plantilla.isNullOrBlank()) {
        return aplicarPlantilla(
            plantilla,
            valoresComunes(nombreCliente, nombreEmpresa) + mapOf(
                "productos" to lineasProductos,
                "total" to "$" + formatearPrecio(total),
                "saldo" to lineaSaldo(resources, saldo)
            )
        )
    }
    return listOfNotNull(
        saludo(resources, nombreCliente),
        resources.getString(R.string.wpp_gracias_compra),
        lineasProductos,
        resources.getString(R.string.wpp_total_format, formatearPrecio(total)),
        lineaSaldo(resources, saldo),
        resources.getString(R.string.wpp_despedida),
        firma(resources, nombreEmpresa)
    ).joinToString("\n\n")
}

// Message with the client's account balance, sent from the client screen
fun armarMensajeSaldo(
    resources: Resources,
    nombreCliente: String?,
    saldo: Double?,
    nombreEmpresa: String?,
    plantilla: String? = null
): String {
    if (!plantilla.isNullOrBlank()) {
        return aplicarPlantilla(
            plantilla,
            valoresComunes(nombreCliente, nombreEmpresa) + ("saldo" to lineaSaldo(resources, saldo))
        )
    }
    return listOfNotNull(
        saludo(resources, nombreCliente),
        resources.getString(R.string.wpp_estado_cuenta),
        lineaSaldo(resources, saldo),
        resources.getString(R.string.wpp_consultas),
        firma(resources, nombreEmpresa)
    ).joinToString("\n\n")
}

// Message with the returnable containers the client keeps (balance above zero)
fun armarMensajeEnvases(
    resources: Resources,
    nombreCliente: String?,
    envases: List<EnvaseMensaje>,
    nombreEmpresa: String?,
    plantilla: String? = null
): String {
    val lineasEnvases = envases.filter { it.cantidad > 0 }.joinToString("\n") {
        resources.getString(R.string.wpp_envase_format, it.cantidad, it.nombre)
    }
    if (!plantilla.isNullOrBlank()) {
        return aplicarPlantilla(
            plantilla,
            valoresComunes(nombreCliente, nombreEmpresa) + ("envases" to lineasEnvases)
        )
    }
    return listOfNotNull(
        saludo(resources, nombreCliente),
        resources.getString(R.string.wpp_envases_intro),
        lineasEnvases,
        resources.getString(R.string.wpp_consultas),
        firma(resources, nombreEmpresa)
    ).joinToString("\n\n")
}

/** True when the client keeps at least one container, so a containers message makes sense. */
fun tieneEnvases(envases: List<EnvaseMensaje>): Boolean = envases.any { it.cantidad > 0 }

/**
 * Template equivalent to the default message of [tipo] (for a named client and business),
 * built from the same strings. The editor starts from it when the owner has no template.
 */
fun plantillaBase(resources: Resources, tipo: TipoPlantilla): String {
    val saludo = resources.getString(R.string.wpp_saludo_nombre, "{cliente}")
    val firma = resources.getString(R.string.wpp_firma_format, "{empresa}")
    val partes = when (tipo) {
        TipoPlantilla.DEUDA -> listOf(
            saludo,
            resources.getString(R.string.wpp_estado_cuenta),
            "{saldo}",
            resources.getString(R.string.wpp_consultas),
            firma
        )
        TipoPlantilla.ENVASES -> listOf(
            saludo,
            resources.getString(R.string.wpp_envases_intro),
            "{envases}",
            resources.getString(R.string.wpp_consultas),
            firma
        )
        TipoPlantilla.VENTA -> listOf(
            saludo,
            resources.getString(R.string.wpp_gracias_compra),
            "{productos}",
            // {total} already carries the "$", so the format keeps only its label
            resources.getString(R.string.wpp_total_format, "").removeSuffix("$") + "{total}",
            "{saldo}",
            resources.getString(R.string.wpp_despedida),
            firma
        )
    }
    return partes.joinToString("\n\n")
}

/**
 * The client's last sale: the rows sharing the latest date (one row per product, see
 * registrar_venta_lineas). Each unit price is the amount charged at the sale; a row without
 * amount falls back to the product price. Null without sales.
 */
fun ultimaVenta(ventas: List<Venta>): UltimaVenta? {
    val fecha = ventas.maxOfOrNull { it.fecha } ?: return null
    val productos = ventas.filter { it.fecha == fecha }.map {
        val precio = if (it.cantidad > 0 && it.monto > 0) it.monto / it.cantidad else it.precio
        ProductoMensaje(it.cantidad, it.producto, precio)
    }
    return UltimaVenta(productos, productos.sumOf { (it.precio ?: 0.0) * it.cantidad })
}

// A {word} is a variable; unknown words and braces around anything else stay as written
private val VARIABLE = Regex("\\{([A-Za-z_]+)\\}")

private fun aplicarPlantilla(plantilla: String, valores: Map<String, String>): String =
    VARIABLE.replace(plantilla) { valores[it.groupValues[1]] ?: it.value }

private fun valoresComunes(nombreCliente: String?, nombreEmpresa: String?): Map<String, String> =
    mapOf(
        "cliente" to nombreCliente?.trim().orEmpty(),
        "empresa" to nombreEmpresa?.trim().orEmpty()
    )

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
