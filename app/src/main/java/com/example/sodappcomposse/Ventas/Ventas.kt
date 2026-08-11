package com.example.sodappcomposse.Ventas

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.sodappcomposse.Agenda
import com.example.sodappcomposse.Cliente.ClienteUiState
import com.example.sodappcomposse.Cliente.ClientesViewModel
import com.example.sodappcomposse.Cliente.ClientesDropDown
import com.example.sodappcomposse.Componentes.CardWpp
import com.example.sodappcomposse.Producto.ProductoUiState
import com.example.sodappcomposse.Producto.ProductoVenta
import com.example.sodappcomposse.Producto.ProductoViewModel
import com.example.sodappcomposse.R

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Ventas(
    ventaModel: VentasViewModel = hiltViewModel(),
    productoModel: ProductoViewModel = hiltViewModel(),
    clienteModel: ClientesViewModel = hiltViewModel(),
    navController: NavController
){
    val TAG = "Ventas screen"
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        ventaModel.getVentas()
        productoModel.getProductos()
    }

    val ventasUiState = ventaModel.ventasUiState
    val ventasAgrupadas by ventaModel.ventasAgrupadas.collectAsState()

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

        item {
            Button(
                onClick = {
                    navController.navigate(Agenda)
                }
            ) {
                Icon(Icons.Filled.DateRange, contentDescription = stringResource(R.string.agendar_desc))
                Spacer(modifier = Modifier.width(4.dp))
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
                    BoxVentas(venta = ventaAgrupada, context)
                }
            }
            else -> {
                item { Text(stringResource(R.string.esperando_datos)) }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
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

    val optionsProds: List<String> = when (productoUiState) {
        is ProductoUiState.Success -> productos.map { it.nombrePr }
        is ProductoUiState.Loading -> listOf("Cargando productos...")
        else -> listOf("No hay productos disponibles")
    }

    var expandedProds by remember { mutableStateOf(false) }

    var currentSelectedProductInDropdown by remember(optionsProds) {
        mutableStateOf(
            if (optionsProds.isNotEmpty() && optionsProds.first() != "Cargando productos..." && optionsProds.first() != "No hay productos disponibles") {
                optionsProds[0]
            } else {
                ""
            }
        )
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

        ExposedDropdownMenuBox(
            expanded = expandedProds,
            onExpandedChange = { expandedProds = !expandedProds },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = currentSelectedProductInDropdown,
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.producto_label)) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedProds)
                },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expandedProds,
                onDismissRequest = { expandedProds = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                // Asegúrate de que optionsProds no contenga los placeholders si no quieres que sean clickeables
                val displayableProds = optionsProds.filter { it != "Cargando productos..." && it != "No hay productos disponibles" }
                displayableProds.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption) },
                        onClick = {
                            currentSelectedProductInDropdown = selectionOption
                            expandedProds = false

                            if (productosParaVenta.none { it.nombre == selectionOption }) {
                                val productoCompleto = productos.find { it.nombrePr == selectionOption }

                                productosParaVenta.add(
                                    ProductoVenta(
                                        nombre = selectionOption,
                                        cantidad = 1,      // Cantidad inicial
                                        precio = productoCompleto?.precioUni ?: 0.0
                                    )
                                )
                                Toast.makeText(context, context.getString(R.string.producto_agregado_msg, selectionOption), Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, context.getString(R.string.producto_ya_en_lista_msg, selectionOption), Toast.LENGTH_SHORT).show()
                            }
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp) // Add some padding
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    stringResource(R.string.cliente_format, clienteParaVenta.value?.nombreCl ?: stringResource(R.string.no_seleccionado)),
                    style = MaterialTheme.typography.bodyMedium, // Adjusted style
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    stringResource(R.string.productos_elegidos_titulo),
                    style = MaterialTheme.typography.bodyMedium, // Adjusted style
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                if (productosParaVenta.isEmpty()) {
                    Text(
                        stringResource(R.string.no_productos_elegidos),
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    productosParaVenta.forEachIndexed { index, productoVenta ->
                        ProductoCantidadItem(
                            nombreProducto = productoVenta.nombre,
                            cantidad = productoVenta.cantidad,
                            onCantidadChange = { nuevaCantidad ->
                                if (nuevaCantidad <= 0) {
                                    // Remove the item if quantity becomes 0 or less
                                    productosParaVenta.removeAt(index)
                                } else {
                                    // Update the item by creating a new instance
                                    productosParaVenta[index] = productoVenta.copy(cantidad = nuevaCantidad)
                                }
                            },
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        // Optional: Add a Divider between items
                        // if (index < productosParaVenta.size - 1) {
                        //     Divider(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        // }
                    }
                }
            }
        }

        Button(
            onClick = {
                if (clienteParaVenta.value == null) {
                    Toast.makeText(context, context.getString(R.string.seleccione_cliente_error), Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (productosParaVenta.isEmpty()) {
                    Toast.makeText(context, context.getString(R.string.agregue_producto_error), Toast.LENGTH_SHORT).show()
                    return@Button
                }

                // Validación de Stock local
                val productosSinStock = productosParaVenta.filter { prodVenta ->
                    val stockDisponible = productos.find { it.nombrePr == prodVenta.nombre }?.stock ?: 0
                    stockDisponible < prodVenta.cantidad
                }
                if (productosSinStock.isNotEmpty()) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.stock_insuficiente_format, productosSinStock.joinToString { it.nombre }),
                        Toast.LENGTH_LONG
                    ).show()
                    return@Button
                }

                // Usamos el scope que ya declaraste arriba con rememberCoroutineScope()
                scope.launch {
                    try {
                        // 1. Ejecuta la petición al backend (dentro de la corrutina)
                        ventaModel.postVenta(
                            clienteId = clienteParaVenta.value!!.idCl,
                            productos = productosParaVenta.toList() // toList() crea una copia estable para la API
                        )

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
            enabled = clienteParaVenta.value != null && productosParaVenta.isNotEmpty()
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

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnrememberedMutableState")
@Composable
 fun BoxVentas(
    venta: VentaAgrupada,
    context: Context,
    clienteModel: ClientesViewModel = hiltViewModel()
 ) {
     //Agrupa las venntas por cliente y fecha, para que ambos productos cargados el mismo dia se vean en un mismo box
    var cliente = clienteModel.clientes.find { it.idCl == venta.cliente?.idCl }
    var totales : MutableList<Double?> = mutableListOf(0.0)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.cliente_format, venta.cliente?.nombreCl ?: stringResource(R.string.desconocido_label)),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.fecha_format, venta.fecha),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.productos_titulo),
                style = MaterialTheme.typography.titleSmall
            )
            venta.productos.forEach { producto ->
                totales.add(producto.precio?.times(producto.cantidad))

                Row (
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .fillMaxWidth()
                        .borderBottom(1.dp, Color.LightGray),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Text(
                        text = stringResource(R.string.producto_item_format, producto.nombre),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = stringResource(R.string.cantidad_item_format, producto.cantidad),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = stringResource(R.string.precio_item_format, producto.precio ?: 0.0),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.total_format, totales.sumOf{ it ?: 0.0  }),
                style = MaterialTheme
                    .typography.bodyMedium
                    .copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                CardWpp(
                    context,
                    cliente,
                    armarMensajeVentasWpp(context, venta, totales.sumOf{ it ?: 0.0  })
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.cantidad_total_items_format, venta.cantidadTotalVenta),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

fun armarMensajeVentasWpp(context: Context, venta: VentaAgrupada, total : Double): String{
    val productosString = venta.productos.joinToString(
        separator = ", ",
        transform = { 
            context.getString(R.string.wpp_producto_format, it.cantidad, it.nombre, it.precio)
        }
    )
    return context.getString(R.string.wpp_mensaje_ventas, productosString, total, venta.cliente?.deudaCl ?: 0.0)
}

@Composable
fun ProductoCantidadItem(
    nombreProducto: String,
    cantidad: Int,
    onCantidadChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = nombreProducto,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.weight(1f) // Text takes available space
        )

        Spacer(modifier = Modifier.width(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    if (cantidad > 1) {
                        onCantidadChange(cantidad - 1)
                    } else if (cantidad == 1) {
                        onCantidadChange(0)
                    }
                },
                enabled = cantidad > 0
            ) {
                Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.disminuir_cantidad_desc))
            }

            OutlinedTextField(
                value = if (cantidad == 0) "" else cantidad.toString(),
                onValueChange = { newValue ->
                    val newQuantity = newValue.toIntOrNull()
                    if (newValue.isEmpty()) {
                        onCantidadChange(0)
                    } else if (newQuantity != null) {
                        if (newQuantity >= 0) {
                            onCantidadChange(newQuantity)
                        } else {
                            onCantidadChange(0)
                        }
                    }
                },
                modifier = Modifier.width(75.dp), // Adjusted width
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )

            IconButton(onClick = {
                onCantidadChange(cantidad + 1)
            }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.aumentar_cantidad_desc))
            }
        }
    }
}
