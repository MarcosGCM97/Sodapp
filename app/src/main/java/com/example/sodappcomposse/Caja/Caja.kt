package com.example.sodappcomposse.Caja

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

@SuppressLint("SuspiciousIndentation")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CajaScreen(
    cajaModel: CajaViewModel = hiltViewModel(),
){
    val TAG = "CajaScreen"

    val cajaUiState = cajaModel.cajaUiState
    val cajaData by cajaModel.caja.collectAsState()
    val mesSeleccionadoViewModel by cajaModel.mesSeleccionadoUi.collectAsState()
    val cajaTotales by cajaModel.cajaTotales.collectAsState()

    val listaDeMeses = remember { Meses.entries.toList() }
    var expandedMeses by remember { mutableStateOf(false)}
    var selectedMes by remember { mutableStateOf<Meses?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ){
        item {
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
                                cajaModel.seleccionarMes(mes)
                                selectedMes = mes
                                expandedMeses = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }
        }

        item {
            Button(
                onClick = {
                    cajaModel.getCajaPorMes()
                }
            ){
                Text("Ver caja del mes")
            }
        }

        when {
            cajaUiState is CajaUiState.Loading -> {
                item {
                    CircularProgressIndicator()
                    Text("Cargando datos de la caja...")
                }
            }
            cajaUiState is CajaUiState.Error -> {
                item { Text("No hay ventas registradas para este mes.") }
            }
            cajaData.caja.isNullOrEmpty() && cajaUiState is CajaUiState.Success -> {
                item { Text("No hay datos disponibles para mostrar.") }
            }
            cajaUiState is CajaUiState.Success -> {
                if(cajaData.success == false){
                    item { Text("No hay ventas registradas para este mes.") }
                } else {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .background(color = MaterialTheme.colorScheme.tertiary),
                            contentAlignment = Alignment.Center
                        ){
                            Text(
                                text = selectedMes?.name ?: "",
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onTertiary,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    items(cajaTotales.cantidadPorProducto.values.toList()) { ventaXprod ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(color = MaterialTheme.colorScheme.primary),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ){
                            Text(
                                text = "${ventaXprod.producto}: ",
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier
                                    .padding(6.dp)
                                    .fillMaxWidth()
                            )
                            Text(
                                text = "Cantidad: ${ventaXprod.cantidad}        Pesos: $${ventaXprod.precio}",
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier
                                    .padding(6.dp)
                                    .fillMaxWidth()
                            )
                        }
                    }

                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .background(color = MaterialTheme.colorScheme.onPrimary),
                            contentAlignment = Alignment.Center
                        ){
                            Text(
                                text ="Total de las Ventas: $${cajaTotales.montoTotal}",
                                modifier = Modifier.padding(16.dp),
                            )
                        }
                    }
                }
            }
            else -> {
                item { Text("Esperando datos...") }
            }
        }
    }
}