package com.example.sodappcomposse.Gastos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.sodappcomposse.Componentes.SelectBuscable
import com.example.sodappcomposse.Funciones.formatearPrecioEditable
import com.example.sodappcomposse.Producto.Producto
import com.example.sodappcomposse.Producto.parsearPrecio
import com.example.sodappcomposse.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val FORMATO_FECHA: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

// Same limit as gastos.php
const val GASTO_CANTIDAD_MAX = 100_000

/** Categories the user may choose: purchases and refills move stock, so they need stock.editar. */
fun categoriasDisponibles(puedeMoverStock: Boolean): List<CategoriaGasto> =
    CategoriaGasto.entries.filter { puedeMoverStock || !it.productoRequerido }

/** Products a category can use: a refill only fills returnable containers. */
fun productosDeCategoria(categoria: CategoriaGasto, productos: List<Producto>): List<Producto> =
    if (categoria == CategoriaGasto.RECARGA_ENVASES) productos.filter { it.esRetornable } else productos

/**
 * The expense the form describes, or null while it is incomplete: amount > 0; a refill needs a
 * product, a purchase may have one (without it, it is stored with no stock effect). With a
 * product it needs a quantity of 1..[GASTO_CANTIDAD_MAX], and a purchase of a returnable
 * product also needs whether the containers come full or empty.
 */
fun armarGasto(
    id: Int?,
    categoria: CategoriaGasto,
    montoTexto: String,
    fecha: LocalDate,
    descripcion: String,
    repartoId: Int?,
    producto: Producto?,
    cantidadTexto: String,
    estado: EstadoEnvase?
): NuevoGasto? {
    val monto = parsearPrecio(montoTexto)?.takeIf { it > 0 } ?: return null
    val base = NuevoGasto(id = id, categoria = categoria, monto = monto, fecha = fecha.toString(), descripcion = descripcion, repartoId = repartoId)
    if (!categoria.llevaProducto) return base
    // A purchase without product is a plain expense; a refill always needs the product it fills
    val elegido = producto ?: return base.takeUnless { categoria.productoRequerido }
    val cantidad = cantidadTexto.trim().toIntOrNull()?.takeIf { it in 1..GASTO_CANTIDAD_MAX } ?: return null
    if (categoria == CategoriaGasto.RECARGA_ENVASES) {
        if (!elegido.esRetornable) return null
        return base.copy(productoId = elegido.id, cantidad = cantidad)
    }
    if (elegido.esRetornable && estado == null) return null
    return base.copy(productoId = elegido.id, cantidad = cantidad, estado = estado.takeIf { elegido.esRetornable })
}

