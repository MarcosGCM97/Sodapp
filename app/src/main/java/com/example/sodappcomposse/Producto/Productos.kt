package com.example.sodappcomposse.Producto

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sodappcomposse.Cliente.AddClienteForm
import com.example.sodappcomposse.Cliente.ClientesViewModel

@Composable
fun Productos(
    productosModel: ProductoViewModel = viewModel()
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
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductoForm(
    productosModel: ProductoViewModel = viewModel()
){
    val context = LocalContext.current

    val productoUiState = productosModel.productoUiState
    val producto = productosModel.productos

    var nombreProducto by remember { mutableStateOf("") }
    var precioProducto by remember { mutableStateOf("") }
    val cantidadProducto = remember { mutableStateOf(1) }

    val addState by productosModel.addProductoUiState
    LaunchedEffect(addState) {
        when (val currentState = addState) {
            is AddProductoUiState.Success -> {
                Toast.makeText(context, currentState.message, Toast.LENGTH_LONG).show()
                nombreProducto = "" // Limpiar campos
                precioProducto = ""
                cantidadProducto.value = 1
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
            value = cantidadProducto.value.toString(),
            onValueChange = { cantidadProducto.value = it.toIntOrNull() ?: 1 },
            label = { Text("Cantidad") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        //Botón para guardar el nuevo producto
        Button(
            onClick = {
                productosModel.agregarNuevoProducto(
                    nombre = nombreProducto,
                    precio = precioProducto,
                    cantidad = cantidadProducto.value
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = nombreProducto.isNotBlank() && precioProducto.isNotBlank() && cantidadProducto.value > 0 && addState !is AddProductoUiState.Loading
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

        //BuscarProducto()
    }
}