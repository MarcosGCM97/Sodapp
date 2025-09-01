package com.example.sodappcomposse.Producto

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sodappcomposse.Componentes.ScreenWithBackButtonWrapper

@SuppressLint("UnrememberedMutableState")
@Composable
fun ProductoEditarSccreen(
    navController: NavController,
    nombreProducto: String?,
    productoModel: ProductoViewModel = viewModel(),
){
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        productoModel.getProductos()
    }

    var nombrePr by remember { mutableStateOf("") }
    var precioPr by remember { mutableStateOf("") }
    var cantidadPr by remember { mutableStateOf("") }

    val productoElegido = productoModel.productos.find { it.nombrePr == nombreProducto }

    ScreenWithBackButtonWrapper(
        navController = navController,
        title = "Editar Producto"
    ){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = nombreProducto.toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = precioPr,
                onValueChange = { precioPr = it },
                label = { Text("Precio = ${productoElegido?.precioUni}") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                singleLine = true
            )
            OutlinedTextField(
                value = cantidadPr,
                onValueChange = { cantidadPr = it },
                label = { Text("Cantidad = ${productoElegido?.stock}") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                modifier = Modifier
                    .width(200.dp),
                onClick = {
                    var prodEditado = Producto(
                        nombrePr = nombreProducto.toString(),
                        precioUni = if (precioPr.isNotBlank()) precioPr else productoElegido?.precioUni.toString(),
                        stock = if (cantidadPr.isNotBlank()) cantidadPr else productoElegido?.stock.toString()
                    )
                    productoModel.editarProducto(prodEditado)
                    navController.popBackStack()
                }
            ) {
                Text("Guardar")
            }
        }
    }
}