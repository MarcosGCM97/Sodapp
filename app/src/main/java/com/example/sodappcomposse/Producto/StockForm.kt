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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

// =============================================
// TAB INVENTARIO (nueva vista de control de stock)
// =============================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabInventario(
    navController: NavController,
    productoModel: ProductoViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val productos = productoModel.productos
    val uiState = productoModel.productoUiState

    var filtroNombre by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        productoModel.getProductos()
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is ProductoUiState.Error -> {
                Toast.makeText(context, uiState.message, Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    val productosFiltrados = if (filtroNombre.isBlank()) {
        productos
    } else {
        productos.filter {
            it.nombrePr.contains(filtroNombre, ignoreCase = true)
        }
    }

    val cantidadStockBajo = productos.count {
        (it.stock.toIntOrNull() ?: 0) < ProductoViewModel.STOCK_BAJO_UMBRAL
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Campo de búsqueda
        OutlinedTextField(
            value = filtroNombre,
            onValueChange = { filtroNombre = it },
            label = { Text("Buscar producto...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Resumen
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${productos.size} productos total",
                style = MaterialTheme.typography.bodyMedium
            )
            if (cantidadStockBajo > 0) {
                Text(
                    text = "$cantidadStockBajo con stock bajo",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Estado de carga
        when (uiState) {
            is ProductoUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is ProductoUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Error al cargar productos",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            else -> {
                if (productosFiltrados.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No hay productos para mostrar")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(productosFiltrados) { producto ->
                            CardProductoInventario(
                                producto = producto,
                                onAjustarStock = { nuevaCantidad ->
                                    productoModel.ajustarStock(producto, nuevaCantidad)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CardProductoInventario(
    producto: ProductoCompleto,
    onAjustarStock: (Int) -> Unit
) {
    val stockActual = producto.stock.toIntOrNull() ?: 0

    val colorStock = when {
        stockActual < ProductoViewModel.STOCK_BAJO_UMBRAL -> MaterialTheme.colorScheme.error
        stockActual < ProductoViewModel.STOCK_MEDIO_UMBRAL -> Color(0xFFFF9800)
        else -> Color(0xFF4CAF50)
    }

    var cantidadLocal by remember(producto.id, producto.stock) {
        mutableIntStateOf(stockActual)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Nombre del producto
            Text(
                text = producto.nombrePr,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Precio
            Text(
                text = "Precio: $${producto.precioUni}",
                style = MaterialTheme.typography.bodyMedium
            )

            // Stock con color
            Text(
                text = "Stock: $stockActual",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = colorStock
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Controles de ajuste de stock
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(
                    onClick = {
                        if (cantidadLocal > 0) {
                            cantidadLocal--
                            onAjustarStock(cantidadLocal)
                        }
                    },
                    enabled = cantidadLocal > 0
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = "Disminuir stock")
                }

                OutlinedTextField(
                    value = cantidadLocal.toString(),
                    onValueChange = { newValue ->
                        val nuevaCantidad = newValue.toIntOrNull()
                        if (newValue.isEmpty()) {
                            cantidadLocal = 0
                        } else if (nuevaCantidad != null && nuevaCantidad >= 0) {
                            cantidadLocal = nuevaCantidad
                        }
                    },
                    modifier = Modifier.width(80.dp),
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                IconButton(
                    onClick = {
                        cantidadLocal++
                        onAjustarStock(cantidadLocal)
                    }
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Aumentar stock")
                }
            }
        }
    }
}