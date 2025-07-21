package com.example.sodappcomposse.Ventas

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import android.widget.Toast
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sodappcomposse.Cliente.Cliente
import com.example.sodappcomposse.Cliente.ClienteUiState
import com.example.sodappcomposse.Cliente.ClientesViewModel
import com.example.sodappcomposse.Cliente.ClientesDropDown
import com.example.sodappcomposse.Producto.ProductoUiState
import com.example.sodappcomposse.Producto.ProductoVenta
import com.example.sodappcomposse.Producto.ProductoViewModel

@Composable
fun Ventas(
    ventaModel: VentasViewModel = viewModel()
){
    val TAG = "Ventas"
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        ventaModel.getVentas()
    }

    val ventasUiState = ventaModel.ventasUiState // Observe this for Loading/Error/Success
    val listaOriginalVentas = ventaModel.ventas

    // This will hold the grouped sales
    val ventasAgrupadas = remember(listaOriginalVentas.toList()) { // Recalculate if listaOriginalVentas changes
        if (listaOriginalVentas.isEmpty()) {
            Log.e(TAG, "No hay ventas para mostrar.")
            emptyList<VentaAgrupada>()
        } else {
            Log.d(TAG, "Ventas recibidas: $listaOriginalVentas")
            // Step 1: Group by Client Name and Date
            val groupedByClienteAndFecha = listaOriginalVentas.groupBy {
                Pair(it.cliente.nombreCl, it.fecha) // Create a Pair to group by two criteria
            }

            // Step 2: Transform the grouped data into VentaAgrupada
            val resultado = groupedByClienteAndFecha.map { (clienteFechaPair, ventasDelGrupo) ->
                // All ventasDelGrupo have the same client and fecha
                val cliente = ventasDelGrupo.first().cliente // Get client info from the first item
                val fecha = clienteFechaPair.second // Get fecha from the Pair

                // Step 2a: Group products within this client/date group and sum quantities
                val productosSumados = ventasDelGrupo
                    .groupBy { it.producto } // Group by product name
                    .map { (nombreProducto, itemsProducto) ->
                        ProductoVenta(
                            nombre = nombreProducto,
                            cantidad = itemsProducto.sumOf { it.cantidad.toIntOrNull() ?: 0 } // Convert String quantity to Int and sum
                        )
                    }

                val cantidadTotalDeEstaVenta = productosSumados.sumOf { it.cantidad }

                VentaAgrupada(
                    cliente = cliente,
                    fecha = fecha,
                    productos = productosSumados,
                    cantidadTotalVenta = cantidadTotalDeEstaVenta
                )
            }
            resultado
        }
    }

    // Handle UI State for loading/error messages based on ventasUiState
    // ...

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
            Log.d("VentasComposable", "UI State: Loading")
        } else if (ventasUiState is VentasUiState.Error) {
            Text("Error: ${(ventasUiState as VentasUiState.Error).message}", color = MaterialTheme.colorScheme.error)
            Log.d("VentasComposable", "UI State: Error - ${(ventasUiState as VentasUiState.Error).message}")
        } else if (ventasAgrupadas.isEmpty() && ventasUiState is VentasUiState.Success) {
            Text("No hay ventas para mostrar.")
            Log.d("VentasComposable", "UI State: Success pero ventasAgrupadas está vacía.")
        } else if (ventasUiState is VentasUiState.Success){ // Si es Success y ventasAgrupadas no está vacía
            Log.d("VentasComposable", "UI State: Success, mostrando ${ventasAgrupadas.size} ventas agrupadas.")
            ventasAgrupadas.forEach { ventaAgrupada ->
                BoxVentas(venta = ventaAgrupada)
            }
        } else {
            // Caso por defecto o si ventasUiState es Idle y la lista no está vacía (poco probable aquí)
            Text("Esperando datos...")
            Log.d("VentasComposable", "UI State: Idle o estado no manejado y ventasAgrupadas está vacía.")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVentaForm(
    ventaModel: VentasViewModel = viewModel(),
    clienteModel: ClientesViewModel = viewModel(),
    productoModel: ProductoViewModel = viewModel()
){
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        clienteModel.getClientes()
        productoModel.getProductos()
    }
    val clienteUiState = clienteModel.clienteUiState
    val clientes = clienteModel.clientes

    val productoUiState = productoModel.productoUiState
    val productos = productoModel.productos

    LaunchedEffect(clienteUiState) {
        when (clienteUiState) {
            is ClienteUiState.Success -> {
                Log.d("AddVentaForm", "Success: ${clientes.size} clients loaded.")
                // onVentaAgregada() // Llama a esto si es apropiado aquí
            }
            is ClienteUiState.Error -> {
                Log.d("AddVentaForm", "Error: ${clienteUiState.message}")
                Toast.makeText(context, clienteUiState.message, Toast.LENGTH_SHORT).show()
            }
            is ClienteUiState.Loading -> {
                Log.d("AddVentaForm", "Loading clients...")
            }
            ClienteUiState.Idle -> {
                Log.d("AddVentaForm", "Client state is Idle.")
            }
        }
    }

    LaunchedEffect(productoUiState) {
        when (productoUiState) {
            is ProductoUiState.Success -> {
                Log.d("AddVentaForm", "Success: ${productos.size} prods loaded.")
                // onVentaAgregada() // Llama a esto si es apropiado aquí
            }
            is ProductoUiState.Error -> {
                Log.d("AddVentaForm", "Error: ${productoUiState.message}")
                Toast.makeText(context, productoUiState.message, Toast.LENGTH_SHORT).show()
            }
            is ProductoUiState.Loading -> {
                Log.d("AddVentaForm", "Loading prods...")
            }
            ProductoUiState.Idle -> {
                Log.d("AddVentaForm", "Prod state is Idle.")
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

    val clienteParaVenta = clienteModel.clienteParaVenta

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

            Log.d("AddVentaForm", "Venta a cargar:")
            Log.d("AddVentaForm", "Cliente: ${clienteParaVenta}")
            productosParaVenta.forEach {
                Log.d("AddVentaForm", "Producto: ${it.nombre}, Cantidad: ${it.cantidad}")
            }
            Toast.makeText(context, "Venta lista para procesar (ver logs)", Toast.LENGTH_LONG).show()
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

@Composable
 fun BoxVentas(venta: VentaAgrupada) {
     //Agrupa las venntas por cliente y fecha, para que ambos productos cargados el mismo dia se vean en un mismo box
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "- ${producto.nombre}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Cantidad: ${producto.cantidad}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Divider()
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
                    // Call onCantidadChange with 0 if you want to trigger removal logic
                    // The parent Composable (AddVentaForm) will handle the actual removal.
                    if (cantidad > 1) {
                        onCantidadChange(cantidad - 1)
                    } else if (cantidad == 1) { // If at 1, next decrement means 0 (signal for removal)
                        onCantidadChange(0)
                    }
                },
                enabled = cantidad > 0 // Only enable if quantity can be decreased
            ) {
                Icon(Icons.Filled.Delete, contentDescription = "Disminuir cantidad")
            }

            OutlinedTextField(
                value = if (cantidad == 0) "" else cantidad.toString(), // Show empty if quantity is 0 (pending removal)
                onValueChange = { newValue ->
                    val newQuantity = newValue.toIntOrNull()
                    if (newValue.isEmpty()) {
                        onCantidadChange(0) // Interpret empty as wanting to remove or set to 0
                    } else if (newQuantity != null) {
                        if (newQuantity >= 0) { // Allow 0 to be typed, parent handles removal
                            onCantidadChange(newQuantity)
                        } else {
                            onCantidadChange(0) // Or some minimum valid quantity like 1
                        }
                    }
                    // If input is not a valid number or empty, do nothing, or reset to current 'cantidad'
                },
                modifier = Modifier.width(75.dp), // Adjusted width
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                // Visual density can make it smaller if needed
                // visualTransformation = VisualTransformation.None,
                // colors = TextFieldDefaults.outlinedTextFieldColors( /* ... */ )
            )

            IconButton(onClick = {
                onCantidadChange(cantidad + 1)
                // Add max limit if needed: if (cantidad < MAX_QUANTITY) onCantidadChange(cantidad + 1)
            }) {
                Icon(Icons.Filled.Add, contentDescription = "Aumentar cantidad")
            }
        }
    }
}


@Preview(showSystemUi = true)
@Composable
fun PreviewVentasScreen(){
    Ventas()
}