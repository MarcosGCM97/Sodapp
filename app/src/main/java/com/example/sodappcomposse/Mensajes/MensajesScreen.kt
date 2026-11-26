package com.example.sodappcomposse.Mensajes

import android.content.res.Resources
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.sodappcomposse.Componentes.ScreenWithBackButtonWrapper
import com.example.sodappcomposse.Funciones.EnvaseMensaje
import com.example.sodappcomposse.Funciones.ProductoMensaje
import com.example.sodappcomposse.Funciones.armarMensajeEnvases
import com.example.sodappcomposse.Funciones.armarMensajeSaldo
import com.example.sodappcomposse.Funciones.armarMensajeVenta
import com.example.sodappcomposse.Funciones.plantillaBase
import com.example.sodappcomposse.R
import com.example.sodappcomposse.Ventas.VentasViewModel
import com.example.sodappcomposse.auth.LocalSessionPermissions
import com.example.sodappcomposse.auth.puedeAdministrar

/**
 * Owner-only editor of the WhatsApp templates: a chip per message type, the text with its
 * variables, a preview with sample data, save and restore the default message.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MensajesScreen(
    navController: NavController,
    viewModel: MensajesViewModel = hiltViewModel(),
    ventasViewModel: VentasViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val resources = context.resources
    val uiState by viewModel.uiState
    val plantillas by viewModel.plantillas
    val nombreEmpresa by ventasViewModel.nombreEmpresa.collectAsState(initial = null)
    val esDueno = puedeAdministrar(LocalSessionPermissions.current)

    var tipo by rememberSaveable { mutableStateOf(TipoPlantilla.DEUDA) }
    var texto by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue("")) }
    val guardada = plantillas[tipo]

    LaunchedEffect(esDueno) {
        if (esDueno) viewModel.cargar()
    }

    // Show the saved template of the type, or the default message as a starting point
    LaunchedEffect(tipo, guardada) {
        val inicial = guardada ?: plantillaBase(resources, tipo)
        texto = TextFieldValue(inicial, TextRange(inicial.length))
    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is MensajesUiState.Success -> {
                Toast.makeText(context, context.getString(state.messageRes), Toast.LENGTH_SHORT).show()
                viewModel.resetState()
            }
            is MensajesUiState.Error -> {
                // Backend validation (400), the 403 message or a network error
                Toast.makeText(context, context.getString(state.messageRes, *state.args), Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> Unit
        }
    }

    ScreenWithBackButtonWrapper(
        navController = navController,
        title = stringResource(R.string.mensajes_titulo),
        mostrarMensajes = false
    ) {
        if (!esDueno) {
            Text(stringResource(R.string.error_sin_permiso))
            return@ScreenWithBackButtonWrapper
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(stringResource(R.string.mensajes_ayuda), style = MaterialTheme.typography.bodyMedium)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TipoPlantilla.entries.forEach { opcion ->
                    FilterChip(
                        selected = tipo == opcion,
                        onClick = { tipo = opcion },
                        label = { Text(stringResource(opcion.tituloRes)) }
                    )
                }
            }

            Text(
                text = stringResource(if (guardada != null) R.string.mensajes_personalizado else R.string.mensajes_por_defecto),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = texto,
                onValueChange = { if (it.text.length <= MAX_TEXTO) texto = it },
                label = { Text(stringResource(R.string.mensajes_texto_label)) },
                supportingText = { Text(stringResource(R.string.mensajes_contador_format, texto.text.length, MAX_TEXTO)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 160.dp)
            )

            Text(stringResource(R.string.mensajes_variables_label), style = MaterialTheme.typography.bodySmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                tipo.variables.forEach { variable ->
                    AssistChip(
                        onClick = { texto = insertarVariable(texto, variable) },
                        label = { Text("{$variable}") }
                    )
                }
            }

            Text(stringResource(R.string.mensajes_vista_previa), style = MaterialTheme.typography.titleSmall)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = vistaPrevia(resources, tipo, texto.text, nombreEmpresa),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Button(
                onClick = { viewModel.guardar(tipo, texto.text) },
                enabled = uiState !is MensajesUiState.Loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.guardar))
            }
            OutlinedButton(
                onClick = { viewModel.restaurar(tipo) },
                enabled = guardada != null && uiState !is MensajesUiState.Loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.mensajes_restaurar))
            }
        }
    }
}

/** Puts `{variable}` where the cursor is (replacing a selection) and moves the cursor after it. */
internal fun insertarVariable(valor: TextFieldValue, variable: String): TextFieldValue {
    val insertado = "{$variable}"
    if (valor.text.length + insertado.length > MAX_TEXTO) return valor
    val inicio = minOf(valor.selection.start, valor.selection.end)
    val fin = maxOf(valor.selection.start, valor.selection.end)
    val texto = valor.text.substring(0, inicio) + insertado + valor.text.substring(fin)
    return TextFieldValue(texto, TextRange(inicio + insertado.length))
}

// The message of [tipo] for a sample client, as WhatsApp would show it
private fun vistaPrevia(resources: Resources, tipo: TipoPlantilla, texto: String, nombreEmpresa: String?): String {
    val cliente = resources.getString(R.string.mensajes_ejemplo_cliente)
    val producto = resources.getString(R.string.mensajes_ejemplo_producto)
    return when (tipo) {
        TipoPlantilla.DEUDA -> armarMensajeSaldo(resources, cliente, 3000.0, nombreEmpresa, plantilla = texto)
        TipoPlantilla.ENVASES -> armarMensajeEnvases(resources, cliente, listOf(EnvaseMensaje(2, producto)), nombreEmpresa, plantilla = texto)
        TipoPlantilla.VENTA -> armarMensajeVenta(
            resources, cliente, listOf(ProductoMensaje(2, producto, 4500.0)), 9000.0, 3000.0, nombreEmpresa, plantilla = texto
        )
    }
}

// Same limit as mensajes.php
private const val MAX_TEXTO = 1000
