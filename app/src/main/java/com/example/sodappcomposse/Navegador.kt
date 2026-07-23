package com.example.sodappcomposse

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
    val isLoggedIn by loginViewModel.isLoggedIn.collectAsState(initial = false)

    NavHost(
        navController = navController, 
        startDestination = if (isLoggedIn) Bienvenida("") else Login
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
