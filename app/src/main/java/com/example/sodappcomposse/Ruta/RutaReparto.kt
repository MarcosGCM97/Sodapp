package com.example.sodappcomposse.Ruta

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sodappcomposse.Producto.ProductoUiState
import com.example.sodappcomposse.R

private const val PAQUETE_MAPS = "com.google.android.apps.maps"

/** Opens the leg in the Google Maps app; without it, in whatever handles the link (fewer waypoints there). */
fun abrirGoogleMaps(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    try {
        context.startActivity(Intent(intent).setPackage(PAQUETE_MAPS))
    } catch (e: ActivityNotFoundException) {
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, context.getString(R.string.maps_no_disponible), Toast.LENGTH_LONG).show()
        }
    }
}

/**
 * Route of the reparto: one button per Maps leg (opening the first one starts the trip),
 * the trip time and "Llegué a planta". [recarga] reloads the route whenever the reparto changes.
 */
@Composable
fun RutaReparto(
    repartoId: Int,
    recarga: Any?,
    onConfigurarPlanta: () -> Unit,
    viewModel: RutaViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val ruta by viewModel.ruta
    val tramos by viewModel.tramos
    val uiState by viewModel.uiState

    LaunchedEffect(repartoId, recarga) { viewModel.cargar(repartoId) }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is ProductoUiState.Success -> {
                Toast.makeText(context, context.getString(state.messageRes, *state.args), Toast.LENGTH_SHORT).show()
                viewModel.resetState()
            }
            is ProductoUiState.Error -> {
                Toast.makeText(context, context.getString(state.messageRes, *state.args), Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    val actual = ruta?.takeIf { it.repartoId == repartoId } ?: return

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.ruta_titulo), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                TextButton(onClick = onConfigurarPlanta) { Text(stringResource(R.string.configurar_planta)) }
            }

            if (actual.fin == null) {
                if (actual.planta == null) {
                    Text(stringResource(R.string.ruta_sin_planta), style = MaterialTheme.typography.bodySmall)
                }
                if (tramos.isEmpty()) {
                    Text(stringResource(R.string.ruta_sin_tramos))
                }
                tramos.forEach { tramo ->
                    OutlinedButton(
                        onClick = {
                            abrirGoogleMaps(context, tramo.url)
                            viewModel.iniciar(repartoId)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(etiquetaTramo(tramo))
                    }
                }
            }

            val minutos = duracionMinutos(actual.inicio, actual.fin)
            when {
                minutos != null -> Text(
                    stringResource(R.string.recorrido_duracion_format, minutos / 60, minutos % 60),
                    style = MaterialTheme.typography.titleSmall
                )
                actual.inicio != null -> Text(stringResource(R.string.recorrido_inicio_format, actual.inicio.drop(11).take(5)))
            }

            if (actual.inicio != null && actual.fin == null) {
                Button(
                    onClick = { viewModel.finalizar(repartoId) },
                    enabled = puedeFinalizar(actual) && uiState !is ProductoUiState.Loading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.llegue_a_planta))
                }
            }
        }
    }
}

@Composable
private fun etiquetaTramo(tramo: TramoRuta): String = when {
    tramo.desde == null || tramo.hasta == null -> stringResource(R.string.ruta_tramo_solo_planta_format, tramo.numero)
    tramo.terminaEnPlanta -> stringResource(R.string.ruta_tramo_planta_format, tramo.numero, tramo.desde, tramo.hasta)
    else -> stringResource(R.string.ruta_tramo_format, tramo.numero, tramo.desde, tramo.hasta)
}
