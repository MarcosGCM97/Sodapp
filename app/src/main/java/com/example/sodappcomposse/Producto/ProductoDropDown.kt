package com.example.sodappcomposse.Producto

import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductoDropDown(
    productoModel: ProductoViewModel= viewModel()
){
    val context = LocalContext.current

    LaunchedEffect(Unit){
        productoModel.getProductos()
    }

    val productoUiState = productoModel.productoUiState
    val productos = productoModel.productos

    LaunchedEffect(productoUiState) {
        when(productoUiState){
            is ProductoUiState.Success -> {
                Toast.makeText(context, "Productos cargados: ${productos.size}", Toast.LENGTH_SHORT).show()
            }
            is ProductoUiState.Error -> {
                Toast.makeText(context, productoUiState.message, Toast.LENGTH_SHORT).show()
            }
            is ProductoUiState.Loading -> {
                //Toast.makeText(context, "Cargando productos...", Toast.LENGTH_SHORT).show()
            }
            ProductoUiState.Idle -> {
                //Toast.makeText(context, "Client state is Idle.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    var expandedProductos by remember { mutableStateOf(false) }

    val productosActuales: List<Producto> = when (productoUiState) {
        is ProductoUiState.Success -> productos
        else -> emptyList()
    }

    val productoParaVer = productoModel.productosParaDropDown

    ExposedDropdownMenuBox(
        expanded = expandedProductos,
        onExpandedChange = { expandedProductos = !expandedProductos },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = productoParaVer.value?.nombrePr ?: "Selecciona un producto",
            onValueChange = {},
            readOnly = true,
            label = { Text("Producto") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedProductos)
            },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expandedProductos,
            onDismissRequest = { expandedProductos = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            if (productoUiState is ProductoUiState.Loading) {
                DropdownMenuItem(
                    text = { Text("Cargando...") },
                    onClick = { },
                    enabled = false
                    )
            } else if (productosActuales.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("No hay productos disponibles") },
                    onClick = { },
                    enabled = false
                )
            } else {
                productosActuales.forEach { selectionOptionProducto ->
                    DropdownMenuItem(
                        text = { Text(selectionOptionProducto.nombrePr) },
                        onClick = {
                            productoParaVer.value = selectionOptionProducto
                            expandedProductos = false
                            Toast.makeText(context, "${selectionOptionProducto.nombrePr} seleccionada", Toast.LENGTH_SHORT).show()
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
    }
}