package com.example.sodappcomposse

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "negro"
    }
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        /*val miVariable = "Hola Mundo desde Kotlin"
        val numero = 123

        // Mostrar un mensaje simple
        Log.e(TAG, "onCreate: La actividad se ha creado.")

        // Mostrar el valor de una variable
        Log.e(TAG, "onCreate: El valor de miVariable es: $miVariable")

        // Mostrar múltiples valores
        Log.e(TAG, "onCreate: Número = $numero, Texto = $miVariable")

        // Ejemplo de un log de información
        Log.i(TAG, "onCreate: Configuración inicial completada.")*/

        enableEdgeToEdge()
        setContent{
            Navigator()
        }
    }
}





