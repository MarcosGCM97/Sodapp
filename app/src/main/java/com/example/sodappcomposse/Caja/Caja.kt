package com.example.sodappcomposse.Caja

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sodappcomposse.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

private const val CLIENTES_VISIBLES = 5

@Composable
fun CajaScreen(cajaModel: CajaViewModel = hiltViewModel()) {
    val periodo by cajaModel.periodo.collectAsState()
    val uiState by cajaModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SelectorPeriodo(
                periodo = periodo,
                puedeAvanzar = cajaModel.puedeAvanzar(),
                onTipo = cajaModel::seleccionarTipo,
                onAnterior = cajaModel::anterior,
                onSiguiente = cajaModel::siguiente,
                onFecha = cajaModel::seleccionarFecha
            )
        }
        when (val estado = uiState) {
            is ReporteUiState.Loading -> item {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            }
            is ReporteUiState.Error -> item {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(estado.messageRes, *estado.args.toTypedArray()), textAlign = TextAlign.Center)
                    Button(onClick = cajaModel::reintentar) { Text(stringResource(R.string.reporte_reintentar)) }
                }
            }
            is ReporteUiState.Success -> {
                contenidoReporte(estado.reporte)
            }
        }
        item { Box(Modifier.padding(bottom = 16.dp)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectorPeriodo(
    periodo: Periodo,
    puedeAvanzar: Boolean,
    onTipo: (TipoPeriodo) -> Unit,
    onAnterior: () -> Unit,
    onSiguiente: () -> Unit,
    onFecha: (LocalDate) -> Unit
) {
    var elegirFecha by remember { mutableStateOf(false) }
    val tipos = listOf(
        TipoPeriodo.DIA to R.string.reporte_dia,
        TipoPeriodo.SEMANA to R.string.reporte_semana,
        TipoPeriodo.MES to R.string.reporte_mes
    )
    Column(Modifier.fillMaxWidth().padding(top = 8.dp)) {
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            tipos.forEachIndexed { i, (tipo, texto) ->
                SegmentedButton(
                    selected = periodo.tipo == tipo,
                    onClick = { onTipo(tipo) },
                    shape = SegmentedButtonDefaults.itemShape(i, tipos.size)
                ) { Text(stringResource(texto)) }
            }
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onAnterior) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, stringResource(R.string.reporte_periodo_anterior))
            }
            TextButton(onClick = { elegirFecha = true }, modifier = Modifier.weight(1f)) {
                Text(textoPeriodo(periodo), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            }
            IconButton(onClick = onSiguiente, enabled = puedeAvanzar) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, stringResource(R.string.reporte_periodo_siguiente))
            }
        }
    }
    if (elegirFecha) {
        // DatePicker works in UTC millis
        val state = rememberDatePickerState(
            initialSelectedDateMillis = periodo.desde.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { elegirFecha = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { onFecha(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()) }
                    elegirFecha = false
                }) { Text(stringResource(R.string.reporte_aceptar)) }
            },
            dismissButton = { TextButton(onClick = { elegirFecha = false }) { Text(stringResource(R.string.reporte_cancelar)) } }
        ) { DatePicker(state = state) }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.contenidoReporte(reporte: Reporte) {
    val r = reporte.resumen
    if (r.ventas == 0 && esCero(r.cobrado)) {
        item {
            Text(
                stringResource(R.string.reporte_sin_movimientos),
                modifier = Modifier.fillMaxWidth().padding(24.dp), textAlign = TextAlign.Center
            )
        }
        return
    }
    if (r.lineasEstimadas > 0) item { AvisoReporte(stringResource(R.string.reporte_aviso_estimadas, r.lineasEstimadas)) }
    if (reporte.cobrosParciales) {
        item { AvisoReporte(stringResource(R.string.reporte_aviso_cobros_desde, textoFecha(reporte.primerCobro!!.toLocalDate()))) }
    }
    item { Resumen(reporte) }
    item {
        SeccionReporte(stringResource(R.string.reporte_cobros_titulo)) {
            if (reporte.cobros.isEmpty()) Text(stringResource(R.string.reporte_sin_cobros))
            reporte.cobros.forEach { FilaParticipacion(it.formaPago, it.total, r.cobrado) }
        }
    }
    if (reporte.periodo.tipo != TipoPeriodo.DIA && reporte.dias.isNotEmpty()) {
        item { SeccionReporte(stringResource(R.string.reporte_por_dia_titulo)) { GraficoVentasPorDia(reporte.periodo, reporte.dias) } }
    }
    if (reporte.productos.isNotEmpty()) {
        item {
            SeccionReporte(stringResource(R.string.reporte_productos_titulo)) {
                reporte.productos.forEach {
                    FilaParticipacion(
                        it.producto ?: stringResource(R.string.reporte_producto_desconocido), it.monto, r.vendido,
                        extra = stringResource(R.string.reporte_unidades_format, it.cantidad)
                    )
                }
            }
        }
    }
    if (reporte.clientes.isNotEmpty()) item { TopClientes(reporte) }
    if (reporte.detalle.isNotEmpty()) {
        item { Text(stringResource(R.string.reporte_detalle_titulo), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
        items(reporte.ventasDetalle, key = { "${it.fecha}|${it.clienteId}" }) { FilaVentaDetalle(it) }
        if (r.lineasEstimadas > 0) item {
            Text(stringResource(R.string.reporte_nota_estimado), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun Resumen(reporte: Reporte) {
    val r = reporte.resumen
    val a = reporte.anterior
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TarjetaKpi(stringResource(R.string.reporte_vendido), precio(r.vendido), Modifier.weight(1f), textoComparacion(r.vendido, a.vendido))
            TarjetaKpi(stringResource(R.string.reporte_cobrado), precio(r.cobrado), Modifier.weight(1f), textoComparacion(r.cobrado, a.cobrado))
        }
        TarjetaKpi(
            stringResource(R.string.reporte_diferencia), precio(r.diferencia), Modifier.fillMaxWidth(),
            stringResource(R.string.reporte_diferencia_ayuda)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TarjetaKpi(stringResource(R.string.reporte_ventas), r.ventas.toString(), Modifier.weight(1f),
                textoComparacion(r.ventas.toDouble(), a.ventas.toDouble()))
            TarjetaKpi(stringResource(R.string.reporte_clientes), r.clientes.toString(), Modifier.weight(1f))
            TarjetaKpi(stringResource(R.string.reporte_ticket), reporte.ticketPromedio?.let { precio(it) } ?: "-", Modifier.weight(1f))
        }
    }
}

@Composable
private fun TopClientes(reporte: Reporte) {
    var todos by remember(reporte.periodo) { mutableStateOf(false) }
    val visibles = if (todos) reporte.clientes else reporte.clientes.take(CLIENTES_VISIBLES)
    SeccionReporte(stringResource(R.string.reporte_clientes_titulo)) {
        visibles.forEach {
            FilaParticipacion(
                it.cliente ?: stringResource(R.string.reporte_cliente_desconocido), it.monto, reporte.resumen.vendido,
                extra = stringResource(R.string.reporte_compras_format, it.ventas)
            )
        }
        if (reporte.clientes.size > CLIENTES_VISIBLES) {
            TextButton(onClick = { todos = !todos }) {
                Text(stringResource(if (todos) R.string.reporte_ver_menos else R.string.reporte_ver_todos, reporte.clientes.size))
            }
        }
    }
}

