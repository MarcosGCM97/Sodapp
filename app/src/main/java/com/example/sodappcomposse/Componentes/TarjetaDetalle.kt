package com.example.sodappcomposse.Componentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.sodappcomposse.R
import com.example.sodappcomposse.ui.theme.GreenPrimario

/** Color of a status label: something to act on, something good, or plain information. */
enum class TonoEtiqueta { ALERTA, EXITO, NEUTRO }

// A client who owes money needs attention; credit is good news; zero debt is just "al día"
fun tonoSaldo(deuda: Double?): TonoEtiqueta = when {
    deuda == null || deuda == 0.0 -> TonoEtiqueta.NEUTRO
    deuda > 0 -> TonoEtiqueta.ALERTA
    else -> TonoEtiqueta.EXITO
}

fun tonoStock(stock: Int): TonoEtiqueta = if (stock <= 0) TonoEtiqueta.ALERTA else TonoEtiqueta.NEUTRO

data class DatoDetalle(val icono: ImageVector, val texto: String)

data class EtiquetaDetalle(val texto: String, val tono: TonoEtiqueta)

/** A labeled button; the [principal] one is filled, the rest are tonal. */
data class AccionDetalle(
    val texto: String,
    val icono: ImageVector,
    val principal: Boolean = false,
    val onClick: () -> Unit
)

/**
 * Detail card of a selected item (client, product): title with an optional highlighted value,
 * icon + text rows, colored status labels and labeled actions on one line. Edit and delete share
 * the last line at opposite ends, delete as red text so it is not tapped by mistake.
 * It knows nothing about the entity: each section maps its data.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TarjetaDetalle(
    titulo: String,
    modifier: Modifier = Modifier,
    destacado: String? = null,
    datos: List<DatoDetalle> = emptyList(),
    etiquetas: List<EtiquetaDetalle> = emptyList(),
    acciones: List<AccionDetalle> = emptyList(),
    onEditar: (() -> Unit)? = null,
    onEliminar: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                destacado?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            datos.forEach { dato ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = dato.icono,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(dato.texto, style = MaterialTheme.typography.bodyLarge)
                }
            }
            if (etiquetas.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    etiquetas.forEach { Etiqueta(it) }
                }
            }
            // Main actions share one line, each taking an equal part of the width
            if (acciones.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    acciones.forEach { accion ->
                        val boton = Modifier.weight(1f)
                        val relleno = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                        val contenido: @Composable () -> Unit = {
                            Icon(accion.icono, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(accion.texto, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        if (accion.principal) {
                            Button(onClick = accion.onClick, modifier = boton, contentPadding = relleno) { contenido() }
                        } else {
                            FilledTonalButton(onClick = accion.onClick, modifier = boton, contentPadding = relleno) { contenido() }
                        }
                    }
                }
            }
            // Edit on the left and delete on the right, far from each other
            if (onEditar != null || onEliminar != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    onEditar?.let {
                        OutlinedButton(onClick = it) {
                            Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(R.string.editar_label))
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    onEliminar?.let {
                        TextButton(onClick = it) {
                            Text(stringResource(R.string.eliminar_label), color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Etiqueta(etiqueta: EtiquetaDetalle) {
    val (fondo, texto) = when (etiqueta.tono) {
        TonoEtiqueta.ALERTA -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        // No "success" role in the color scheme: a light green wash keeps the theme text readable
        TonoEtiqueta.EXITO -> GreenPrimario.copy(alpha = 0.22f) to MaterialTheme.colorScheme.onSurface
        TonoEtiqueta.NEUTRO -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(color = fondo, contentColor = texto, shape = MaterialTheme.shapes.small) {
        Text(
            text = etiqueta.texto,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}
