package com.example.sodappcomposse

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import kotlinx.serialization.Serializable
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.sodappcomposse.Componentes.BienvenidaScreen
import com.example.sodappcomposse.Componentes.LoginScreen

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
            BienvenidaScreen(parametro.nombre) {
                navController.navigate(Login) {
                    popUpTo<Login> {
                        inclusive = true
                    }
                }
            }
        }
/*
        composable<Ventas> {backStackEntry ->
            val parametro: Ventas = backStackEntry.toRoute()
            VentasScreen(
                parametro.nombre,
                navLogin = {
                    navController.navigate(Login) {
                        popUpTo<Login> {
                            inclusive = true
                        }
                    }
                }
            ) {
                navController.navigate(Bienvenida(nombre=parametro.nombre))
            }
        }*/

    }
}


