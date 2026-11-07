package com.example.sodappcomposse.Cliente

import com.example.sodappcomposse.auth.LocalSessionPermissions
import com.example.sodappcomposse.auth.Permiso
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.compose.ui.res.stringResource
import com.example.sodappcomposse.R
import com.example.sodappcomposse.Funciones.formatearPrecio
import com.example.sodappcomposse.Funciones.textoSaldoCliente
import com.example.sodappcomposse.Funciones.armarMensajeSaldo
import com.example.sodappcomposse.Deuda
import com.example.sodappcomposse.Bienvenida
import com.example.sodappcomposse.CLAVE_COBRAR_CLIENTE
import com.example.sodappcomposse.Componentes.CardWpp
import com.example.sodappcomposse.Componentes.ScreenWithBackButtonWrapper
import com.example.sodappcomposse.Producto.ProductoViewModel
import com.example.sodappcomposse.Ventas.Venta
import com.example.sodappcomposse.Ventas.VentasViewModel
import com.example.sodappcomposse.Ventas.VentasUiState
import com.example.sodappcomposse.Envases.EnvasesClienteSection
import com.example.sodappcomposse.Cliente.DeudaViewModel
import com.example.sodappcomposse.ui.theme.BluePrimario
import com.example.sodappcomposse.ui.theme.GreenPrimario


