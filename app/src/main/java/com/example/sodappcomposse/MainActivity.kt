package com.example.sodappcomposse

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.example.sodappcomposse.Producto.ProductoViewModel
import com.example.sodappcomposse.Ventas.VentasViewModel
import com.example.sodappcomposse.Cliente.ClientesViewModel
import com.example.sodappcomposse.ui.theme.SodAppComposseTheme

class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "negro"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent{
            SodAppComposseTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background // El fondo de Surface también usará el tema
                ) {
                    Navigator()
                }
            }
        }
    }
}





