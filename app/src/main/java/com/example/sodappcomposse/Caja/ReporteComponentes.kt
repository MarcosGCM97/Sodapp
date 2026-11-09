package com.example.sodappcomposse.Caja

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.sodappcomposse.Funciones.formatearPrecio
import com.example.sodappcomposse.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

private val LOCALE_AR = Locale("es", "AR")
private val FORMATO_DIA_CORTO = DateTimeFormatter.ofPattern("dd/MM", LOCALE_AR)
private val FORMATO_DIA = DateTimeFormatter.ofPattern("dd/MM/yyyy", LOCALE_AR)
private val FORMATO_DIA_LARGO = DateTimeFormatter.ofPattern("EEEE dd/MM/yyyy", LOCALE_AR)
private val FORMATO_MES = DateTimeFormatter.ofPattern("MMMM yyyy", LOCALE_AR)
private val FORMATO_HORA = DateTimeFormatter.ofPattern("dd/MM HH:mm", LOCALE_AR)

// "Marzo 2026", "16/03 al 22/03/2026", "Miércoles 18/03/2026"
fun textoPeriodo(periodo: Periodo): String = when (periodo.tipo) {
    TipoPeriodo.DIA -> periodo.desde.format(FORMATO_DIA_LARGO).replaceFirstChar { it.titlecase(LOCALE_AR) }
    TipoPeriodo.SEMANA -> "${periodo.desde.format(FORMATO_DIA_CORTO)} al ${periodo.hasta.format(FORMATO_DIA)}"
    TipoPeriodo.MES -> periodo.desde.format(FORMATO_MES).replaceFirstChar { it.titlecase(LOCALE_AR) }
}

fun textoFecha(fecha: LocalDate): String = fecha.format(FORMATO_DIA)

fun textoFechaHora(fechaApi: String): String =
    parseFechaHoraApi(fechaApi)?.format(FORMATO_HORA) ?: fechaApi

fun textoPorcentaje(valor: Double): String = String.format(LOCALE_AR, "%.1f%%", valor)

fun textoVariacion(valor: Double): String = (if (valor > 0) "+" else "") + textoPorcentaje(valor)

fun precio(valor: Double): String = "$" + formatearPrecio(valor)

@Composable
fun AvisoReporte(texto: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
            Spacer(Modifier.width(8.dp))
            Text(texto, color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodySmall)
        }
    }
}

// One headline figure; comparacion is the change against the previous period, when it exists
@Composable
fun TarjetaKpi(titulo: String, valor: String, modifier: Modifier = Modifier, detalle: String? = null) {
    Card(modifier = modifier) {
        Column(Modifier.padding(12.dp)) {
            Text(titulo, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(valor, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (detalle != null) {
                Text(detalle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun SeccionReporte(titulo: String, contenido: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            contenido()
        }
    }
}

// A labelled share bar: name and amount on top, share of the total below
@Composable
fun FilaParticipacion(nombre: String, monto: Double, total: Double, extra: String? = null) {
    val pct = porcentaje(monto, total)
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(nombre, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(precio(monto), fontWeight = FontWeight.SemiBold)
        }
        LinearProgressIndicator(
            progress = { ((pct ?: 0.0) / 100).toFloat().coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            drawStopIndicator = {}
        )
        Text(
            listOfNotNull(extra, pct?.let { textoPorcentaje(it) }).joinToString(" · "),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Sales per day of the period, days without sales as zero; tap a bar to read its value
@Composable
fun GraficoVentasPorDia(periodo: Periodo, dias: List<DiaReporte>) {
    val montos = remember(periodo, dias) {
        val porFecha = dias.associate { it.fecha to it.monto }
        generateSequence(periodo.desde) { it.plusDays(1) }
            .takeWhile { !it.isAfter(periodo.hasta) }
            .map { it to (porFecha[it.format(FORMATO_API)] ?: 0.0) }
            .toList()
    }
    val maximo = montos.maxOfOrNull { it.second }?.takeIf { it > 0 } ?: return
    var seleccionado by remember(periodo) { mutableStateOf(montos.indexOfFirst { it.second == maximo }) }
    val colorBarra = MaterialTheme.colorScheme.primary
    val colorEje = MaterialTheme.colorScheme.outlineVariant

    val (fecha, monto) = montos[seleccionado]
    Text(
        stringResource(R.string.reporte_grafico_seleccion, textoFecha(fecha), precio(monto)),
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold
    )
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .pointerInput(montos) {
                detectTapGestures { offset ->
                    val ancho = size.width / montos.size
                    seleccionado = (offset.x / ancho).toInt().coerceIn(0, montos.lastIndex)
                }
            }
    ) {
        val ancho = size.width / montos.size
        val gap = 2.dp.toPx()
        val radio = minOf(4.dp.toPx(), (ancho - gap) / 2)
        drawLine(colorEje, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 1.dp.toPx())
        montos.forEachIndexed { i, (_, valor) ->
            if (valor <= 0) return@forEachIndexed
            val alto = (valor / maximo * size.height).toFloat()
            val x = i * ancho + gap / 2
            val w = ancho - gap
            // Rounded data end only; flat on the baseline
            val path = Path().apply {
                addRoundRect(
                    androidx.compose.ui.geometry.RoundRect(
                        left = x, top = size.height - alto, right = x + w, bottom = size.height,
                        topLeftCornerRadius = CornerRadius(radio), topRightCornerRadius = CornerRadius(radio)
                    )
                )
            }
            drawPath(path, colorBarra, alpha = if (i == seleccionado) 1f else 0.55f)
            if (i == seleccionado) {
                drawRect(colorBarra, Offset(x, size.height - alto), Size(w, alto), style = Stroke(1.dp.toPx()))
            }
        }
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(textoFecha(periodo.desde), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(textoFecha(periodo.hasta), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun FilaVentaDetalle(venta: VentaDetalle) {
    var abierta by remember(venta.fecha, venta.clienteId) { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().clickable { abierta = !abierta }.padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    venta.cliente ?: stringResource(R.string.reporte_cliente_desconocido),
                    fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Text(textoFechaHora(venta.fecha), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(precio(venta.total), fontWeight = FontWeight.SemiBold)
            Icon(
                if (abierta) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                contentDescription = stringResource(if (abierta) R.string.reporte_ocultar else R.string.reporte_ver_productos)
            )
        }
        if (abierta) {
            venta.lineas.forEach { linea ->
                Row(Modifier.fillMaxWidth().padding(start = 12.dp, bottom = 4.dp)) {
                    Text(
                        stringResource(R.string.reporte_linea_format, linea.cantidad, linea.producto ?: "-"),
                        modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        precio(linea.monto) + if (linea.estimado) " *" else "",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        HorizontalDivider()
    }
}

// Signed change against the previous period, or nothing to compare with
@Composable
fun textoComparacion(actual: Double, anterior: Double): String =
    variacion(actual, anterior)?.let { stringResource(R.string.reporte_vs_anterior, textoVariacion(it)) }
        ?: stringResource(R.string.reporte_sin_comparacion)

fun esCero(valor: Double) = abs(valor) < 0.005
