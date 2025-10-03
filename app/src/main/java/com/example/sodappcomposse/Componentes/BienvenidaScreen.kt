package com.example.sodappcomposse.Componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.example.sodappcomposse.Caja.CajaScreen
import com.example.sodappcomposse.Cliente.Clientes
import com.example.sodappcomposse.ContenidoBienvenida
import com.example.sodappcomposse.Producto.Productos
import com.example.sodappcomposse.Ventas.Ventas

@Composable
fun ContenidoVentas(modifier: Modifier = Modifier, navController: NavController) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Ventas(navController = navController)
    }
}

@Composable
fun ContenidoClientes(modifier: Modifier = Modifier, navController: NavController) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Clientes(
            navController = navController
        )
    }
}

@Composable
fun ContenidoStock(modifier: Modifier = Modifier, navController: NavController) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Productos(
            navController = navController
        )
    }
}

@Composable
fun ContenidoCaja(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        CajaScreen()
    }
}

@Composable
fun BienvenidaScreen(
    nombreUser: String,
    navLogin: () -> Unit,
    navController: NavController
){
    var contenidoActual by remember { mutableStateOf(ContenidoBienvenida.VENTAS) }

    Scaffold(
        topBar = {
            Encabezado(nombre = nombreUser, navLogin = navLogin)
        },
        bottomBar = {
            NavBottom(
                currentRoute = contenidoActual,
                onContenidoSeleccionado = { nuevoContenido ->
                    when (nuevoContenido) {
                        ContenidoBienvenida.VENTAS -> contenidoActual = ContenidoBienvenida.VENTAS
                        ContenidoBienvenida.STOCK -> contenidoActual = ContenidoBienvenida.STOCK
                        ContenidoBienvenida.CLIENTES -> contenidoActual =
                            ContenidoBienvenida.CLIENTES
                        ContenidoBienvenida.CAJA -> contenidoActual = ContenidoBienvenida.CAJA
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
                    ContenidoBienvenida.VENTAS -> ContenidoVentas(navController = navController)
                    ContenidoBienvenida.STOCK -> ContenidoStock(navController = navController)
                    ContenidoBienvenida.CLIENTES -> ContenidoClientes(navController = navController)
                    ContenidoBienvenida.CAJA -> ContenidoCaja()
                }
            }
        }
    )
}

/*@Preview(showSystemUi = true)
@Composable
fun PreviewBienvenida(){
    BienvenidaScreen(nombreUser = "samuel", navLogin = { println("Boton Login") })
}*/
