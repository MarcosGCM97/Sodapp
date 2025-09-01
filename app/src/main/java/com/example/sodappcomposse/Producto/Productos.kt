package com.example.sodappcomposse.Producto

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sodappcomposse.Cliente.AddClienteForm
import com.example.sodappcomposse.Cliente.ClientesViewModel
import com.example.sodappcomposse.Ventas.Ventas

@Composable
fun Productos(
    navController: NavController
){
    val TAG = "Productos"
    val scrollState = rememberScrollState()


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
            AddProductoForm(
                navController = navController
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductoForm(
    productosModel: ProductoViewModel = viewModel(),
    navController: NavController
){
    val context = LocalContext.current

    val productoUiState = productosModel.productoUiState
    val producto = productosModel.productos

    var nombreProducto by remember { mutableStateOf("") }
    var precioProducto by remember { mutableStateOf("") }
    var cantidadInputString by remember { mutableStateOf("1") }

    val cantidadProducto: Int = cantidadInputString.toIntOrNull() ?: 1

    val addState by productosModel.addProductoUiState
    LaunchedEffect(addState) {
        when (val currentState = addState) {
            is AddProductoUiState.Success -> {
                Toast.makeText(context, currentState.message, Toast.LENGTH_LONG).show()
                nombreProducto = "" // Limpiar campos
                precioProducto = ""
                cantidadInputString = "1"
                productosModel.resetAddProductoState() // Resetear el estado para futuros guardados
            }
            is AddProductoUiState.Error -> {
                Toast.makeText(context, currentState.message, Toast.LENGTH_LONG).show()
                productosModel.resetAddProductoState()
            }
            is AddProductoUiState.Loading -> {
                // Podrías mostrar un indicador de carga aquí si el guardado tarda mucho
            }
            AddProductoUiState.Idle -> {
                // No hacer nada
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Cargar al producto", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(
            value = nombreProducto,
            onValueChange = { nombreProducto = it },
            label = { Text("Nombre del Producto") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Campo para el Precio del Producto
        OutlinedTextField(
            value = precioProducto,
            onValueChange = { precioProducto = it },
            label = { Text("Precio") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        //Campo para cargar la cantidad
        OutlinedTextField(
            value = cantidadInputString,
            onValueChange = { newValue ->
                if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                    cantidadInputString = newValue
                }
            },
            label = { Text("Cantidad") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = cantidadInputString.toIntOrNull() == null && cantidadInputString.isNotEmpty() // Show error if not empty and not a valid number
        )

        //Botón para guardar el nuevo producto
        Button(
            onClick = {
                productosModel.agregarNuevoProducto(
                    nombre = nombreProducto,
                    precio = precioProducto,
                    cantidad = cantidadProducto
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = nombreProducto.isNotBlank() && precioProducto.isNotBlank() && cantidadProducto > 0 && addState !is AddProductoUiState.Loading
        ){
            if (addState is AddProductoUiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Guardar Producto")
            }
        }

        //Separador visual si quieres (opcional)
        Divider(modifier = Modifier.padding(vertical = 16.dp))

        Text("o buscar uno existente:", style = MaterialTheme.typography.titleMedium) // Añadido para dar contexto al buscador

        BuscarProducto(
            navController = navController
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuscarProducto(
    productosModel: ProductoViewModel = viewModel(),
    navController: NavController
){
    val context = LocalContext.current

    val productoUiState = productosModel.productoUiState
    //val producto = productosModel.productos

    var productoSeleccionado by productosModel.productosParaDropDown

    // Estado para controlar la visibilidad del diálogo de confirmación
    var mostrarDialogoConfirmacion by remember { mutableStateOf(false) }
    // Estado para recordar qué producto se va a eliminar (si el usuario confirma)
    var productoAEliminar by remember { mutableStateOf<Producto?>(null) }


    LaunchedEffect(productoUiState) {
        when (productoUiState) {
            is ProductoUiState.Success -> {
                //Log.d("AddProductoForm", "Success: ${productos.size} productos loaded.")
            }
            is ProductoUiState.Error -> {
                //Log.d("AddProductoForm", "Error: ${productoUiState.message}")
                Toast.makeText(context, productoUiState.message, Toast.LENGTH_SHORT).show()
            }
            is ProductoUiState.Loading -> {
                //Log.d("AddProductoForm", "Loading productos...")
            }
            ProductoUiState.Idle -> {
                //Log.d("AddProductoForm", "Producto state is Idle.")
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ProductoDropDown()
    }
    productoSeleccionado?.let { producto -> // Se muestra solo si hay un producto seleccionado
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Detalles del Producto:", style = MaterialTheme.typography.titleMedium)
                Text("Nombre: ${producto.nombrePr}")
                Text("Precio: ${producto.precioUni}")
                Text("Cantidad: ${producto.stock}")

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start // Alinea el botón a la derecha
                ) {
                    Button(
                        onClick = {
                            navController.navigate("productoEditarScreen/${producto.nombrePr}")
                        }
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = "Editar")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Editar")
                    }
                    Spacer(modifier = Modifier.width(8.dp)) // Espacio entre los botones
                    Button(
                        onClick = {
                            // Guardar el producto que se intenta eliminar y mostrar el diálogo
                            productoAEliminar = producto
                            mostrarDialogoConfirmacion = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error) // Color rojo para eliminar
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Eliminar")
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }
            }
        }
    }
    // Diálogo de confirmación
    if (mostrarDialogoConfirmacion && productoAEliminar != null) {
        AlertDialog(
            onDismissRequest = {
                // Se llama cuando el usuario toca fuera del diálogo o presiona el botón de atrás
                mostrarDialogoConfirmacion = false
                productoAEliminar = null // Limpiar el producto a eliminar
            },
            title = {
                Text(text = "Confirmar Eliminación")
            },
            text = {
                Text("¿Estás seguro de que deseas eliminar el producto \"${productoAEliminar?.nombrePr}\"? Esta acción no se puede deshacer.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        productoAEliminar?.let { prod ->
                            productosModel.eliminarProducto(prod)
                            // Opcional: Refrescar la lista de productos explícitamente si no se actualiza automáticamente
                            // productosModel.getProductos() // Ya lo haces abajo, pero considera el momento
                        }
                        mostrarDialogoConfirmacion = false
                        productoAEliminar = null
                        productosModel.productosParaDropDown
                        productoSeleccionado = null
                        productosModel.getProductos()
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        mostrarDialogoConfirmacion = false
                        productoAEliminar = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}


@Preview(showSystemUi = true)
@Composable
fun PreviewProductosScreen(){
    Productos(navController = NavController(LocalContext.current))
}