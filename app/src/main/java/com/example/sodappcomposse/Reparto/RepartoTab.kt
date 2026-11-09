package com.example.sodappcomposse.Reparto

import com.example.sodappcomposse.auth.LocalSessionPermissions
import com.example.sodappcomposse.auth.Permiso
import com.example.sodappcomposse.auth.puedeCobrar
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.DateRange
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
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sodappcomposse.Cliente.Cliente
import com.example.sodappcomposse.Cliente.DeudaUiState
import com.example.sodappcomposse.Cliente.DeudaViewModel
import com.example.sodappcomposse.Cliente.FormularioCobro
import com.example.sodappcomposse.Cliente.montoCobrarTodo
import com.example.sodappcomposse.Componentes.SelectBuscable
import com.example.sodappcomposse.Funciones.formatearPrecio
import com.example.sodappcomposse.Funciones.formatearPrecioEditable
import com.example.sodappcomposse.Producto.Producto
import com.example.sodappcomposse.Producto.ProductoUiState
import com.example.sodappcomposse.R
import com.example.sodappcomposse.Ruta.RutaReparto
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val FORMATO_FECHA: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

/** Ventas > Reparto: the reparto of a date, stops in route order with the client address, editable while pendiente. */
@Composable
fun RepartoTab(
    // Opens the clients by delivery day (Agenda)
    onVerDias: () -> Unit,
    // Opens the plant setting (start and end of the route)
    onConfigurarPlanta: () -> Unit = {},
    viewModel: RepartoViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val fecha by viewModel.fecha
    val reparto by viewModel.reparto
    val cargando by viewModel.cargando
    val uiState by viewModel.uiState
    var paradaEditando by remember { mutableStateOf<ParadaReparto?>(null) }
    var paradaEntregando by remember { mutableStateOf<ParadaReparto?>(null) }
    // Creating, editing and delivering need repartos.gestionar; without it the tab is read-only
    val gestionar = LocalSessionPermissions.current.tiene(Permiso.REPARTOS_GESTIONAR)

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

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SelectorFecha(fecha = fecha, onCambiar = viewModel::cambiarDia, modifier = Modifier.weight(1f))
            IconButton(onClick = onVerDias) {
                Icon(Icons.Filled.DateRange, contentDescription = stringResource(R.string.dias_reparto_label))
            }
        }

        val actual = reparto
        when {
            cargando -> Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            actual == null && !gestionar -> Text(stringResource(R.string.sin_reparto))
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
                onEntregar = { paradaEntregando = it },
                onNoEntregar = viewModel::noEntregar,
                onReabrir = viewModel::reabrir,
                onAgregar = viewModel::agregarParada,
                onGuardarPlantilla = viewModel::guardarPlantilla,
                gestionar = gestionar,
                encabezado = {
                    if (actual.paradas.isNotEmpty()) {
                        RutaReparto(repartoId = actual.id, recarga = actual, onConfigurarPlanta = onConfigurarPlanta)
                    }
                }
            )
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

    paradaEntregando?.let { parada ->
        EntregaParadaDialog(
            parada = parada,
            productos = viewModel.productos.value,
            onConfirmar = { lineas ->
                viewModel.entregar(parada, lineas)
                paradaEntregando = null
            },
            onDismiss = { paradaEntregando = null }
        )
    }

    // The payment offered after a delivery needs the cobros permissions; without them it is skipped
    val puedeCobrarEntrega = puedeCobrar(LocalSessionPermissions.current)
    viewModel.cobroPendiente.value?.let { cobro ->
        if (puedeCobrarEntrega) {
            CobroEntregaDialog(cobro = cobro, onCerrar = viewModel::cerrarCobro)
        } else {
            LaunchedEffect(cobro) { viewModel.cerrarCobro() }
        }
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
private fun SelectorFecha(fecha: LocalDate, onCambiar: (Long) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(vertical = 8.dp),
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
    onEntregar: (ParadaReparto) -> Unit,
    onNoEntregar: (Int) -> Unit,
    onReabrir: (Int) -> Unit,
    onAgregar: (Int) -> Unit,
    onGuardarPlantilla: () -> Unit,
    gestionar: Boolean = true,
    encabezado: @Composable () -> Unit = {}
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { encabezado() }
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
                onEditar = { onEditar(parada) },
                onEntregar = { onEntregar(parada) },
                onNoEntregar = { onNoEntregar(parada.id) },
                onReabrir = { onReabrir(parada.id) },
                gestionar = gestionar
            )
        }
        if (!gestionar) return@LazyColumn
        item {
            val enReparto = reparto.paradas.map { it.clienteId }.toSet()
            SelectBuscable(
                opciones = clientes.filter { it.idCl !in enReparto }.sortedBy { it.nombreCl.lowercase() },
                etiqueta = { it.nombreCl },
                onSeleccionar = { onAgregar(it.idCl) },
                label = stringResource(R.string.agregar_parada),
                limpiarAlSeleccionar = true
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
    onEditar: () -> Unit,
    onEntregar: () -> Unit,
    onNoEntregar: () -> Unit,
    onReabrir: () -> Unit,
    gestionar: Boolean = true
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (gestionar) Column {
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
                        val entregada = linea.cantidadEntregada
                        Text(
                            text = if (entregada != null) {
                                stringResource(
                                    R.string.linea_entregada_format,
                                    entregada,
                                    linea.cantidadPlanificada,
                                    linea.nombreProducto,
                                    formatearPrecio(linea.precio)
                                )
                            } else {
                                stringResource(
                                    R.string.linea_reparto_format,
                                    linea.cantidadPlanificada,
                                    linea.nombreProducto,
                                    formatearPrecio(linea.precio)
                                )
                            },
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                // Only pendiente stops can be edited or removed (the backend rejects the rest)
                if (parada.esPendiente && gestionar) {
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
            if (gestionar) Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
            ) {
                when (parada.estado) {
                    ParadaReparto.ESTADO_PENDIENTE -> {
                        OutlinedButton(onClick = onNoEntregar) { Text(stringResource(R.string.no_entregada_label)) }
                        Button(onClick = onEntregar) { Text(stringResource(R.string.entregar_label)) }
                    }
                    ParadaReparto.ESTADO_NO_ENTREGADA -> {
                        OutlinedButton(onClick = onReabrir) { Text(stringResource(R.string.reabrir_label)) }
                    }
                }
            }
        }
    }
}

// A tap outside a form would drop what was typed (quantities, a payment): only the
// buttons and the system back close these dialogs
private val FORMULARIO_SIN_CIERRE_AFUERA = DialogProperties(dismissOnClickOutside = false)

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
        properties = FORMULARIO_SIN_CIERRE_AFUERA,
        title = { Text(stringResource(R.string.lineas_parada_titulo_format, parada.nombreCliente)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                productos.forEach { producto ->
                    Text(producto.nombrePr, style = MaterialTheme.typography.titleSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CampoLinea(
                            etiqueta = stringResource(R.string.cantidad_label),
                            value = cantidades[producto.id].orEmpty(),
                            onValueChange = { cantidades[producto.id] = it.filter(Char::isDigit) },
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f)
                        )
                        CampoLinea(
                            etiqueta = stringResource(R.string.precio_label),
                            value = precios[producto.id].orEmpty(),
                            onValueChange = { precios[producto.id] = it },
                            keyboardType = KeyboardType.Decimal,
                            placeholder = formatearPrecioEditable(producto.precioUni),
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

/**
 * Line field with its label always shown small above it: a floating label would fill the
 * narrow empty field (material3 1.3 has no option to keep it minimized).
 */
@Composable
private fun CampoLinea(
    etiqueta: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType,
    modifier: Modifier = Modifier,
    placeholder: String? = null
) {
    Column(modifier = modifier) {
        Text(etiqueta, style = MaterialTheme.typography.labelSmall)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Delivered quantities per product, prefilled with the plan: change them, add products the
 * client asked for (planned 0) and count the returned containers of returnable products.
 */
@Composable
private fun EntregaParadaDialog(
    parada: ParadaReparto,
    productos: List<Producto>,
    onConfirmar: (List<LineaRepartoRequest>) -> Unit,
    onDismiss: () -> Unit
) {
    val planificados = parada.lineas.associateBy { it.productoId }
    // Planned products first, in plan order
    val ordenados = productos.sortedBy { producto ->
        parada.lineas.indexOfFirst { it.productoId == producto.id }.let { if (it < 0) Int.MAX_VALUE else it }
    }
    val cantidades = remember(parada.id) {
        mutableStateMapOf<Int, String>().apply {
            parada.lineas.forEach { put(it.productoId, it.cantidadPlanificada.toString()) }
        }
    }
    val devueltos = remember(parada.id) { mutableStateMapOf<Int, String>() }
    val precios = remember(parada.id) { mutableStateMapOf<Int, String>() }
    // The price only matters (and is only shown) for products actually delivered
    val conEntrega: (Producto) -> Boolean = { (cantidades[it.id]?.toIntOrNull() ?: 0) > 0 }
    val validos = ordenados.filter(conEntrega).all { producto ->
        val precio = precios[producto.id].orEmpty()
        precio.isBlank() || (precio.toDoubleOrNull() ?: 0.0) > 0
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = FORMULARIO_SIN_CIERRE_AFUERA,
        title = { Text(stringResource(R.string.entrega_parada_titulo_format, parada.nombreCliente)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ordenados.forEach { producto ->
                    Text(producto.nombrePr, style = MaterialTheme.typography.titleSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CampoLinea(
                            etiqueta = stringResource(R.string.cantidad_label),
                            value = cantidades[producto.id].orEmpty(),
                            onValueChange = { cantidades[producto.id] = it.filter(Char::isDigit) },
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f)
                        )
                        if (producto.esRetornable) {
                            CampoLinea(
                                etiqueta = stringResource(R.string.devueltos_label),
                                value = devueltos[producto.id].orEmpty(),
                                onValueChange = { devueltos[producto.id] = it.filter(Char::isDigit) },
                                keyboardType = KeyboardType.Number,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (conEntrega(producto)) {
                            CampoLinea(
                                etiqueta = stringResource(R.string.precio_label),
                                value = precios[producto.id].orEmpty(),
                                onValueChange = { precios[producto.id] = it },
                                keyboardType = KeyboardType.Decimal,
                                placeholder = formatearPrecioEditable(planificados[producto.id]?.precio ?: producto.precioUni),
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            // Keeps the other fields the same width when the price is hidden
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = validos,
                onClick = {
                    onConfirmar(ordenados.map { producto ->
                        LineaRepartoRequest(
                            productoId = producto.id,
                            cantidad = cantidades[producto.id]?.toIntOrNull() ?: 0,
                            precio = precios[producto.id]?.toDoubleOrNull(),
                            devueltos = if (producto.esRetornable) devueltos[producto.id]?.toIntOrNull() ?: 0 else null
                        )
                    })
                }
            ) { Text(stringResource(R.string.confirmar_entrega)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancelar)) }
        }
    )
}

/**
 * Right after a delivery: the shared payment form (same as Clientes > Cobros), prefilled with
 * the delivered amount; the client's whole debt can be collected too.
 */
@Composable
private fun CobroEntregaDialog(
    cobro: CobroEntrega,
    onCerrar: () -> Unit,
    deudaModel: DeudaViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    LaunchedEffect(cobro) {
        deudaModel.cargarFormasPago()
        deudaModel.cargarDeudores()
    }
    val uiState = deudaModel.uiState.value
    LaunchedEffect(uiState) {
        when (uiState) {
            is DeudaUiState.Success -> {
                Toast.makeText(context, context.getString(R.string.pago_registrado_success), Toast.LENGTH_SHORT).show()
                deudaModel.resetState()
                onCerrar()
            }
            is DeudaUiState.Error -> {
                Toast.makeText(context, context.getString(uiState.messageRes, *uiState.args), Toast.LENGTH_SHORT).show()
                deudaModel.resetState()
            }
            else -> Unit
        }
    }
    // The debt already includes this delivery; until it loads, the delivered amount is the limit
    val saldo = deudaModel.deudores.value.find { it.id == cobro.clienteId }?.saldo ?: cobro.monto

    AlertDialog(
        onDismissRequest = onCerrar,
        properties = FORMULARIO_SIN_CIERRE_AFUERA,
        title = { Text(stringResource(R.string.cobro_entrega_titulo_format, cobro.nombreCliente)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.entregado_monto_format, formatearPrecio(cobro.monto)))
                Text(stringResource(R.string.saldo_pendiente_format, formatearPrecio(saldo)))
                FormularioCobro(
                    clienteId = cobro.clienteId,
                    saldo = saldo,
                    formas = deudaModel.formasPago.value,
                    enCurso = deudaModel.isPagoEnCurso,
                    montoInicial = montoCobrarTodo(cobro.monto),
                    onCobrar = { monto, formaPago -> deudaModel.pagarDeudaCliente(cobro.clienteId, monto, formaPago) }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onCerrar) { Text(stringResource(R.string.cerrar)) }
        }
    )
}
