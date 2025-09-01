package com.example.sodappcomposse.Caja

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.error
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay

@SuppressLint("SuspiciousIndentation")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CajaScreen(
    cajaModel: CajaViewModel = viewModel(),
){
    val TAG = "CajaScreen"
    val ScrollState = rememberScrollState()

    val cajaUiState = cajaModel.cajaUiState
    val cajaData by cajaModel.caja.collectAsState() // Observa los datos de la caja
    val mesSeleccionadoViewModel by cajaModel.mesSeleccionadoUi.collectAsState() // Observa el mes desde el ViewModel

    val listaDeMeses = remember { Meses.entries.toList() }
    var expandedMeses by remember { mutableStateOf(false)}
    var selectedMes by remember { mutableStateOf<Meses?>(null) }

    var showDelayedElements by remember { mutableStateOf(false) }
    val delayMillis = 1000L

    /*LaunchedEffect(mesSeleccionadoViewModel, cajaUiState) {
        if (mesSeleccionadoViewModel != null && cajaUiState is CajaUiState.Success) {
            //delay(delayMillis)
            showDelayedElements = true
            Log.d(TAG, "showDelayedElements activado para ${mesSeleccionadoViewModel?.name}")
        } else if (mesSeleccionadoViewModel == null || cajaUiState !is CajaUiState.Success) {
            // Resetea si no hay mes o no hay datos cargados
            showDelayedElements = false
        }
    }*/
    val catidadDeVentasPorProducto = cajaData.caja?.groupBy { it.producto }?.mapValues { (_, ventas) ->
        catidadDeVentasPorProducto(
            producto = ventas.first().producto,
            cantidad = ventas.sumOf { it.cantidad.toIntOrNull() ?: 0 },
            precio = ventas.sumOf { it.precio * it.cantidad.toDouble() }
        )
    }

    val catidadDeVentas = cajaData.caja?.sumOf { venta ->
        venta.cantidad.toIntOrNull() ?: 0
    }

    val cantidadDePlata = cajaData.caja?.sumOf { venta ->
        venta.precio * (venta.cantidad.toIntOrNull() ?: 0)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(ScrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ){
        ExposedDropdownMenuBox(
            expanded = expandedMeses,
            onExpandedChange = { expandedMeses = !expandedMeses },
            modifier = Modifier.fillMaxWidth()
        ){
            TextField(
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                readOnly = true,
                value = mesSeleccionadoViewModel?.name ?: "Seleccione un mes",
                onValueChange = {},
                label = { Text("Mes") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = expandedMeses
                    )
                },
                colors = ExposedDropdownMenuDefaults.textFieldColors()
            )

            ExposedDropdownMenu(
                expanded = expandedMeses,
                onDismissRequest = { expandedMeses = false },
                modifier = Modifier.fillMaxWidth()
            ){
                listaDeMeses.forEach{ mes ->
                    DropdownMenuItem(
                        text = { Text(mes.name) },
                        onClick = {
                            Log.d("CajaScreen", "Mes seleccionado: ${mes.name}, ${mes.numero}")
                            cajaModel.seleccionarMes(mes)
                            selectedMes = mes
                            expandedMeses = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }

        Spacer(modifier= Modifier.height(16.dp))

        Button(
            onClick = {
                Log.d(TAG, "Botón 'Ver caja del mes' clickeado")
                //showDelayedElements = true
                cajaModel.getCajaPorMes()
            }
        ){
            Text("Ver caja del mes")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if(cajaUiState is CajaUiState.Loading){
            CircularProgressIndicator()
            Text("Cargando datos de la caja...")
        }else if(cajaUiState is CajaUiState.Error){

            Text("No hay ventas registradas para este mes.")
        }else if(cajaData.caja!!.isEmpty()){
            Text("No hay datos disponibles para mostrar.")
        } else if(cajaUiState is CajaUiState.Success){

            if(cajaData.success == false){
                Text("No hay ventas registradas para este mes.")
            }else{
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .background(color = MaterialTheme.colorScheme.tertiary),
                    contentAlignment = Alignment.Center
                ){
                    Text(
                        text = selectedMes!!.name,
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                            .background(color = MaterialTheme.colorScheme.tertiary),
                        color = MaterialTheme.colorScheme.onTertiary,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                        .weight(1f)
                        .background(color = MaterialTheme.colorScheme.tertiary),
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    catidadDeVentasPorProducto?.forEach { ventaXprod ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(6.dp)
                                .background(color = MaterialTheme.colorScheme.primary),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ){
                            Text(
                                text = "${ventaXprod.value.producto}: ",
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier
                                    .padding(6.dp)
                                    .fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "Cantidad: ${ventaXprod.value.cantidad}        Pesos: $${ventaXprod.value.precio}",
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier
                                    .padding(6.dp)
                                    .fillMaxWidth()
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .background(color = MaterialTheme.colorScheme.onPrimary),
                        contentAlignment = Alignment.Center
                    ){
                        Text(
                            text ="Total de las Ventas:: $$cantidadDePlata",
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                        )
                    }
                }
            }


        }else {
            CircularProgressIndicator()
            Text("Esperando datos...")
        }

        //if (showDelayedElements) {
        //    Text("Mostrando datos de la caja...")
        //    Log.d(TAG, "Mostrando datos de la caja...${cajaData.ventas.size}")
        //}

       /* when (cajaUiState) {
            is CajaUiState.Loading -> {
                Log.d(TAG, "Estado: Cargando datos para ${mesSeleccionadoViewModel?.name}...")
                CircularProgressIndicator()
                Text("Cargando datos para ${mesSeleccionadoViewModel?.name ?: "el mes seleccionado"}...")
            }
            is CajaUiState.Success -> {
                if (mesSeleccionadoViewModel != null) {
                    Text("Mes: ${mesSeleccionadoViewModel!!.name} (${mesSeleccionadoViewModel!!.numero})")

                    if (cajaData.ventas.isEmpty()) {
                        Text("No hay ventas registradas para este mes.")
                    } else {
                        if (showDelayedElements) {
                            Text("Mostrando ${cajaData.ventas.size} ventas (con retraso):")
                            cajaData.ventas.forEach { venta ->
                                Text("Cliente: ${venta.cliente ?: "N/D"}")
                                Text("Dirección: ${venta.direccion ?: "N/D"}")
                                Text("Teléfono: ${venta.telefono ?: "N/D"}")
                                Text("Producto: ${venta.producto ?: "N/D"}")
                                Text("Precio: ${venta.precio ?: 0.0}")
                                Text("Cantidad: ${venta.cantidad ?: 0}")
                                Text("Fecha: ${venta.fecha ?: "N/D"}")
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        } else {
                            Log.d(TAG, "Datos listos para ${mesSeleccionadoViewModel!!.name}, pero esperando delay para mostrar ventas.")
                            Text("Preparando visualización de ventas...") // Opcional
                        }
                    }
                } else {
                    Text("Datos cargados pero no hay mes seleccionado en el ViewModel.")
                }
            }
            is CajaUiState.Error -> {
                Log.e(TAG, "Estado: Error - ${(cajaUiState as CajaUiState.Error).message}")
                Text("Error al cargar datos: ${(cajaUiState as CajaUiState.Error).message}", color = MaterialTheme.colorScheme.error)
                Text("Por favor, intenta seleccionar un mes nuevamente.")
            }
            is CajaUiState.Idle -> {
                Text("Selecciona un mes para ver los datos de la caja.")
                Log.d(TAG, "Estado: Idle. No hay mes seleccionado o carga iniciada.")
            }
        }*/
    }
}