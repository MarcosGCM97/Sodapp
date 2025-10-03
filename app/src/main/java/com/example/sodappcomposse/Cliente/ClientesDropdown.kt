package com.example.sodappcomposse.Cliente

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientesDropDown(
    clienteModel: ClientesViewModel = viewModel()
){
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        clienteModel.getClientes()
    }
    val clienteUiState = clienteModel.clienteUiState
    val clientes = clienteModel.clientes

    LaunchedEffect(clienteUiState) {
        when (clienteUiState) {
            is ClienteUiState.Success -> {
                //Log.d("AddVentaForm", "Success: ${clientes.size} clients loaded.")
                // onVentaAgregada() // Llama a esto si es apropiado aquí
                Toast.makeText(context, "Clientes cargados: ${clientes.size}", Toast.LENGTH_SHORT).show()
            }
            is ClienteUiState.Error -> {
                //Log.d("AddVentaForm", "Error: ${clienteUiState.message}")
                Toast.makeText(context, clienteUiState.message, Toast.LENGTH_SHORT).show()
            }
            is ClienteUiState.Loading -> {
                //Log.d("AddVentaForm", "Loading clients...")
            }
            ClienteUiState.Idle -> {
                //Log.d("AddVentaForm", "Client state is Idle.")
            }
        }
    }

    var expandedClients by remember { mutableStateOf(false) }

    val clientesActuales: List<Cliente> = when (clienteUiState) {
        is ClienteUiState.Success -> clientes
        else -> emptyList() // Return an empty list of Cliente
    }

    val clienteParaVer = clienteModel.clienteParaDropDown

    ExposedDropdownMenuBox(
        expanded = expandedClients,
        onExpandedChange = { expandedClients = !expandedClients },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = clienteParaVer.value?.nombreCl ?: "Selecciona un cliente",
            onValueChange = {},
            readOnly = true,
            label = { Text("Cliente") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedClients)
            },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expandedClients,
            onDismissRequest = { expandedClients = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            if (clienteUiState is ClienteUiState.Loading) {
                DropdownMenuItem(
                    text = { Text("Cargando...") },
                    onClick = { },
                    enabled = false
                )
            } else if (clientesActuales.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("No hay clientes disponibles") }, // Or error message
                    onClick = { },
                    enabled = false
                )
            } else {
                clientesActuales.forEach { selectionOptionCliente ->
                    DropdownMenuItem(
                        text = { Text(selectionOptionCliente.nombreCl) },
                        onClick = {
                            clienteParaVer.value = selectionOptionCliente
                            expandedClients = false
                            //Toast.makeText(context, "${selectionOptionCliente.nombreCl} seleccionada", Toast.LENGTH_SHORT).show()
                            clienteModel.getDiasEntregaById(selectionOptionCliente.idCl)
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
    }
}
