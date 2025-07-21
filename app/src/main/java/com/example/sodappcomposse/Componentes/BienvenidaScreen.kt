package com.example.sodappcomposse.Componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.sodappcomposse.Cliente.Clientes
import com.example.sodappcomposse.ContenidoBienvenida
import com.example.sodappcomposse.Producto.Productos
import com.example.sodappcomposse.Ventas.Ventas

@Composable
fun ContenidoVentas(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Green.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
    ) {
        Ventas()
    }
}

@Composable
fun ContenidoClientes(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Yellow.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
    ) {
        Clientes()
    }
}

@Composable
fun ContenidoStock(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Magenta.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
    ) {
        Productos()
    }
}

@Composable
fun BienvenidaScreen(nombreUser: String, navLogin: () -> Unit){
    var contenidoActual by remember { mutableStateOf(ContenidoBienvenida.VENTAS) }

    Scaffold(
        topBar = {
            Encabezado(nombre = nombreUser, navLogin = navLogin)
        },
        bottomBar = {
            NavBottom(
                onContenidoSeleccionado = { nuevoContenido ->
                    when (nuevoContenido) {
                        ContenidoBienvenida.VENTAS -> contenidoActual = ContenidoBienvenida.VENTAS
                        ContenidoBienvenida.STOCK -> contenidoActual = ContenidoBienvenida.STOCK
                        ContenidoBienvenida.CLIENTES -> contenidoActual =
                            ContenidoBienvenida.CLIENTES
                    }
                }
            )
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                when (contenidoActual){
                    ContenidoBienvenida.VENTAS -> ContenidoVentas()
                    ContenidoBienvenida.STOCK -> ContenidoStock()
                    ContenidoBienvenida.CLIENTES -> ContenidoClientes()
                }
            }
        }
    )
}

@Preview(showSystemUi = true)
@Composable
fun PreviewBienvenida(){
    BienvenidaScreen(nombreUser = "samuel", navLogin = { println("Boton Login") })
}
