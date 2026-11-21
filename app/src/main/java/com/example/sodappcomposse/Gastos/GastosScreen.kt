package com.example.sodappcomposse.Gastos

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.sodappcomposse.Componentes.ScreenWithBackButtonWrapper
import com.example.sodappcomposse.Funciones.formatearPrecio
import com.example.sodappcomposse.R
import com.example.sodappcomposse.auth.LocalSessionPermissions
import com.example.sodappcomposse.auth.Permiso
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private val FORMATO_MES: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("es", "AR"))
private val FORMATO_DIA: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM")

/** Expenses of a month with a category filter and their total; loading and editing follow the permissions. */
@Composable
fun GastosScreen(navController: NavController, viewModel: GastosViewModel = hiltViewModel()) {
    val permisos = LocalSessionPermissions.current
    val puedeCargar = permisos.tiene(Permiso.GASTOS_CARGAR)
    val puedeEditar = permisos.tiene(Permiso.GASTOS_EDITAR)
    val puedeMoverStock = permisos.tiene(Permiso.STOCK_EDITAR)
    val mes by viewModel.mes
    val categoria by viewModel.categoria
    val periodo by viewModel.periodo
    val uiState by viewModel.uiState
    val guardado by viewModel.guardado
    // null: no form; Nuevo: create; Editar: the expense being edited
    var formulario by remember { mutableStateOf<FormularioGasto?>(null) }

    LaunchedEffect(Unit) { viewModel.cargar() }
    MensajesGastos(uiState, viewModel::resetState)
    LaunchedEffect(guardado) {
        if (guardado) {
            formulario = null
            viewModel.guardadoUsado()
        }
    }

    ScreenWithBackButtonWrapper(navController = navController, title = stringResource(R.string.gastos_titulo)) {
        Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            SelectorMes(mes = mes, onCambiar = viewModel::cambiarMes)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(selected = categoria == null, onClick = { viewModel.filtrar(null) }, label = { Text(stringResource(R.string.gastos_todas)) })
                CategoriaGasto.entries.forEach { opcion ->
                    FilterChip(
                        selected = categoria == opcion,
                        onClick = { viewModel.filtrar(opcion) },
                        label = { Text(stringResource(opcion.etiqueta)) }
                    )
                }
            }
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.gastos_total_format, formatearPrecio(periodo?.total ?: 0.0)),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (puedeCargar) {
                    Button(onClick = { formulario = FormularioGasto.Nuevo }) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Text(stringResource(R.string.gasto_registrar))
                    }
                }
            }
            val gastos = periodo?.gastos.orEmpty()
            when {
                uiState is GastosUiState.Loading && periodo == null -> Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                gastos.isEmpty() -> Text(stringResource(R.string.gastos_vacio), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(24.dp))
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(gastos, key = { it.id }) { gasto ->
                        // Editing a stock expense also moves stock back, so it needs stock.editar
                        val editable = puedeEditar && (puedeMoverStock || CategoriaGasto.desdeCodigo(gasto.categoria)?.mueveStock != true)
                        GastoCard(gasto = gasto, onClick = { formulario = FormularioGasto.Editar(gasto) }.takeIf { editable })
                    }
                }
            }
        }
    }

    formulario?.let { actual ->
        val editando = (actual as? FormularioGasto.Editar)?.gasto
        GastoDialog(
            inicial = editando,
            productos = viewModel.productos.value,
            puedeMoverStock = puedeMoverStock,
            enCurso = uiState is GastosUiState.Loading,
            onGuardar = viewModel::guardar,
            onDismiss = { formulario = null },
            onEliminar = editando?.let { g -> { viewModel.eliminar(g.id); formulario = null } }
        )
    }
}

/** Quick expense of a reparto (e.g. a breakdown on the way): the form alone, it closes when saved. */
@Composable
fun RegistrarGastoScreen(navController: NavController, repartoId: Int, viewModel: GastosViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState
    val guardado by viewModel.guardado
    LaunchedEffect(Unit) { viewModel.cargar() }
    MensajesGastos(uiState, viewModel::resetState)
    LaunchedEffect(guardado) {
        if (guardado) navController.popBackStack()
    }
    ScreenWithBackButtonWrapper(navController = navController, title = stringResource(R.string.gasto_registrar)) {
        GastoDialog(
            inicial = null,
            productos = viewModel.productos.value,
            puedeMoverStock = LocalSessionPermissions.current.tiene(Permiso.STOCK_EDITAR),
            enCurso = uiState is GastosUiState.Loading,
            onGuardar = viewModel::guardar,
            onDismiss = { navController.popBackStack() },
            repartoId = repartoId.takeIf { it > 0 }
        )
    }
}

private sealed interface FormularioGasto {
    object Nuevo : FormularioGasto
    data class Editar(val gasto: Gasto) : FormularioGasto
}

@Composable
private fun MensajesGastos(uiState: GastosUiState, onMostrado: () -> Unit) {
    val context = LocalContext.current
    LaunchedEffect(uiState) {
        when (uiState) {
            is GastosUiState.Success -> {
                Toast.makeText(context, context.getString(uiState.messageRes), Toast.LENGTH_SHORT).show()
                onMostrado()
            }
            is GastosUiState.Error -> {
                Toast.makeText(context, context.getString(uiState.messageRes, *uiState.args), Toast.LENGTH_LONG).show()
                onMostrado()
            }
            else -> {}
        }
    }
}

@Composable
private fun SelectorMes(mes: YearMonth, onCambiar: (Long) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { onCambiar(-1) }) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, stringResource(R.string.gastos_mes_anterior))
        }
        Text(
            mes.format(FORMATO_MES).replaceFirstChar { it.titlecase(Locale("es", "AR")) },
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = { onCambiar(1) }) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, stringResource(R.string.gastos_mes_siguiente))
        }
    }
}

@Composable
private fun GastoCard(gasto: Gasto, onClick: (() -> Unit)?) {
    val categoria = CategoriaGasto.desdeCodigo(gasto.categoria)
    Card(Modifier.fillMaxWidth().let { m -> onClick?.let { m.clickable(onClick = it) } ?: m }) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    categoria?.let { stringResource(it.etiqueta) } ?: gasto.categoria,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text("$" + formatearPrecio(gasto.monto), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            val fecha = runCatching { LocalDate.parse(gasto.fecha).format(FORMATO_DIA) }.getOrDefault(gasto.fecha)
            Text(fecha, style = MaterialTheme.typography.bodySmall)
            val nombre = gasto.productoNombre
            val cantidad = gasto.cantidad
            if (nombre != null && cantidad != null) {
                val estado = EstadoEnvase.desdeCodigo(gasto.estado)
                Text(
                    if (estado != null) stringResource(R.string.gasto_producto_cantidad_estado_format, nombre, cantidad, stringResource(estado.etiqueta))
                    else stringResource(R.string.gasto_producto_cantidad_format, nombre, cantidad)
                )
            }
            gasto.descripcion?.let { Text(it) }
            if (gasto.repartoId != null) Text(stringResource(R.string.gasto_del_reparto), style = MaterialTheme.typography.bodySmall)
            gasto.usuarioNombre?.let { Text(stringResource(R.string.gasto_cargado_por_format, it), style = MaterialTheme.typography.bodySmall) }
        }
    }
}
