package com.example.sodappcomposse.Ventas

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import android.widget.Toast
import kotlinx.coroutines.launch
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.sodappcomposse.Agenda
import com.example.sodappcomposse.PlantaConfig
import com.example.sodappcomposse.Cliente.ClienteUiState
import com.example.sodappcomposse.Cliente.ClientesViewModel
import com.example.sodappcomposse.Cliente.ClientesDropDown
import com.example.sodappcomposse.Componentes.BotonWpp
import com.example.sodappcomposse.Producto.ProductoUiState
import com.example.sodappcomposse.Producto.ProductoVenta
import com.example.sodappcomposse.Producto.conDevueltos
import com.example.sodappcomposse.Producto.parsearPrecio
import com.example.sodappcomposse.Producto.productosDisponibles
import com.example.sodappcomposse.Producto.subtotal
import com.example.sodappcomposse.Producto.totalVenta
import com.example.sodappcomposse.Producto.ventaCompleta
import com.example.sodappcomposse.Componentes.SelectBuscable
import com.example.sodappcomposse.Funciones.formatearPrecioEditable
import com.example.sodappcomposse.Producto.ProductoViewModel
import com.example.sodappcomposse.R
import com.example.sodappcomposse.Reparto.RepartoTab
import com.example.sodappcomposse.Funciones.formatearPrecio
import com.example.sodappcomposse.Funciones.ProductoMensaje
import com.example.sodappcomposse.Funciones.armarMensajeVenta

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

@Composable
fun Ventas(navController: NavController) {
    // 0 = Ventas, 1 = Reparto
    var tabSeleccionado by rememberSaveable { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = tabSeleccionado) {
            listOf(stringResource(R.string.tab_ventas), stringResource(R.string.tab_reparto)).forEachIndexed { index, titulo ->
                Tab(
                    selected = tabSeleccionado == index,
                    onClick = { tabSeleccionado = index },
                    text = { Text(titulo) }
                )
            }
        }

        when (tabSeleccionado) {
            0 -> TabVentas()
            1 -> RepartoTab(
                onVerDias = { navController.navigate(Agenda) },
                onConfigurarPlanta = { navController.navigate(PlantaConfig) }
            )
        }
    }
}

