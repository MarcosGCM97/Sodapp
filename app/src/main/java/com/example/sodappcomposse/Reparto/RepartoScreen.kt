package com.example.sodappcomposse.Reparto

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.sodappcomposse.Cliente.Cliente
import com.example.sodappcomposse.Componentes.ScreenWithBackButtonWrapper
import com.example.sodappcomposse.Funciones.formatearPrecio
import com.example.sodappcomposse.Producto.Producto
import com.example.sodappcomposse.Producto.ProductoUiState
import com.example.sodappcomposse.R
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val FORMATO_FECHA: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

/** Builds the reparto of a date: stops in route order with the client address, editable while pendiente. */
@Composable
fun RepartoScreen(
    navController: NavController,
    viewModel: RepartoViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val fecha by viewModel.fecha
    val reparto by viewModel.reparto
    val cargando by viewModel.cargando
    val uiState by viewModel.uiState
    var paradaEditando by remember { mutableStateOf<ParadaReparto?>(null) }

    LaunchedEffect(Unit) {
        viewModel.cargar()
        viewModel.cargarCatalogos()
    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is ProductoUiState.Success -> {
                Toast.makeText(context, context.getString(state.messageRes, *state.args), Toast.LENGTH_SHORT).show()
                viewModel.resetState()
            }
            is ProductoUiState.Error -> {
                Toast.makeText(context, context.getString(state.messageRes, *state.args), Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    ScreenWithBackButtonWrapper(navController, stringResource(R.string.repartos_titulo)) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            SelectorFecha(fecha = fecha, onCambiar = viewModel::cambiarDia)

            val actual = reparto
            when {
                cargando -> Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                actual == null -> CrearReparto(
                    enCurso = uiState is ProductoUiState.Loading,
                    onCrear = viewModel::crear
                )
                else -> ParadasReparto(
                    reparto = actual,
                    nombreDia = nombreDia(fecha.dayOfWeek),
                    clientes = viewModel.clientes.value,
                    onMover = viewModel::mover,
                    onQuitar = viewModel::quitarParada,
                    onEditar = { paradaEditando = it },
                    onAgregar = viewModel::agregarParada,
                    onGuardarPlantilla = viewModel::guardarPlantilla
                )
            }
        }
    }

    paradaEditando?.let { parada ->
        LineasParadaDialog(
            parada = parada,
            productos = viewModel.productos.value,
            onGuardar = { lineas ->
                viewModel.guardarLineas(parada.id, lineas)
                paradaEditando = null
            },
            onDismiss = { paradaEditando = null }
        )
    }
}

@Composable
private fun nombreDia(dia: DayOfWeek): String = stringResource(
    when (dia) {
        DayOfWeek.MONDAY -> R.string.lunes
        DayOfWeek.TUESDAY -> R.string.martes
        DayOfWeek.WEDNESDAY -> R.string.miercoles
        DayOfWeek.THURSDAY -> R.string.jueves
        DayOfWeek.FRIDAY -> R.string.viernes
        DayOfWeek.SATURDAY -> R.string.sabado
        DayOfWeek.SUNDAY -> R.string.domingo
    }
)

@Composable
private fun SelectorFecha(fecha: LocalDate, onCambiar: (Long) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = { onCambiar(-1) }) {
            Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = stringResource(R.string.dia_anterior_desc))
        }
        Text(
            text = stringResource(R.string.reparto_fecha_format, nombreDia(fecha.dayOfWeek), fecha.format(FORMATO_FECHA)),
            style = MaterialTheme.typography.titleMedium
        )
        IconButton(onClick = { onCambiar(1) }) {
            Icon(Icons.Filled.KeyboardArrowRight, contentDescription = stringResource(R.string.dia_siguiente_desc))
        }
    }
}

@Composable
private fun CrearReparto(enCurso: Boolean, onCrear: (OrigenReparto) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.sin_reparto))
        listOf(
            OrigenReparto.PLANTILLA to R.string.origen_plantilla,
            OrigenReparto.ANTERIOR to R.string.origen_anterior,
            OrigenReparto.DIAS to R.string.origen_dias,
            OrigenReparto.VACIO to R.string.origen_vacio
        ).forEach { (origen, texto) ->
            OutlinedButton(onClick = { onCrear(origen) }, enabled = !enCurso, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(texto))
            }
        }
    }
}

@Composable
private fun ParadasReparto(
    reparto: Reparto,
    nombreDia: String,
    clientes: List<Cliente>,
    onMover: (Int, Int) -> Unit,
    onQuitar: (Int) -> Unit,
    onEditar: (ParadaReparto) -> Unit,
    onAgregar: (Int) -> Unit,
    onGuardarPlantilla: () -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (reparto.paradas.isEmpty()) {
            item { Text(stringResource(R.string.reparto_sin_paradas)) }
        }
        items(reparto.paradas, key = { it.id }) { parada ->
            val posicion = reparto.paradas.indexOf(parada)
            ParadaCard(
                parada = parada,
                posicion = posicion + 1,
                puedeSubir = posicion > 0,
                puedeBajar = posicion < reparto.paradas.lastIndex,
                onMover = { delta -> onMover(parada.id, delta) },
                onQuitar = { onQuitar(parada.id) },
                onEditar = { onEditar(parada) }
            )
        }
        item {
            val enReparto = reparto.paradas.map { it.clienteId }.toSet()
            AgregarParada(
                clientes = clientes.filter { it.idCl !in enReparto }.sortedBy { it.nombreCl.lowercase() },
                onAgregar = onAgregar
            )
        }
        item {
            Button(onClick = onGuardarPlantilla, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                Text(stringResource(R.string.guardar_plantilla_format, nombreDia))
            }
        }
    }
}

