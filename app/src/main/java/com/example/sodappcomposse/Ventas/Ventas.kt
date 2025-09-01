package com.example.sodappcomposse.Ventas

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import android.widget.Toast
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sodappcomposse.Cliente.ClienteUiState
import com.example.sodappcomposse.Cliente.ClientesViewModel
import com.example.sodappcomposse.Cliente.ClientesDropDown
import com.example.sodappcomposse.Componentes.CardWpp
import com.example.sodappcomposse.Producto.ProductoUiState
import com.example.sodappcomposse.Producto.ProductoVenta
import com.example.sodappcomposse.Producto.ProductoViewModel

@Composable
fun Ventas(
    ventaModel: VentasViewModel = viewModel(),
    productoModel: ProductoViewModel = viewModel(),
){
    val TAG = "Ventas screen"
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        productoModel.getProductos()
        ventaModel.getVentas()
    }

    val ventasUiState = ventaModel.ventasUiState
    val listaOriginalVentas = ventaModel.ventas
    val listaOriginalProductos = productoModel.productos

    val ventasAgrupadas = remember(listaOriginalVentas.toList()) {
        if (listaOriginalVentas.isEmpty()) {
            emptyList<VentaAgrupada>()
        } else {
            val groupedByClienteAndFecha = listaOriginalVentas.groupBy {
                Pair(it.cliente.nombreCl, it.fecha)
            }

            val resultado = groupedByClienteAndFecha.map { (clienteFechaPair, ventasDelGrupo) ->
                val cliente = ventasDelGrupo.first().cliente
                val fecha = clienteFechaPair.second

                val productosSumados = ventasDelGrupo
                    .groupBy { it.producto }
                    .map { (nombreProducto, itemsProducto) ->
                        ProductoVenta(
                            nombre = nombreProducto,
                            cantidad = itemsProducto.sumOf { it.cantidad.toIntOrNull() ?: 0 },
                            precio = listaOriginalProductos.find { it.nombrePr == nombreProducto }?.precioUni?.toDouble()
                        )
                    }

                val cantidadTotalDeEstaVenta = productosSumados.sumOf { it.cantidad }
                val montoTotalDeEstaVenta = productosSumados.sumOf { it.cantidad * (it.precio ?: 0.0) }

                VentaAgrupada(
                    cliente = cliente,
                    fecha = fecha,
                    productos = productosSumados,
                    cantidadTotalVenta = cantidadTotalDeEstaVenta,
                    montoTotalVenta = montoTotalDeEstaVenta
                )
            }
            resultado
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            AddVentaForm(
            ) // Assuming this is defined elsewhere or above
        }

        if (ventasUiState is VentasUiState.Loading) {
            CircularProgressIndicator()
            //Log.d("VentasComposable", "UI State: Loading")
        } else if (ventasUiState is VentasUiState.Error) {
            Text("Error: ${(ventasUiState).message}", color = MaterialTheme.colorScheme.error)
            //Log.d("VentasComposable", "UI State: Error - ${(ventasUiState as VentasUiState.Error).message}")
        } else if (ventasAgrupadas.isEmpty() && ventasUiState is VentasUiState.Success) {
            Text("No hay ventas para mostrar.")
            //Log.d("VentasComposable", "UI State: Success pero ventasAgrupadas está vacía.")
        } else if (ventasUiState is VentasUiState.Success){
            //Log.d("VentasComposable", "UI State: Success, mostrando ${ventasAgrupadas.size} ventas agrupadas.")
            ventasAgrupadas.forEach { ventaAgrupada ->
                BoxVentas(venta = ventaAgrupada, context)
            }
        } else {
            // Caso por defecto o si ventasUiState es Idle y la lista no está vacía (poco probable aquí)
            Text("Esperando datos...")
            //Log.d("VentasComposable", "UI State: Idle o estado no manejado y ventasAgrupadas está vacía.")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVentaForm(
    clienteModel: ClientesViewModel = viewModel(),
    productoModel: ProductoViewModel = viewModel(),
    ventaModel: VentasViewModel = viewModel()
){
    val context = LocalContext.current

    val clienteUiState = clienteModel.clienteUiState
    val clientes = clienteModel.clientes

    val productoUiState = productoModel.productoUiState
    val productos = productoModel.productos


    LaunchedEffect(clienteUiState) {
        when (clienteUiState) {
            is ClienteUiState.Success -> {
                //Log.d("AddVentaForm", "Success: ${clientes.size} clients loaded.")
                // onVentaAgregada() // Llama a esto si es apropiado aquí
            }
            is ClienteUiState.Error -> {
                //Log.d("AddVentaForm", "Error: ${clienteUiState.message}")
                Toast.makeText(context, clienteUiState.message, Toast.LENGTH_SHORT).show()
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
                Toast.makeText(context, productoUiState.message, Toast.LENGTH_SHORT).show()
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

// State for the product selected in the dropdown (to be added to the list)
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
        Text("Cargar la venta", style = MaterialTheme.typography.headlineSmall)

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
                label = { Text("Producto") },
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
                            currentSelectedProductInDropdown = selectionOption // Update current selection in dropdown
                            expandedProds = false
                            // Add to productosParaVenta if not already present
                            if (productosParaVenta.none { it.nombre == selectionOption }) {
                                productoModel.getProductoByName(selectionOption)//cargo el producto para despues procesar la deuda del cliente

                                productosParaVenta.add(ProductoVenta(nombre = selectionOption)) // Adds with default quantity 1
                                Toast.makeText(context, "$selectionOption agregado a la lista", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "$selectionOption ya está en la lista", Toast.LENGTH_SHORT).show()
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
                    "Cliente: ${clienteParaVenta.value?.nombreCl ?: "No seleccionado"}",
                    style = MaterialTheme.typography.bodyMedium, // Adjusted style
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    "Productos elegidos:",
                    style = MaterialTheme.typography.bodyMedium, // Adjusted style
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                if (productosParaVenta.isEmpty()) {
                    Text(
                        "- No hay productos elegidos",
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

        Button(onClick = {
            if (clienteParaVenta.value == null) {
                Toast.makeText(context, "Por favor, seleccione un cliente", Toast.LENGTH_SHORT).show()
                return@Button
            }
            if (productosParaVenta.isEmpty()) {
                Toast.makeText(context, "Por favor, agregue al menos un producto", Toast.LENGTH_SHORT).show()
                return@Button
            }

            //Log.d("AddVentaForm", "Venta a cargar:")
            //Log.d("AddVentaForm", "Cliente: ${clienteParaVenta}")

            //Toast.makeText(context, "Venta lista para procesar (ver Logs)", Toast.LENGTH_LONG).show()
            ventaModel.postVenta(clienteId = clienteParaVenta.value!!.idCl, productos = productosParaVenta)
            ventaModel.getVentas()

            //Example: Clear form after processing
            clienteParaVenta.value = null
            productosParaVenta.clear()
        },
            // Enable button only if a client is selected and at least one product is added
            enabled = clienteParaVenta.value != null && productosParaVenta.isNotEmpty()
        ) {
            Text("Cargar venta")
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
    clienteModel: ClientesViewModel = viewModel()
 ) {
     //Agrupa las venntas por cliente y fecha, para que ambos productos cargados el mismo dia se vean en un mismo box
     var cliente = clienteModel.clientes.find { it.idCl == venta.cliente.idCl }
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
                text = "Cliente: ${venta.cliente.nombreCl}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Fecha: ${venta.fecha}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Productos:",
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
                        text = "- ${producto.nombre}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "-Cantidad: ${producto.cantidad}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "-Precio x 1: $${producto.precio}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Total: $${totales.sumOf{ it ?: 0.0  }}",
                style = MaterialTheme
                    .typography.bodyMedium
                    .copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                CardWpp(
                    context,
                    cliente,
                    armarMensajeVentasWpp(venta, totales.sumOf{ it ?: 0.0  })
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Cantidad Total de Items: ${venta.cantidadTotalVenta}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

fun armarMensajeVentasWpp(venta: VentaAgrupada, total : Double): String{
    val productosString = venta.productos.joinToString(
        separator = ", ",
        transform = { it.cantidad.toString() + " " + it.nombre + " por $" + it.precio.toString() + " c/u" }
    )
    return "Tu compra fue de $productosString, por un total de $$total. Gracias por tu compra!"
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
                Icon(Icons.Filled.Delete, contentDescription = "Disminuir cantidad")
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
                Icon(Icons.Filled.Add, contentDescription = "Aumentar cantidad")
            }
        }
    }
}

/*
@Preview(showSystemUi = true)
@Composable
fun PreviewVentasScreen(){
    Ventas()
}*/