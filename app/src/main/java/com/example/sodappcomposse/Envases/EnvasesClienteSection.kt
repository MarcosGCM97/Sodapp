package com.example.sodappcomposse.Envases

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sodappcomposse.Producto.ProductoUiState
import com.example.sodappcomposse.R

/**
 * "Envases" section of the client detail: the client's container balance per returnable
 * product, with an edit action to set it. Reloads whenever [recargarKey] changes
 * (for example after a sale of this client is registered or deleted).
 */
@Composable
fun EnvasesClienteSection(
    clienteId: Int,
    recargarKey: Any?,
    modifier: Modifier = Modifier,
    envasesModel: EnvasesClienteViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val envases = envasesModel.envases.value
    val uiState = envasesModel.uiState.value

    var envaseEnEdicion by remember { mutableStateOf<EnvaseCliente?>(null) }

    LaunchedEffect(clienteId, recargarKey) {
        envasesModel.cargarEnvases(clienteId)
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is ProductoUiState.Success -> {
                Toast.makeText(context, context.getString(uiState.messageRes, *uiState.args), Toast.LENGTH_SHORT).show()
                envasesModel.resetState()
            }
            is ProductoUiState.Error -> {
                Toast.makeText(context, context.getString(uiState.messageRes, *uiState.args), Toast.LENGTH_LONG).show()
                envasesModel.resetState()
            }
            else -> Unit
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(stringResource(R.string.envases_titulo), style = MaterialTheme.typography.titleMedium)
            if (envases.isEmpty()) {
                Text(stringResource(R.string.envases_sin_retornables), style = MaterialTheme.typography.bodyMedium)
            } else {
                envases.forEach { envase ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.envases_cliente_item_format, envase.nombreProducto, envase.cantidad),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { envaseEnEdicion = envase }) {
                            Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.editar_desc))
                        }
                    }
                }
            }
        }
    }

    envaseEnEdicion?.let { envase ->
        EditarSaldoEnvasesDialog(
            envase = envase,
            onDismiss = { envaseEnEdicion = null },
            onGuardar = { cantidad ->
                envasesModel.guardarSaldo(clienteId, envase.productoId, cantidad)
                envaseEnEdicion = null
            }
        )
    }
}

@Composable
private fun EditarSaldoEnvasesDialog(
    envase: EnvaseCliente,
    onDismiss: () -> Unit,
    onGuardar: (Int) -> Unit
) {
    var cantidad by remember(envase) { mutableStateOf(envase.cantidad.coerceAtLeast(0).toString()) }
    val cantidadValida = cantidad.toIntOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.editar_saldo_envases_titulo, envase.nombreProducto)) },
        text = {
            OutlinedTextField(
                value = cantidad,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                        cantidad = newValue
                    }
                },
                label = { Text(stringResource(R.string.cantidad_label)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        },
        confirmButton = {
            TextButton(
                onClick = { cantidadValida?.let(onGuardar) },
                enabled = cantidadValida != null
            ) {
                Text(stringResource(R.string.guardar_label))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancelar))
            }
        }
    )
}
