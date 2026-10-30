package com.example.sodappcomposse.Cliente

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sodappcomposse.Funciones.formatearPrecio
import com.example.sodappcomposse.R
import com.example.sodappcomposse.ui.theme.GreenPrimario
import com.google.gson.annotations.SerializedName

// Client with a pending balance, as listed by GET deudaCliente.php
data class Deudor(
    @SerializedName("cl_ide")
    val id: Int,
    @SerializedName("cl_nom")
    val nombre: String,
    @SerializedName("cl_deb")
    val saldo: Double
)

data class DeudoresResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("deudores")
    val deudores: List<Deudor> = emptyList()
)

fun filtrarDeudores(deudores: List<Deudor>, busqueda: String): List<Deudor> {
    val texto = busqueda.trim()
    if (texto.isEmpty()) return deudores
    return deudores.filter { it.nombre.contains(texto, ignoreCase = true) }
}

// Amount typed into the form by "Cobrar todo": a plain number the amount field can parse back
fun montoCobrarTodo(saldo: Double): String =
    if (saldo % 1.0 == 0.0) saldo.toLong().toString() else saldo.toString()

/**
 * Clientes > Cobros: debtors sorted by balance with a search, and the payment form of the selected one.
 * [clienteInicial] preselects a client when the user arrives from the client screen's "Cobrar" button.
 */
@Composable
fun CobrosTab(
    clienteInicial: Int?,
    onClienteInicialUsado: () -> Unit,
    deudaModel: DeudaViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var busqueda by rememberSaveable { mutableStateOf("") }
    var seleccionadoId by rememberSaveable { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) {
        deudaModel.cargarFormasPago()
        deudaModel.cargarDeudores()
    }
    LaunchedEffect(clienteInicial) {
        if (clienteInicial != null) {
            seleccionadoId = clienteInicial
            onClienteInicialUsado()
        }
    }

    val uiState = deudaModel.uiState.value
    LaunchedEffect(uiState) {
        when (uiState) {
            is DeudaUiState.Success -> {
                Toast.makeText(context, context.getString(R.string.pago_registrado_success), Toast.LENGTH_SHORT).show()
                // Keep the form locked until the refreshed balances are on screen
                deudaModel.cargarDeudores()
                deudaModel.resetState()
            }
            is DeudaUiState.Error -> {
                Toast.makeText(context, context.getString(uiState.messageRes, *uiState.args), Toast.LENGTH_SHORT).show()
                deudaModel.resetState()
            }
            else -> Unit
        }
    }

    val deudores = deudaModel.deudores.value
    // A client brought to zero leaves the list, so the form closes by itself
    val seleccionado = deudores.find { it.id == seleccionadoId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (seleccionado != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(seleccionado.nombre, style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.saldo_pendiente_format, formatearPrecio(seleccionado.saldo)))
                    FormularioCobro(
                        clienteId = seleccionado.id,
                        saldo = seleccionado.saldo,
                        formas = deudaModel.formasPago.value,
                        enCurso = deudaModel.isPagoEnCurso,
                        onCobrar = { monto, formaPago -> deudaModel.pagarDeudaCliente(seleccionado.id, monto, formaPago) }
                    )
                    TextButton(onClick = { seleccionadoId = null }) {
                        Text(stringResource(R.string.cambiar_cliente))
                    }
                }
            }
        } else {
            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                label = { Text(stringResource(R.string.buscar_cliente_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            val filtrados = filtrarDeudores(deudores, busqueda)
            if (filtrados.isEmpty()) {
                Text(
                    stringResource(R.string.sin_clientes_saldo_pendiente),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    textAlign = TextAlign.Center
                )
            }
            filtrados.forEach { deudor ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { seleccionadoId = deudor.id },
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(deudor.nombre, modifier = Modifier.weight(1f))
                        Text(
                            stringResource(R.string.saldo_pendiente_format, formatearPrecio(deudor.saldo)),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

// Payment form: method (no default), amount, "Cobrar todo" and Saldar
@Composable
private fun FormularioCobro(
    clienteId: Int,
    saldo: Double,
    formas: List<FormaPago>,
    enCurso: Boolean,
    onCobrar: (monto: Double, formaPago: Int) -> Unit
) {
    val context = LocalContext.current
    // Changing client starts an empty form
    var monto by remember(clienteId) { mutableStateOf("") }
    var formaPago by remember(clienteId) { mutableStateOf<Int?>(null) }

    Text(stringResource(R.string.forma_pago_label), style = MaterialTheme.typography.labelLarge)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        formas.forEach { forma ->
            FilterChip(
                selected = formaPago == forma.id,
                onClick = { formaPago = forma.id },
                label = { Text(forma.nombre) }
            )
        }
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = monto,
            onValueChange = { monto = it },
            label = { Text(stringResource(R.string.ingrese_monto_label)) },
            modifier = Modifier.weight(1f),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(modifier = Modifier.size(8.dp))
        OutlinedButton(onClick = { monto = montoCobrarTodo(saldo) }) {
            Text(stringResource(R.string.cobrar_todo))
        }
    }
    Spacer(modifier = Modifier.height(4.dp))
    Button(
        onClick = {
            val valor = monto.toDoubleOrNull()
            val forma = formaPago
            when {
                valor == null || valor <= 0 ->
                    Toast.makeText(context, context.getString(R.string.monto_invalido_msg), Toast.LENGTH_SHORT).show()
                valor > saldo ->
                    Toast.makeText(context, context.getString(R.string.monto_supera_deuda_msg, formatearPrecio(valor), formatearPrecio(saldo)), Toast.LENGTH_SHORT).show()
                forma == null ->
                    Toast.makeText(context, context.getString(R.string.forma_pago_requerida_msg), Toast.LENGTH_SHORT).show()
                else -> {
                    onCobrar(valor, forma)
                    monto = ""
                    formaPago = null
                }
            }
        },
        enabled = !enCurso,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimario, contentColor = Color.White)
    ) {
        if (enCurso) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
        } else {
            Text(stringResource(R.string.saldar_label))
        }
    }
}