/**
 * Form to create or edit an expense. [inicial] null creates one (for [repartoId] when given);
 * [onEliminar] null hides the delete button.
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GastoDialog(
    inicial: Gasto?,
    productos: List<Producto>,
    puedeMoverStock: Boolean,
    enCurso: Boolean,
    onGuardar: (NuevoGasto) -> Unit,
    onDismiss: () -> Unit,
    repartoId: Int? = null,
    onEliminar: (() -> Unit)? = null
) {
    val categorias = categoriasDisponibles(puedeMoverStock)
    var categoria by rememberSaveable {
        mutableStateOf(CategoriaGasto.desdeCodigo(inicial?.categoria) ?: if (repartoId != null) CategoriaGasto.VEHICULO else categorias.first())
    }
    var montoTexto by rememberSaveable { mutableStateOf(inicial?.monto?.let(::formatearPrecioEditable).orEmpty()) }
    var fecha by remember { mutableStateOf(inicial?.fecha?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: LocalDate.now()) }
    var descripcion by rememberSaveable { mutableStateOf(inicial?.descripcion.orEmpty()) }
    var productoId by rememberSaveable { mutableStateOf(inicial?.productoId) }
    var cantidadTexto by rememberSaveable { mutableStateOf(inicial?.cantidad?.toString().orEmpty()) }
    var estado by rememberSaveable { mutableStateOf(EstadoEnvase.desdeCodigo(inicial?.estado)) }
    var elegirFecha by remember { mutableStateOf(false) }
    var confirmarEliminar by remember { mutableStateOf(false) }

    val opciones = productosDeCategoria(categoria, productos)
    val producto = opciones.firstOrNull { it.id == productoId }
    val gasto = armarGasto(inicial?.id, categoria, montoTexto, fecha, descripcion, inicial?.repartoId ?: repartoId, producto, cantidadTexto, estado)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(if (inicial == null) R.string.gasto_registrar else R.string.gasto_editar)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.gasto_categoria_label), style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    categorias.forEach { opcion ->
                        FilterChip(
                            selected = opcion == categoria,
                            onClick = { categoria = opcion },
                            label = { Text(stringResource(opcion.etiqueta)) }
                        )
                    }
                }
                OutlinedTextField(
                    value = montoTexto,
                    onValueChange = { montoTexto = it },
                    label = { Text(stringResource(R.string.gasto_monto_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                TextButton(onClick = { elegirFecha = true }) {
                    Text(stringResource(R.string.gasto_fecha_format, fecha.format(FORMATO_FECHA)))
                }
                if (categoria.llevaProducto && puedeMoverStock) {
                    if (opciones.isEmpty()) {
                        Text(stringResource(R.string.gasto_sin_productos_retornables))
                    } else {
                        SelectBuscable(
                            opciones = opciones,
                            etiqueta = { it.nombrePr },
                            onSeleccionar = { productoId = it.id },
                            label = stringResource(R.string.gasto_producto_label),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    producto?.let { Text(stringResource(R.string.gasto_producto_elegido_format, it.nombrePr)) }
                    if (!categoria.productoRequerido) {
                        if (producto == null) {
                            Text(stringResource(R.string.gasto_sin_producto_ayuda), style = MaterialTheme.typography.bodySmall)
                        } else {
                            TextButton(onClick = { productoId = null }) { Text(stringResource(R.string.gasto_quitar_producto)) }
                        }
                    }
                }
                // Quantity and state only matter once a product is chosen (a refill always has one)
                if (categoria.llevaProducto && puedeMoverStock && (producto != null || categoria.productoRequerido)) {
                    OutlinedTextField(
                        value = cantidadTexto,
                        onValueChange = { cantidadTexto = it.filter(Char::isDigit) },
                        label = { Text(stringResource(R.string.gasto_cantidad_label)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (categoria == CategoriaGasto.COMPRA_PRODUCTO && producto?.esRetornable == true) {
                        Text(stringResource(R.string.gasto_estado_label), style = MaterialTheme.typography.labelLarge)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            EstadoEnvase.entries.forEach { opcion ->
                                FilterChip(
                                    selected = opcion == estado,
                                    onClick = { estado = opcion },
                                    label = { Text(stringResource(opcion.etiqueta)) }
                                )
                            }
                        }
                    }
                    gasto?.let { EfectoStock(it, producto) }
                }
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it.take(255) },
                    label = { Text(stringResource(R.string.gasto_descripcion_label)) },
                    modifier = Modifier.fillMaxWidth()
                )
                onEliminar?.let {
                    TextButton(onClick = { confirmarEliminar = true }, enabled = !enCurso) {
                        Text(stringResource(R.string.gasto_eliminar), color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { gasto?.let(onGuardar) }, enabled = gasto != null && !enCurso) {
                Text(stringResource(R.string.gasto_guardar))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.gasto_cancelar)) } }
    )

    if (elegirFecha) {
        // DatePicker works in UTC millis
        val state = rememberDatePickerState(initialSelectedDateMillis = fecha.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
        DatePickerDialog(
            onDismissRequest = { elegirFecha = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { fecha = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate() }
                    elegirFecha = false
                }) { Text(stringResource(R.string.reporte_aceptar)) }
            },
            dismissButton = { TextButton(onClick = { elegirFecha = false }) { Text(stringResource(R.string.reporte_cancelar)) } }
        ) { DatePicker(state = state) }
    }

    if (confirmarEliminar && onEliminar != null) {
        AlertDialog(
            onDismissRequest = { confirmarEliminar = false },
            text = { Text(stringResource(R.string.gasto_eliminar_confirmar)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmarEliminar = false
                    onEliminar()
                }) { Text(stringResource(R.string.gasto_eliminar), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirmarEliminar = false }) { Text(stringResource(R.string.gasto_cancelar)) } }
        )
    }
}

// What saving will do to the stock, so the user sees it before confirming
@Composable
private fun EfectoStock(gasto: NuevoGasto, producto: Producto?) {
    val nombre = producto?.nombrePr ?: return
    val cantidad = gasto.cantidad ?: return
    val texto = when {
        gasto.categoria == CategoriaGasto.RECARGA_ENVASES -> stringResource(R.string.gasto_efecto_recarga, cantidad, nombre)
        gasto.estado == EstadoEnvase.VACIO -> stringResource(R.string.gasto_efecto_compra_vacio, cantidad, nombre)
        else -> stringResource(R.string.gasto_efecto_compra_lleno, cantidad, nombre)
    }
    Text(texto, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
}
