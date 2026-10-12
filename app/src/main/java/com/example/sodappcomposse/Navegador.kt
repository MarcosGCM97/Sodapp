package com.example.sodappcomposse

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
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

    // Fixed once the session state is known, so clearing the session later does not rebuild the graph
    val startDestination: Any = remember { if (isLoggedIn == true) Bienvenida("") else Login }

    val sessionExpired by loginViewModel.sessionExpired.collectAsState()
    LaunchedEffect(sessionExpired) {
        // sessionExpired only flips back to false on the next login, so several 401s navigate once
        if (sessionExpired) {
            loginViewModel.onSessionExpired()
            navController.navigateToLoginClearingBackStack()
        }
    }

    NavHost(
        navController = navController, 
        startDestination = startDestination
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
                    // Logout runs in the activity-scoped ViewModel so popping this screen cannot cancel it
                    loginViewModel.logout()
                    navController.navigateToLoginClearingBackStack()
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

// Pops every screen so their ViewModels (and the previous user's data) are cleared
private fun NavHostController.navigateToLoginClearingBackStack() {
    navigate(Login) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}
