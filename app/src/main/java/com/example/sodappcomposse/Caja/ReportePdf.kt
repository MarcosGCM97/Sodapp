package com.example.sodappcomposse.Caja

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.TextUtils
import android.text.TextPaint
import androidx.annotation.StringRes
import androidx.core.content.FileProvider
import com.example.sodappcomposse.R
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// ---------------------------------------------------------------- content (pure, unit tested)

sealed class BloquePdf {
    data class Titulo(val texto: String) : BloquePdf()
    data class Subtitulo(val texto: String) : BloquePdf()
    data class Texto(val texto: String) : BloquePdf()
    data class Fila(val izquierda: String, val derecha: String, val negrita: Boolean = false, val sangria: Boolean = false) : BloquePdf()
    object Separador : BloquePdf()
}

typealias Textos = (Int, Array<out Any>) -> String

private val FORMATO_GENERADO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

// Everything the screen shows for the period, in print order; the per-day chart becomes a table
fun contenidoPdf(reporte: Reporte, empresa: String?, generado: LocalDateTime, textos: Textos): List<BloquePdf> {
    fun t(@StringRes id: Int, vararg args: Any) = textos(id, args)
    val r = reporte.resumen
    val a = reporte.anterior
    fun comparacion(actual: Double, anterior: Double) =
        variacion(actual, anterior)?.let { t(R.string.reporte_vs_anterior, textoVariacion(it)) } ?: t(R.string.reporte_sin_comparacion)

    val bloques = mutableListOf<BloquePdf>()
    bloques += BloquePdf.Titulo(t(R.string.pdf_titulo))
    if (!empresa.isNullOrBlank()) bloques += BloquePdf.Texto(empresa)
    bloques += BloquePdf.Texto(t(R.string.pdf_periodo, textoPeriodo(reporte.periodo), textoFecha(reporte.periodo.desde), textoFecha(reporte.periodo.hasta)))
    bloques += BloquePdf.Texto(t(R.string.pdf_generado, generado.format(FORMATO_GENERADO)))
    if (r.lineasEstimadas > 0) bloques += BloquePdf.Texto(t(R.string.reporte_aviso_estimadas, r.lineasEstimadas))
    if (reporte.cobrosParciales) bloques += BloquePdf.Texto(t(R.string.reporte_aviso_cobros_desde, textoFecha(reporte.primerCobro!!.toLocalDate())))
    bloques += BloquePdf.Separador

    bloques += BloquePdf.Subtitulo(t(R.string.pdf_resumen))
    bloques += BloquePdf.Fila(t(R.string.reporte_vendido), "${precio(r.vendido)}  (${comparacion(r.vendido, a.vendido)})", negrita = true)
    bloques += BloquePdf.Fila(t(R.string.reporte_cobrado), "${precio(r.cobrado)}  (${if (reporte.comparacionCobradoIncompleta) t(R.string.reporte_comparacion_incompleta) else comparacion(r.cobrado, a.cobrado)})", negrita = true)
    bloques += BloquePdf.Fila(t(R.string.reporte_diferencia), precio(r.diferencia), negrita = true)
    bloques += BloquePdf.Fila(t(R.string.reporte_ventas), "${r.ventas}  (${comparacion(r.ventas.toDouble(), a.ventas.toDouble())})")
    bloques += BloquePdf.Fila(t(R.string.reporte_clientes), r.clientes.toString())
    bloques += BloquePdf.Fila(t(R.string.reporte_ticket), reporte.ticketPromedio?.let { precio(it) } ?: "-")
    bloques += BloquePdf.Fila(t(R.string.pdf_periodo_anterior, textoFecha(fechaDeApi(a.desde)), textoFecha(fechaDeApi(a.hasta))),
        t(R.string.pdf_anterior_valores, precio(a.vendido), precio(a.cobrado), a.ventas))
    bloques += BloquePdf.Texto(t(R.string.reporte_diferencia_ayuda))

    bloques += BloquePdf.Subtitulo(t(R.string.reporte_cobros_titulo))
    if (reporte.cobros.isEmpty()) bloques += BloquePdf.Texto(t(R.string.reporte_sin_cobros))
    reporte.cobros.forEach { bloques += BloquePdf.Fila(it.formaPago, conPorcentaje(it.total, r.cobrado)) }

    if (reporte.productos.isNotEmpty()) {
        bloques += BloquePdf.Subtitulo(t(R.string.reporte_productos_titulo))
        reporte.productos.forEach {
            bloques += BloquePdf.Fila(
                "${it.producto ?: t(R.string.reporte_producto_desconocido)} · ${t(R.string.reporte_unidades_format, it.cantidad)}",
                conPorcentaje(it.monto, r.vendido)
            )
        }
    }
    if (reporte.clientes.isNotEmpty()) {
        bloques += BloquePdf.Subtitulo(t(R.string.pdf_clientes_titulo))
        reporte.clientes.forEach {
            bloques += BloquePdf.Fila(
                "${it.cliente ?: t(R.string.reporte_cliente_desconocido)} · ${t(R.string.reporte_compras_format, it.ventas)}",
                conPorcentaje(it.monto, r.vendido)
            )
        }
    }
    if (reporte.periodo.tipo != TipoPeriodo.DIA && reporte.dias.isNotEmpty()) {
        bloques += BloquePdf.Subtitulo(t(R.string.reporte_por_dia_titulo))
        reporte.dias.forEach { bloques += BloquePdf.Fila(textoFecha(fechaDeApi(it.fecha)), precio(it.monto)) }
    }
    if (reporte.detalle.isNotEmpty()) {
        bloques += BloquePdf.Subtitulo(t(R.string.reporte_detalle_titulo))
        reporte.ventasDetalle.forEach { venta ->
            bloques += BloquePdf.Fila(
                "${textoFechaHora(venta.fecha)} · ${venta.cliente ?: t(R.string.reporte_cliente_desconocido)}",
                precio(venta.total), negrita = true
            )
            venta.lineas.forEach { l ->
                bloques += BloquePdf.Fila(
                    t(R.string.reporte_linea_format, l.cantidad, l.producto ?: "-"),
                    precio(l.monto) + if (l.estimado) " *" else "", sangria = true
                )
            }
        }
        if (r.lineasEstimadas > 0) bloques += BloquePdf.Texto(t(R.string.reporte_nota_estimado))
    }
    return bloques
}