@SuppressLint("UnrememberedMutableState", "StateFlowValueCalledInComposition", "DefaultLocale")
@Composable
fun DeudaScreen(
    navController: NavController,
    clienteId: String?,
    clienteModel: ClientesViewModel = hiltViewModel(),
    ventaModel: VentasViewModel = hiltViewModel(),
    productosModel: ProductoViewModel = hiltViewModel(),
    deudaModel: DeudaViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val scrollState = rememberScrollState()
    val permisos = LocalSessionPermissions.current
    // Payments need cobros.ver to be listed and cobros.cargar to be registered
    val puedeVerCobros = permisos.tiene(Permiso.COBROS_VER)
    val puedeCobrar = puedeVerCobros && permisos.tiene(Permiso.COBROS_CARGAR)
    val puedeEliminarVenta = permisos.tiene(Permiso.VENTAS_ELIMINAR)

    //val editarEstados = remember { mutableStateMapOf<Int, Boolean>() }

    LaunchedEffect(key1 = Unit) {//, key2 = editarEstados
        if (clienteId != null && clienteId != "{clienteId}") {
            ventaModel.getVentasByClienteId(clienteId)
            clienteModel.getClienteById(clienteId)
            productosModel.getProductos()
            if (puedeVerCobros) clienteId.toIntOrNull()?.let { deudaModel.cargarPagos(it) }
            Log.d("DeudaScreen", "clienteId recibido: $clienteId")
        } else {
            Log.e("DeudaScreen", "clienteId es nulo o inválido: $clienteId")
            navController.popBackStack()
        }
    }
    // Refresh the client only after the debt update has finished on the backend
    val nombreEmpresa by ventaModel.nombreEmpresa.collectAsState(initial = null)
    val deudaUiState = deudaModel.uiState.value
    LaunchedEffect(deudaUiState) {
        when (deudaUiState) {
            is DeudaUiState.Success -> {
                // Keep the pay button locked until the refreshed debt is on screen
                clienteModel.recargarClienteById(clienteId.toString())
                if (puedeVerCobros) clienteId?.toIntOrNull()?.let { deudaModel.cargarPagos(it) }
                deudaModel.resetState()
            }
            is DeudaUiState.Error -> {
                Toast.makeText(context, context.getString(deudaUiState.messageRes, *deudaUiState.args), Toast.LENGTH_SHORT).show()
                deudaModel.resetState()
            }
            else -> Unit
        }
    }

    val cliente = clienteModel.clienteById // Manteniendo tu estructura actual por ahora

    val ventasDelCliente = ventaModel.ventasPorClienteId
    // Sale waiting for the user to confirm its deletion
    var ventaAEliminar by remember { mutableStateOf<Venta?>(null) }


    // 0 = Compras, 1 = Pagos
    var tabCliente by remember { mutableStateOf(0) }

    //var nuevaCantidad by remember { mutableStateOf(0) }

    val cantidadesEnEdicion = remember { mutableStateMapOf<Int, String>() }

    // Usamos el nuevo wrapper
    ScreenWithBackButtonWrapper(
        navController = navController,
        title = stringResource(R.string.detalles_deuda_titulo)
    ) {
        Column(modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState) // <--- AÑADIDO PARA EL SCROLL
            .padding(16.dp)
        ) {
            if (cliente.value != null) { // Importante: Chequear si el cliente está cargado
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(stringResource(R.string.detalles_cliente_titulo), style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(R.string.nombre_format, cliente.value?.nombreCl ?: ""))
                        Text(stringResource(R.string.direccion_format, cliente.value?.direccionCl ?: ""))
                        Text(stringResource(R.string.telefono_format, cliente.value?.numTelCl ?: ""))
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(stringResource(R.string.saldo_ventas_label), style = MaterialTheme.typography.titleMedium)
                        Text(textoSaldoCliente(context.resources, cliente.value?.deudaCl))

                        Spacer(modifier = Modifier.height(8.dp))

                        if (puedeCobrar) {
                            Button(
                                onClick = { clienteId?.toIntOrNull()?.let { irACobrarCliente(navController, it) } },
                                modifier = Modifier.fillMaxWidth(),
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                    containerColor = GreenPrimario,
                                    contentColor = Color.White
                                )
                            ) {
                                Text(stringResource(R.string.cobrar_label))
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                                //.padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Botón de WhatsApp
                            CardWpp(context, cliente.value, armarMensajeSaldo(context.resources, cliente.value?.nombreCl, cliente.value?.deudaCl, nombreEmpresa))

                            // Botón de Llamada
                            Card(
                                modifier = Modifier
                                    .width(100.dp)
                                    .padding(start = 8.dp)
                                    .clickable {
                                        val numTel = cliente.value?.numTelCl.toString()
                                        openDialer(context, numTel)
                                    },
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = BluePrimario,
                                    contentColor = Color.White
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Filled.Call,
                                        contentDescription = stringResource(R.string.llamar_desc),
                                        tint = Color.White,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    /*Text(
                                        "Llamada",
                                        style = TextStyle(fontWeight = FontWeight.Normal),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )*/
                                }
                            }
                        }
                    }
                }
            } else {
                Column( // Para centrar el indicador y el texto
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ){
                    CircularProgressIndicator() // No necesita .align en Column con horizontalAlignment
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(stringResource(R.string.cargando_cliente))
                }
            }

            // Sección de Envases: se recarga cuando cambian las ventas del cliente (alta o baja)
            val idClienteEnvases = clienteId?.toIntOrNull()
            if (cliente.value != null && idClienteEnvases != null) {
                Spacer(modifier = Modifier.height(16.dp))
                EnvasesClienteSection(
                    clienteId = idClienteEnvases,
                    recargarKey = ventaModel.ventasUiState.takeIf { it is VentasUiState.Success }
                )
            }

            // Sección de Últimas Ventas
            Spacer(modifier = Modifier.height(16.dp))

            TabRow(selectedTabIndex = tabCliente) {
                // The Pagos tab needs cobros.ver
                val titulos = listOf(stringResource(R.string.tab_compras), stringResource(R.string.tab_pagos))
                titulos.take(if (puedeVerCobros) 2 else 1).forEachIndexed { index, titulo ->
                    Tab(
                        selected = tabCliente == index,
                        onClick = { tabCliente = index },
                        text = { Text(titulo) }
                    )
                }
            }

            if (tabCliente == 1 && puedeVerCobros) {
                PagosCliente(deudaModel.pagos.value)
            } else if (ventasDelCliente.value.isNotEmpty()) {
                ventasDelCliente.value.forEach { venta ->

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ){
                            Column(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .weight(1.5f)
                            ) {
                                Text(stringResource(R.string.producto_format, venta.producto))
                                Text(stringResource(R.string.fecha_format, venta.fecha))
                                Text(stringResource(R.string.cantidad_format, venta.cantidad))
                                Text(stringResource(R.string.precio_format, formatearPrecio(venta.precio)))
                            }
                            // Deleting a sale needs ventas.eliminar
                            if (puedeEliminarVenta) {
                                IconButton(
                                    // Ask first, like deleting a product: the sale is gone for good
                                    onClick = { ventaAEliminar = venta },
                                    modifier = Modifier
                                        .weight(0.5f)
                                        .background(Color.Red)
                                ) {
                                    Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.eliminar_desc))
                                }
                            }
                        }
                    }
                }
            } else {
                if (cliente.value != null) {
                    Text(
                        stringResource(R.string.cargar_ventas_previas),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    ventaAEliminar?.let { venta ->
        AlertDialog(
            onDismissRequest = { ventaAEliminar = null },
            title = { Text(stringResource(R.string.confirmar_eliminacion_titulo)) },
            text = {
                Text(stringResource(R.string.confirmar_eliminar_venta_msg, venta.cantidad, venta.producto, venta.fecha))
            },
            confirmButton = {
                Button(
                    onClick = {
                        ventaAEliminar = null
                        ventaModel.eliminarVenta(venta.idVenta, clienteId!!.toInt())
                        navController.navigate(Deuda(id = clienteId)) {
                            popUpTo<Deuda> { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                ) { Text(stringResource(R.string.eliminar_label)) }
            },
            dismissButton = {
                Button(onClick = { ventaAEliminar = null }) { Text(stringResource(R.string.cancelar)) }
            }
        )
    }
}

@Composable
private fun PagosCliente(pagos: List<Pago>) {
    if (pagos.isEmpty()) {
        Text(
            stringResource(R.string.sin_pagos_registrados),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            textAlign = TextAlign.Center
        )
        return
    }
    pagos.forEach { pago ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(R.string.fecha_format, pago.fecha))
                Text(stringResource(R.string.monto_pago_format, formatearPrecio(pago.monto)))
                Text(stringResource(R.string.forma_pago_item_format, pago.formaPago))
            }
        }
    }
}

// Back to the main screen on Clientes > Cobros with this client preselected
private fun irACobrarCliente(navController: NavController, clienteId: Int) {
    val principal = runCatching { navController.getBackStackEntry<Bienvenida>() }.getOrNull() ?: return
    principal.savedStateHandle[CLAVE_COBRAR_CLIENTE] = clienteId
    navController.popBackStack<Bienvenida>(inclusive = false)
}

fun openDialer(context: Context, phoneNumber: String) {
    val uri = "tel:$phoneNumber".toUri()
    val intent = Intent(Intent.ACTION_DIAL, uri)
    context.startActivity(intent)
}