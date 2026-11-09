package com.example.sodappcomposse.Ruta

import com.example.sodappcomposse.auth.LocalSessionPermissions
import com.example.sodappcomposse.auth.Permiso

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.sodappcomposse.Componentes.ScreenWithBackButtonWrapper
import com.example.sodappcomposse.Componentes.UbicacionEditor
import com.example.sodappcomposse.Producto.ProductoUiState
import com.example.sodappcomposse.R

/** Plant address: start and end of every route. Coordinates come from "Ubicar por dirección" or the GPS. */
@Composable
fun PlantaScreen(navController: NavController, viewModel: RutaViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val planta by viewModel.planta
    // Saving the plant address is a write (repartos.gestionar)
    val gestionar = LocalSessionPermissions.current.tiene(Permiso.REPARTOS_GESTIONAR)
    val uiState by viewModel.uiState
    var direccion by remember { mutableStateOf("") }
    var localidad by remember { mutableStateOf("") }
    var coordenadas by remember { mutableStateOf<Pair<Double, Double>?>(null) }

    LaunchedEffect(Unit) { viewModel.cargarPlanta() }

    LaunchedEffect(planta) {
        planta?.let {
            direccion = it.direccion
            localidad = it.localidad
            coordenadas = if (it.lat != null && it.lng != null) it.lat to it.lng else null
        }
    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is ProductoUiState.Success -> {
                Toast.makeText(context, context.getString(state.messageRes, *state.args), Toast.LENGTH_SHORT).show()
                viewModel.resetState()
                navController.popBackStack()
            }
            is ProductoUiState.Error -> {
                Toast.makeText(context, context.getString(state.messageRes, *state.args), Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    ScreenWithBackButtonWrapper(navController = navController, title = stringResource(R.string.planta_titulo)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(stringResource(R.string.planta_descripcion))
            OutlinedTextField(
                value = direccion,
                onValueChange = { direccion = it },
                label = { Text(stringResource(R.string.planta_direccion_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = localidad,
                onValueChange = { localidad = it },
                label = { Text(stringResource(R.string.localidad_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            UbicacionEditor(
                direccion = direccion,
                localidad = localidad,
                coordenadas = coordenadas,
                onCambiar = { coordenadas = it }
            )
            val guardando = uiState is ProductoUiState.Loading
            if (gestionar) Button(
                onClick = { viewModel.guardarPlanta(direccion, localidad, coordenadas?.first, coordenadas?.second) },
                enabled = direccion.isNotBlank() && localidad.isNotBlank() && !guardando,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (guardando) CircularProgressIndicator() else Text(stringResource(R.string.guardar_label))
            }
        }
    }
}
