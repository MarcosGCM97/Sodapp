package com.example.sodappcomposse

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import kotlinx.serialization.Serializable
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.toRoute
import com.example.sodappcomposse.Cliente.ClienteEditarScreen
import com.example.sodappcomposse.Cliente.DeudaScreen
import com.example.sodappcomposse.Componentes.AgendaScreen
import com.example.sodappcomposse.Componentes.BienvenidaScreen
import com.example.sodappcomposse.Componentes.LoginScreen
import com.example.sodappcomposse.Producto.ProductoEditarSccreen

@Serializable
object Login

@Serializable
data class Bienvenida(val nombre: String)
/*
@Serializable
object NavBottom*/

@Composable
fun Navigator(){
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Login){
        composable<Login> {
            LoginScreen { nombre ->
                navController.navigate(Bienvenida(nombre = nombre))
            }
        }

        composable<Bienvenida> {backStackEntry ->
            val parametro: Bienvenida = backStackEntry.toRoute()
            BienvenidaScreen(
                parametro.nombre, // Coincide con el primer parámetro
                navLogin = {                   // Coincide con el segundo parámetro (la lambda)
                    navController.navigate(Login) {
                        popUpTo<Login> {
                            inclusive = true
                        }
                    }
                },
                navController = navController
            )
        }

        composable(
            "deudaScreen/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry -> // Asegúrate de que esta ruta exista
            val clienteId = backStackEntry.arguments?.getString("id") // O Int si es un Int
            DeudaScreen(navController, clienteId)
        }

        composable(
            "productoEditarScreen/{nombre}",
            arguments = listOf(navArgument("nombre") { type = NavType.StringType })
        ) { backStackEntry ->
            val nombreProducto = backStackEntry.arguments?.getString("nombre")
            ProductoEditarSccreen(navController, nombreProducto = nombreProducto)
        }

        composable(
            "clienteEditarScreen/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) {
            val idCliente = it.arguments?.getString("id")
            ClienteEditarScreen(navController, idCliente = idCliente?.toInt())
        }

        composable("agendaScreen"){
            AgendaScreen(navController = navController)
        }
    }
}



