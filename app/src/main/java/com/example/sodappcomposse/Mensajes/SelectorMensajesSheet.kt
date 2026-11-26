package com.example.sodappcomposse.Mensajes

import android.content.res.Resources
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.sodappcomposse.Funciones.EnvaseMensaje
import com.example.sodappcomposse.Funciones.armarMensajeEnvases
import com.example.sodappcomposse.Funciones.armarMensajeSaldo
import com.example.sodappcomposse.Funciones.armarMensajeVenta
import com.example.sodappcomposse.Funciones.tieneEnvases
import com.example.sodappcomposse.Funciones.ultimaVenta
import com.example.sodappcomposse.R
import com.example.sodappcomposse.Ventas.Venta

/** A message the client screen can send; [mensaje] is null when there is nothing to send. */
data class OpcionMensaje(val tipo: TipoPlantilla, val mensaje: String?)

/**
 * The client screen messages, in order: the balance (always), the containers the client
 * keeps and the client's last sale. Each one uses the owner's template of its type, if any.
 */
fun opcionesMensajeCliente(
    resources: Resources,
    nombreCliente: String?,
    saldo: Double?,
    envases: List<EnvaseMensaje>,
    ventas: List<Venta>,
    nombreEmpresa: String?,
    plantillas: Map<TipoPlantilla, String>
): List<OpcionMensaje> {
    val ultima = ultimaVenta(ventas)
    return listOf(
        OpcionMensaje(
            TipoPlantilla.DEUDA,
            armarMensajeSaldo(resources, nombreCliente, saldo, nombreEmpresa, plantillas[TipoPlantilla.DEUDA])
        ),
        OpcionMensaje(
            TipoPlantilla.ENVASES,
            armarMensajeEnvases(resources, nombreCliente, envases, nombreEmpresa, plantillas[TipoPlantilla.ENVASES])
                .takeIf { tieneEnvases(envases) }
        ),
        OpcionMensaje(
            TipoPlantilla.VENTA,
            ultima?.let {
                armarMensajeVenta(resources, nombreCliente, it.productos, it.total, saldo, nombreEmpresa, plantillas[TipoPlantilla.VENTA])
            }
        ),
    )
}

/**
 * Bottom sheet to choose which WhatsApp message to send to the client, with a preview of
 * each. An option without message is shown disabled with the reason. Presentational.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorMensajesSheet(
    opciones: List<OpcionMensaje>,
    onEnviar: (String) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(stringResource(R.string.mensajes_selector_titulo), style = MaterialTheme.typography.titleMedium)
            opciones.forEach { opcion ->
                val mensaje = opcion.mensaje
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (mensaje != null) MaterialTheme.colorScheme.secondaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = mensaje != null) { mensaje?.let(onEnviar) }
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = stringResource(opcion.tipo.tituloRes),
                            style = MaterialTheme.typography.titleSmall,
                            color = if (mensaje != null) MaterialTheme.colorScheme.onSecondaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = mensaje ?: stringResource(sinDatosRes(opcion.tipo)),
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 6,
                            overflow = TextOverflow.Ellipsis,
                            color = if (mensaje != null) MaterialTheme.colorScheme.onSecondaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private fun sinDatosRes(tipo: TipoPlantilla): Int = when (tipo) {
    TipoPlantilla.DEUDA -> R.string.mensajes_selector_sin_datos
    TipoPlantilla.ENVASES -> R.string.mensajes_selector_sin_envases
    TipoPlantilla.VENTA -> R.string.mensajes_selector_sin_ventas
}
