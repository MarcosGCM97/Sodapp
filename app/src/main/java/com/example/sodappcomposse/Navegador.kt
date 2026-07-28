package com.example.sodappcomposse

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.sodappcomposse.Cliente.ClienteEditarScreen
import com.example.sodappcomposse.Cliente.DeudaScreen
import com.example.sodappcomposse.Componentes.AgendaScreen
import com.example.sodappcomposse.Componentes.BienvenidaScreen
import com.example.sodappcomposse.Componentes.LoginScreen
import com.example.sodappcomposse.IngresoUsuario.LoginViewModel
import com.example.sodappcomposse.Producto.ProductoEditarScreen
import kotlinx.serialization.Serializable

@Serializable
object Login

@Serializable
data class Bienvenida(val nombre: String)

@Serializable
data class Deuda(val id: String)

@Serializable
data class ProductoEditar(val nombre: String)

@Serializable
data class ClienteEditar(val id: Int)

@Serializable
object Agenda

@Composable
fun Navigator(
    loginViewModel: LoginViewModel = hiltViewModel()
){
    val navController = rememberNavController()
    val isLoggedIn by loginViewModel.isLoggedIn.collectAsState(initial = null)

    if (isLoggedIn == null) {
        // Mostrar pantalla de carga mientras se lee el estado de sesión
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    NavHost(
        navController = navController, 
        startDestination = if (isLoggedIn == true) Bienvenida("") else Login
    ){
        composable<Login> {
            LoginScreen { nombre ->
                navController.navigate(Bienvenida(nombre = nombre)) {
                    popUpTo<Login> { inclusive = true }
                }
            }
        }

        composable<Bienvenida> { backStackEntry ->
            val route: Bienvenida = backStackEntry.toRoute()
            BienvenidaScreen(
                nombreUser = route.nombre,
                navLogin = {
                    navController.navigate(Login) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                navController = navController
            )
        }

        composable<Deuda> { backStackEntry ->
            val route: Deuda = backStackEntry.toRoute()
            DeudaScreen(navController, route.id)
        }

        composable<ProductoEditar> { backStackEntry ->
            val route: ProductoEditar = backStackEntry.toRoute()
            ProductoEditarScreen(navController, nombreProducto = route.nombre)
        }

        composable<ClienteEditar> { backStackEntry ->
            val route: ClienteEditar = backStackEntry.toRoute()
            ClienteEditarScreen(navController, idCliente = route.id)
        }

        composable<Agenda> {
            AgendaScreen(navController = navController)
        }
    }
}
