package com.example.sodappcomposse.Cliente

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sodappcomposse.Componentes.CardWpp
import com.example.sodappcomposse.Componentes.ScreenWithBackButtonWrapper
import com.example.sodappcomposse.Producto.ProductoViewModel
import com.example.sodappcomposse.Ventas.VentaIdEditar
import com.example.sodappcomposse.Ventas.VentasViewModel
import com.example.sodappcomposse.ui.theme.BluePrimario
import com.example.sodappcomposse.ui.theme.GreenPrimario

@SuppressLint("UnrememberedMutableState", "StateFlowValueCalledInComposition")
@Composable
fun DeudaScreen(
    navController: NavController,
    clienteId: String?,
    clienteModel: ClientesViewModel = viewModel(),
    ventaModel: VentasViewModel = viewModel(),
    productosModel: ProductoViewModel = viewModel()
) {
    val context = LocalContext.current

    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        if (clienteId != null && clienteId != "{clienteId}") {
            ventaModel.getVentasByClienteId(clienteId)
            clienteModel.getClienteById(clienteId)
            productosModel.getProductos()
            Log.d("DeudaScreen", "clienteId recibido: $clienteId")
        } else {
            Log.e("DeudaScreen", "clienteId es nulo o inválido: $clienteId")
            navController.popBackStack()
        }
    }
    val cliente = clienteModel.clienteById // Manteniendo tu estructura actual por ahora

    val ventasDelCliente = ventaModel.ventasPorClienteId

    var pagarMonto by remember { mutableStateOf(0) }

    var ver by remember { mutableStateOf(false) }

    //var nuevaCantidad by remember { mutableStateOf(0) }

    var editar by remember { mutableStateOf(false) }

    var editarEstados = remember { mutableMapOf<Int, Boolean>() }

    // Usamos el nuevo wrapper
    ScreenWithBackButtonWrapper(
        navController = navController,
        title = "Detalles de Deuda"
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
                        Text("Detalles del Cliente:", style = MaterialTheme.typography.titleMedium)
                        Text("Nombre: ${cliente.value?.nombreCl}")
                        Text("Dirección: ${cliente.value?.direccionCl}")
                        Text("Teléfono: ${cliente.value?.numTelCl}")
                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Saldo ventas:", style = MaterialTheme.typography.titleMedium)
                        Text("$${cliente.value?.deudaCl}")

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Pagar deuda", style = MaterialTheme.typography.headlineSmall)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                                //.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            OutlinedTextField(
                                value = "$pagarMonto",
                                onValueChange = { pagarMonto = it.toIntOrNull() ?: 0 },
                                label = { Text("Ingrese monto") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Card(
                                modifier = Modifier
                                    .width(90.dp)
                                    .height(40.dp)
                                    .clickable {
                                        clienteModel.pagarDeudaCliente(
                                            cliente.value?.idCl ?: 0,
                                            pagarMonto.toDouble()
                                        )
                                        pagarMonto = 0
                                        clienteModel.getClienteById(clienteId.toString())
                                    },
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = GreenPrimario,
                                    contentColor = Color.White
                                )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center // Centra el Text
                                ) {
                                    Text(
                                        text = "Saldar",
                                        textAlign = TextAlign.Center,
                                        fontSize = 14.sp, // Descomenta y ajusta si necesitas un tamaño específico
                                        // fontWeight = FontWeight.Bold // Para que se destaque más
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                                //.padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Botón de WhatsApp
                            CardWpp(context, cliente.value, "Tienes una deuda de $${cliente.value?.deudaCl}")

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
                                        contentDescription = "Llamar",
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
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ){
                    CircularProgressIndicator() // No necesita .align en Column con horizontalAlignment
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Cargando datos del cliente...")
                }
            }

            // Sección de Últimas Ventas
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    ver = !ver
                    Log.d("DeudaScreen", "Ver ultimas compras del cliente: $ver, ${ventasDelCliente.value}")
                          },
                modifier = Modifier
                    .fillMaxWidth()
                    //.padding(horizontal = 16.dp)
                    .padding(top = 8.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = GreenPrimario,
                    contentColor = Color.White
                )
               // "Ver ultimas compras del cliente:",
               // style = MaterialTheme.typography.titleMedium,
               // modifier = Modifier.padding(bottom = 8.dp)
            ){
                Text("Ver ultimas compras del cliente")
            }

            if (ventasDelCliente.value.isNotEmpty() && ver) {
                ventasDelCliente.value.forEach { venta ->
                    editarEstados[venta.idVenta.toInt()] = editar

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
                                Text("Producto: ${venta.producto}")
                                Text("Fecha: ${venta.fecha}")
                                if (editarEstados[venta.idVenta.toInt()] == true){
                                    OutlinedTextField(
                                        value = venta.cantidad,
                                        onValueChange = { nuevaCantidad ->
                                            venta.cantidad = nuevaCantidad
                                        },
                                        label = { Text("Nueva cantidad") }
                                    )
                                }else {
                                    Text("Cantidad: ${venta.cantidad}")
                                }
                                Text("Precio: $${venta.cantidad.toDouble() * venta.precio.toDouble()}")
                            }

                            if(editar){
                                IconButton(
                                    onClick = {
                                        ventaModel.updateVenta(venta.idVenta.toInt(), venta.cantidad.toInt())
                                        editar = !editar
                                    },
                                    modifier = Modifier.weight(0.5f)
                                ) {
                                    Icon(Icons.Filled.Build, contentDescription = "Editar")
                                }
                            }else{
                                IconButton(
                                    onClick = {
                                        editar = !editar
                                        Log.d("DeudaScreen", "Ver ultimas compras del cliente: $ver")
                                    },
                                    modifier = Modifier.weight(0.5f)

                                ) {
                                    Icon(Icons.Filled.Create, contentDescription = "Editar")
                                }
                            }
                        }
                    }
                }
            } else {
                Log.d("DeudaScreen", "No hay ventas para este cliente $ventasDelCliente, $ver")
                if (cliente.value != null) {
                    Text(
                        "No hay ventas registradas para este cliente.",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

    }
}

fun openDialer(context: Context, phoneNumber: String) {
    val uri = "tel:$phoneNumber".toUri()
    val intent = Intent(Intent.ACTION_DIAL, uri)
    context.startActivity(intent)
}