@Composable
private fun TabVentas(
    ventaModel: VentasViewModel = hiltViewModel(),
    productoModel: ProductoViewModel = hiltViewModel(),
    clienteModel: ClientesViewModel = hiltViewModel()
){
    val TAG = "Ventas screen"
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        ventaModel.getVentas()
        productoModel.getProductos()
    }

    val ventasUiState = ventaModel.ventasUiState
    val ventasAgrupadas by ventaModel.ventasAgrupadas.collectAsState()
    val nombreEmpresa by ventaModel.nombreEmpresa.collectAsState(initial = null)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                AddVentaForm(
                    ventaModel = ventaModel,
                    productoModel = productoModel,
                    clienteModel = clienteModel
                )
            }
        }

        when {
            ventasUiState is VentasUiState.Loading -> {
                item { CircularProgressIndicator() }
            }
            ventasUiState is VentasUiState.Error -> {
                item { Text(stringResource(ventasUiState.messageRes, *ventasUiState.args), color = MaterialTheme.colorScheme.error) }
            }
            ventasAgrupadas.isEmpty() && ventasUiState is VentasUiState.Success -> {
                item { Text(stringResource(R.string.no_ventas_label)) }
            }
            ventasUiState is VentasUiState.Success -> {
                items(ventasAgrupadas) { ventaAgrupada ->
                    BoxVentas(venta = ventaAgrupada, context, nombreEmpresa)
                }
            }
            else -> {
                item { Text(stringResource(R.string.esperando_datos)) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVentaForm(
    clienteModel: ClientesViewModel = hiltViewModel(),
    productoModel: ProductoViewModel = hiltViewModel(),
    ventaModel: VentasViewModel = hiltViewModel()
){
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val clienteUiState = clienteModel.clienteUiState
    val clientes = clienteModel.clientes

    val productoUiState = productoModel.productoUiState
    val productos = productoModel.productos

    LaunchedEffect(Unit) {
        productoModel.getProductos()
    }

    LaunchedEffect(clienteUiState) {
        when (clienteUiState) {
            is ClienteUiState.Success -> {
                //Log.d("AddVentaForm", "Success: ${clientes.size} clients loaded.")
                // onVentaAgregada() // Llama a esto si es apropiado aquí
            }
            is ClienteUiState.Error -> {
                //Log.d("AddVentaForm", "Error: ${clienteUiState.message}")
                Toast.makeText(context, context.getString(clienteUiState.messageRes, *clienteUiState.args), Toast.LENGTH_SHORT).show()
            }
            is ClienteUiState.Loading -> {
                //Log.d("AddVentaForm", "Loading clients...")
            }
            ClienteUiState.Idle -> {
                //Log.d("AddVentaForm", "Client state is Idle.")
            }
        }
    }

    LaunchedEffect(productoUiState) {
        when (productoUiState) {
            is ProductoUiState.Success -> {
                //Log.d("AddVentaForm", "Success: ${productos.size} prods loaded.")
            }
            is ProductoUiState.Error -> {
                //Log.d("AddVentaForm", "Error: ${productoUiState.message}")
                Toast.makeText(context, context.getString(productoUiState.messageRes, *productoUiState.args), Toast.LENGTH_SHORT).show()
            }
            is ProductoUiState.Loading -> {
                //Log.d("AddVentaForm", "Loading prods...")
            }
            ProductoUiState.Idle -> {
                //Log.d("AddVentaForm", "Prod state is Idle.")
            }
        }
    }

    val clienteParaVenta = clienteModel.clienteParaDropDown

    // State to hold the list of chosen products with their quantities for the sale
    val productosParaVenta = remember { mutableStateListOf<ProductoVenta>() }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(stringResource(R.string.cargar_venta_titulo), style = MaterialTheme.typography.headlineSmall)

        ClientesDropDown()

        // Empty after each choice; products already in the sale are not offered again
        SelectBuscable(
            opciones = productosDisponibles(productos, productosParaVenta),
            etiqueta = { it.nombrePr },
            onSeleccionar = { producto ->
                productosParaVenta.add(ProductoVenta(nombre = producto.nombrePr, cantidad = 1, precio = producto.precioUni))
            },
            label = stringResource(R.string.agregar_producto_label),
            limpiarAlSeleccionar = true
        )

        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (productosParaVenta.isEmpty()) {
                Text(
                    stringResource(R.string.no_productos_elegidos),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            productosParaVenta.forEachIndexed { index, productoVenta ->
                // Keyed by product so each line keeps its own text fields after a removal
                key(productoVenta.nombre) {
                    ProductoCantidadItem(
                        producto = productoVenta,
                        onCantidadChange = { productosParaVenta[index] = productosParaVenta[index].copy(cantidad = it) },
                        onPrecioChange = { productosParaVenta[index] = productosParaVenta[index].copy(precio = it) },
                        onQuitar = { productosParaVenta.removeAt(index) }
                    )
                    // Envases devueltos: solo para productos retornables; el resto envía 0
                    val esRetornable = productos.find { it.nombrePr == productoVenta.nombre }?.esRetornable == true
                    if (esRetornable) {
                        EnvasesDevueltosItem(
                            devueltos = productoVenta.devueltos,
                            onDevueltosChange = { nuevos ->
                                productosParaVenta[index] = productosParaVenta[index].conDevueltos(nuevos)
                            },
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                    HorizontalDivider()
                }
            }
            if (productosParaVenta.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.total_format, formatearPrecio(totalVenta(productosParaVenta))),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Button(
            onClick = {
                if (clienteParaVenta.value == null) {
                    Toast.makeText(context, context.getString(R.string.seleccione_cliente_error), Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (!ventaCompleta(productosParaVenta)) {
                    Toast.makeText(context, context.getString(R.string.agregue_producto_error), Toast.LENGTH_SHORT).show()
                    return@Button
                }

                // No stock check: the delivery already happened, so a sale may leave stock negative

                // Usamos el scope que ya declaraste arriba con rememberCoroutineScope()
                scope.launch {
                    try {
                        // 1. Ejecuta la petición al backend (dentro de la corrutina)
                        val guardada = ventaModel.postVenta(
                            clienteId = clienteParaVenta.value!!.idCl,
                            productos = productosParaVenta.toList() // toList() crea una copia estable para la API
                        )

                        // Si el backend rechazó la venta, avisar y conservar el formulario
                        val estado = ventaModel.ventasUiState
                        if (!guardada) {
                            if (estado is VentasUiState.Error) {
                                Toast.makeText(context, context.getString(estado.messageRes, *estado.args), Toast.LENGTH_LONG).show()
                            }
                            return@launch
                        }

                        // 2. Refresca la lista de ventas
                        ventaModel.getVentas()

                        // 3. Limpiar el formulario tras el éxito
                        clienteParaVenta.value = null
                        productosParaVenta.clear()

                        Toast.makeText(context, context.getString(R.string.venta_cargada_success), Toast.LENGTH_SHORT).show()

                    } catch (e: Exception) {
                        Toast.makeText(context, context.getString(R.string.error_procesar_format, e.message ?: ""), Toast.LENGTH_LONG).show()
                    }
                }
            },
            // Habilitar botón solo si hay cliente y productos
            enabled = clienteParaVenta.value != null && ventaCompleta(productosParaVenta)
        ) {
            Text(stringResource(R.string.cargar_venta_label))
        }
    }
}

fun Modifier.borderBottom(width: Dp, color: Color): Modifier = this.then(
    Modifier.drawBehind {
        val strokeWidthPx = width.toPx()
        val y = size.height - strokeWidthPx / 2
        drawLine(
            color = color,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = strokeWidthPx
        )
    }
)

@SuppressLint("UnrememberedMutableState")
@Composable
 fun BoxVentas(
    venta: VentaAgrupada,
    context: Context,
    nombreEmpresa: String?,
    clienteModel: ClientesViewModel = hiltViewModel()
 ) {
     //Agrupa las venntas por cliente y fecha, para que ambos productos cargados el mismo dia se vean en un mismo box
    val cliente = clienteModel.clientes.find { it.idCl == venta.cliente?.idCl }
    val total = venta.productos.sumOf { (it.precio ?: 0.0) * it.cantidad }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = venta.cliente?.nombreCl ?: stringResource(R.string.desconocido_label),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = venta.fecha,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            HorizontalDivider()
            // One line per product: name, quantity x unit price, subtotal
            venta.productos.forEach { producto ->
                val precio = producto.precio ?: 0.0
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = producto.nombre,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = stringResource(R.string.linea_venta_format, producto.cantidad, formatearPrecio(precio)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$" + formatearPrecio(precio * producto.cantidad),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.End,
                        modifier = Modifier.widthIn(min = 80.dp)
                    )
                }
            }
            HorizontalDivider()
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.items_format, venta.cantidadTotalVenta),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = stringResource(R.string.total_format, formatearPrecio(total)),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            BotonWpp(
                context = context,
                cliente = cliente,
                mensaje = armarMensajeVenta(
                    context.resources,
                    venta.cliente?.nombreCl,
                    venta.productos.map { ProductoMensaje(it.cantidad, it.nombre, it.precio) },
                    total,
                    venta.cliente?.deudaCl,
                    nombreEmpresa
                ),
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

/**
 * One product of the sale: name, subtotal and remove on top; quantity (− field +) and editable
 * unit price below. A blank quantity is 0 and keeps the line: only the trash button removes it.
 */
@Composable
fun ProductoCantidadItem(
    producto: ProductoVenta,
    onCantidadChange: (Int) -> Unit,
    onPrecioChange: (Double?) -> Unit,
    onQuitar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val disminuirDesc = stringResource(R.string.disminuir_cantidad_desc)
    var cantidadTexto by remember { mutableStateOf(producto.cantidad.toString()) }
    var precioTexto by remember { mutableStateOf(formatearPrecioEditable(producto.precio ?: 0.0)) }
    // Follow the − / + buttons without overwriting a field the user left blank (0)
    LaunchedEffect(producto.cantidad) {
        if ((cantidadTexto.toIntOrNull() ?: 0) != producto.cantidad) cantidadTexto = producto.cantidad.toString()
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = producto.nombre,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "$" + formatearPrecio(producto.subtotal),
                style = MaterialTheme.typography.bodyLarge
            )
            IconButton(onClick = onQuitar) {
                Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.quitar_producto_desc))
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { onCantidadChange(producto.cantidad - 1) },
                enabled = producto.cantidad > 1,
                modifier = Modifier.semantics { contentDescription = disminuirDesc }
            ) {
                Text(text = "−", style = MaterialTheme.typography.titleLarge)
            }
            OutlinedTextField(
                value = cantidadTexto,
                onValueChange = { nuevo ->
                    cantidadTexto = nuevo.filter(Char::isDigit)
                    onCantidadChange(cantidadTexto.toIntOrNull() ?: 0)
                },
                modifier = Modifier.width(64.dp),
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            IconButton(onClick = { onCantidadChange(producto.cantidad + 1) }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.aumentar_cantidad_desc))
            }
            Text("×", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(horizontal = 4.dp))
            OutlinedTextField(
                value = precioTexto,
                onValueChange = { nuevo ->
                    precioTexto = nuevo
                    onPrecioChange(parsearPrecio(nuevo))
                },
                prefix = { Text("$") },
                isError = parsearPrecio(precioTexto)?.let { it < 0 } ?: true,
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )
        }
    }
}


/** "Envases devueltos" stepper for a returnable product in the sale form (default 0, never negative). */
@Composable
fun EnvasesDevueltosItem(
    devueltos: Int,
    onDevueltosChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val disminuirDesc = stringResource(R.string.disminuir_devueltos_desc)
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.envases_devueltos_label),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        IconButton(
            onClick = { onDevueltosChange(devueltos - 1) },
            enabled = devueltos > 0,
            modifier = Modifier.semantics { contentDescription = disminuirDesc }
        ) {
            Text(text = "−", style = MaterialTheme.typography.titleLarge)
        }
        OutlinedTextField(
            value = devueltos.toString(),
            onValueChange = { newValue ->
                if (newValue.isEmpty()) {
                    onDevueltosChange(0)
                } else {
                    newValue.toIntOrNull()?.let { onDevueltosChange(it) }
                }
            },
            modifier = Modifier.width(75.dp),
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
        IconButton(onClick = { onDevueltosChange(devueltos + 1) }) {
            Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.aumentar_devueltos_desc))
        }
    }
}
