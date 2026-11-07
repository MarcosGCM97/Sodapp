package com.example.sodappcomposse.Componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sodappcomposse.ui.theme.ThemeViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import com.example.sodappcomposse.CLAVE_COBRAR_CLIENTE
import com.example.sodappcomposse.Bienvenida
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.example.sodappcomposse.R
import com.example.sodappcomposse.auth.LocalSessionPermissions
import com.example.sodappcomposse.auth.seccionInicial
import com.example.sodappcomposse.auth.seccionesVisibles
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

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
fun ContenidoClientes(
    modifier: Modifier = Modifier,
    navController: NavController,
    clienteACobrar: Int? = null,
    onClienteACobrarUsado: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Clientes(
            navController = navController,
            clienteACobrar = clienteACobrar,
            onClienteACobrarUsado = onClienteACobrarUsado
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
    navController: NavController,
    themeViewModel: ThemeViewModel = hiltViewModel()
){
    // Saveable so coming back from a detail screen (e.g. a client) returns to the same section
    var contenidoActual by rememberSaveable { mutableStateOf(ContenidoBienvenida.VENTAS) }
    // The client screen's "Cobrar" button leaves the client id here before popping back
    val handle = remember { runCatching { navController.getBackStackEntry<Bienvenida>() }.getOrNull()?.savedStateHandle }
    val clienteACobrar by (handle?.getStateFlow<Int?>(CLAVE_COBRAR_CLIENTE, null)
        ?: kotlinx.coroutines.flow.MutableStateFlow(null)).collectAsState()
    LaunchedEffect(clienteACobrar) {
        if (clienteACobrar != null) contenidoActual = ContenidoBienvenida.CLIENTES
    }
    // Only the sections the user's permissions allow; null when none is allowed
    val permisos = LocalSessionPermissions.current
    val secciones = seccionesVisibles(permisos)
    val seccion = seccionInicial(permisos, contenidoActual)
    val themeModeState = themeViewModel.themeMode.collectAsState()
    val themeMode = themeModeState.value
    val isSystemDark = isSystemInDarkTheme()
    val isDark = themeMode ?: isSystemDark

    Scaffold(
        topBar = {
            Encabezado(
                nombre = nombreUser,
                isDark = isDark,
                onToggleTheme = { themeViewModel.toggleTheme(isDark) },
                navLogin = navLogin
            )
        },
        bottomBar = {
            if (seccion != null) {
                NavBottom(
                    currentRoute = seccion,
                    secciones = secciones,
                    onContenidoSeleccionado = { nuevoContenido -> contenidoActual = nuevoContenido }
                )
            }
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                when (seccion){
                    null -> Text(
                        text = stringResource(R.string.sin_secciones_disponibles),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp)
                    )
                    ContenidoBienvenida.VENTAS -> ContenidoVentas(navController = navController)
                    ContenidoBienvenida.STOCK -> ContenidoStock(navController = navController)
                    ContenidoBienvenida.CLIENTES -> ContenidoClientes(
                        navController = navController,
                        clienteACobrar = clienteACobrar,
                        onClienteACobrarUsado = { handle?.set(CLAVE_COBRAR_CLIENTE, null) }
                    )
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