@Composable
private fun ParadaCard(
    parada: ParadaReparto,
    posicion: Int,
    puedeSubir: Boolean,
    puedeBajar: Boolean,
    onMover: (Int) -> Unit,
    onQuitar: () -> Unit,
    onEditar: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column {
                IconButton(onClick = { onMover(-1) }, enabled = puedeSubir) {
                    Icon(Icons.Filled.KeyboardArrowUp, contentDescription = stringResource(R.string.subir_desc))
                }
                IconButton(onClick = { onMover(1) }, enabled = puedeBajar) {
                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = stringResource(R.string.bajar_desc))
                }
            }
            Column(modifier = Modifier.weight(1f).padding(horizontal = 4.dp)) {
                Text(
                    text = stringResource(R.string.parada_titulo_format, posicion, parada.nombreCliente),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = parada.direccion?.takeIf { it.isNotBlank() } ?: stringResource(R.string.sin_direccion),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = stringResource(
                        when (parada.estado) {
                            ParadaReparto.ESTADO_ENTREGADA -> R.string.estado_entregada
                            ParadaReparto.ESTADO_NO_ENTREGADA -> R.string.estado_no_entregada
                            else -> R.string.estado_pendiente
                        }
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                if (parada.lineas.isEmpty()) {
                    Text(stringResource(R.string.sin_productos), style = MaterialTheme.typography.bodySmall)
                }
                parada.lineas.forEach { linea ->
                    Text(
                        text = stringResource(
                            R.string.linea_reparto_format,
                            linea.cantidadPlanificada,
                            linea.nombreProducto,
                            formatearPrecio(linea.precio)
                        ),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            // Only pendiente stops can be edited or removed (the backend rejects the rest)
            if (parada.esPendiente) {
                Column {
                    IconButton(onClick = onEditar) {
                        Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.productos_parada_desc))
                    }
                    IconButton(onClick = onQuitar) {
                        Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.eliminar_desc))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AgregarParada(clientes: List<Cliente>, onAgregar: (Int) -> Unit) {
    var expandido by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expandido,
        onExpandedChange = { expandido = !expandido },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = stringResource(R.string.agregar_parada),
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
            clientes.forEach { cliente ->
                DropdownMenuItem(
                    text = { Text(cliente.nombreCl) },
                    onClick = {
                        expandido = false
                        onAgregar(cliente.idCl)
                    }
                )
            }
        }
    }
}

/** One row per product: planned quantity and price (blank price = current product price). */
@Composable
private fun LineasParadaDialog(
    parada: ParadaReparto,
    productos: List<Producto>,
    onGuardar: (List<LineaRepartoRequest>) -> Unit,
    onDismiss: () -> Unit
) {
    val cantidades = remember(parada.id) {
        mutableStateMapOf<Int, String>().apply {
            parada.lineas.forEach { put(it.productoId, it.cantidadPlanificada.toString()) }
        }
    }
    val precios = remember(parada.id) {
        mutableStateMapOf<Int, String>().apply {
            parada.lineas.forEach { put(it.productoId, formatearPrecioEditable(it.precio)) }
        }
    }
    val validos = productos.all { producto ->
        val cantidad = cantidades[producto.id].orEmpty()
        val precio = precios[producto.id].orEmpty()
        (cantidad.isBlank() || cantidad.toIntOrNull() != null) &&
            (precio.isBlank() || (precio.toDoubleOrNull() ?: 0.0) > 0)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.lineas_parada_titulo_format, parada.nombreCliente)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                productos.forEach { producto ->
                    Text(producto.nombrePr, style = MaterialTheme.typography.titleSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = cantidades[producto.id].orEmpty(),
                            onValueChange = { cantidades[producto.id] = it.filter(Char::isDigit) },
                            label = { Text(stringResource(R.string.cantidad_label)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = precios[producto.id].orEmpty(),
                            onValueChange = { precios[producto.id] = it },
                            label = { Text(stringResource(R.string.precio_label)) },
                            placeholder = { Text(formatearPrecioEditable(producto.precioUni)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = validos,
                onClick = {
                    onGuardar(productos.map { producto ->
                        LineaRepartoRequest(
                            productoId = producto.id,
                            cantidad = cantidades[producto.id]?.toIntOrNull() ?: 0,
                            precio = precios[producto.id]?.toDoubleOrNull()
                        )
                    })
                }
            ) { Text(stringResource(R.string.guardar_label)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancelar)) }
        }
    )
}

// "450" instead of "450.0" so the field shows what the user would type
private fun formatearPrecioEditable(precio: Double): String =
    if (precio % 1.0 == 0.0) precio.toLong().toString() else precio.toString()
