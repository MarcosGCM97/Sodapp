package com.example.sodappcomposse.Producto

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.ui.res.stringResource
import com.example.sodappcomposse.R
import com.example.sodappcomposse.ProductoEditar
import com.example.sodappcomposse.Cliente.AddClienteForm
import com.example.sodappcomposse.Cliente.ClientesViewModel
import com.example.sodappcomposse.Ventas.Ventas

@Composable
fun Productos(
    navController: NavController
){
    val TAG = "Productos"
    val scrollState = rememberScrollState()

    var tabSeleccionado by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        stringResource(R.string.tab_productos),
        stringResource(R.string.tab_inventario)
    )

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = tabSeleccionado) {
            tabs.forEachIndexed { index, titulo ->
                Tab(
                    selected = tabSeleccionado == index,
                    onClick = { tabSeleccionado = index },
                    text = { Text(titulo) }
                )
            }
        }

        when (tabSeleccionado) {
            0 -> TabProductos(navController = navController)
            1 -> TabInventario(navController = navController)
        }
    }
}

// =============================================
// TAB PRODUCTOS (contenido original sin cambios)
// =============================================
@Composable
fun TabProductos(navController: NavController) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            AddProductoForm(
                navController = navController
            )
        }
    }
}



@Preview(showSystemUi = true)
@Composable
fun PreviewProductosScreen(){
    Productos(navController = NavController(LocalContext.current))
}