private fun conPorcentaje(monto: Double, total: Double): String =
    precio(monto) + (porcentaje(monto, total)?.let { "  (${textoPorcentaje(it)})" } ?: "")

private fun fechaDeApi(fechaApi: String) = java.time.LocalDate.parse(fechaApi.take(10))

fun nombreArchivoPdf(periodo: Periodo): String = "reporte_${periodo.desde}_${periodo.hasta}.pdf"

// ---------------------------------------------------------------- rendering and sharing

private const val ANCHO = 595 // A4 in points
private const val ALTO = 842
private const val MARGEN = 40f

// Writes the report to the app cache and returns the file
fun escribirPdf(context: Context, nombre: String, bloques: List<BloquePdf>): File {
    val documento = PdfDocument()
    val normal = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 10f }
    val negrita = TextPaint(normal).apply { typeface = Typeface.DEFAULT_BOLD }
    val subtitulo = TextPaint(negrita).apply { textSize = 13f }
    val titulo = TextPaint(negrita).apply { textSize = 18f }
    val gris = TextPaint(normal).apply { color = 0xFF666666.toInt(); textSize = 8f }
    val ancho = ANCHO - 2 * MARGEN

    var numero = 0
    var pagina: PdfDocument.Page? = null
    var y = 0f

    fun nuevaPagina() {
        pagina?.let { documento.finishPage(it) }
        numero++
        pagina = documento.startPage(PdfDocument.PageInfo.Builder(ANCHO, ALTO, numero).create())
        pagina!!.canvas.drawText(context.getString(R.string.pdf_pagina, numero), MARGEN, ALTO - 20f, gris)
        y = MARGEN
    }

    fun asegurar(alto: Float) {
        if (pagina == null || y + alto > ALTO - MARGEN) nuevaPagina()
    }

    // Long text wraps into as many lines as it needs
    fun parrafo(texto: String, paint: TextPaint, interlineado: Float) {
        var resto = texto
        while (resto.isNotEmpty()) {
            val n = paint.breakText(resto, true, ancho, null).coerceAtLeast(1)
            val corte = if (n < resto.length) resto.lastIndexOf(' ', n).takeIf { it > 0 } ?: n else n
            asegurar(interlineado)
            pagina!!.canvas.drawText(resto.substring(0, corte).trim(), MARGEN, y + paint.textSize, paint)
            y += interlineado
            resto = resto.substring(corte).trimStart()
        }
    }

    bloques.forEach { b ->
        when (b) {
            is BloquePdf.Titulo -> parrafo(b.texto, titulo, 26f)
            is BloquePdf.Subtitulo -> { y += 8f; asegurar(40f); parrafo(b.texto, subtitulo, 20f) }
            is BloquePdf.Texto -> parrafo(b.texto, normal, 14f)
            is BloquePdf.Separador -> {
                asegurar(12f)
                pagina!!.canvas.drawLine(MARGEN, y + 6f, ANCHO - MARGEN, y + 6f, gris)
                y += 12f
            }
            is BloquePdf.Fila -> {
                asegurar(15f)
                val paint = if (b.negrita) negrita else normal
                val x = if (b.sangria) MARGEN + 16f else MARGEN
                val anchoDerecha = paint.measureText(b.derecha)
                val izquierda = TextUtils.ellipsize(b.izquierda, paint, ANCHO - MARGEN - anchoDerecha - 12f - x, TextUtils.TruncateAt.END)
                pagina!!.canvas.drawText(izquierda.toString(), x, y + paint.textSize, paint)
                pagina!!.canvas.drawText(b.derecha, ANCHO - MARGEN - anchoDerecha, y + paint.textSize, paint)
                y += 15f
            }
        }
    }
    if (pagina == null) nuevaPagina()
    documento.finishPage(pagina)

    val carpeta = File(context.cacheDir, "reportes").apply { mkdirs() }
    val archivo = File(carpeta, nombre)
    try {
        archivo.outputStream().use { documento.writeTo(it) }
    } finally {
        documento.close()
    }
    return archivo
}

fun compartirPdf(context: Context, archivo: File) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, context.getString(R.string.pdf_compartir)))
}
