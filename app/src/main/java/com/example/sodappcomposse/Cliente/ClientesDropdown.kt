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

    val optionClients: List<Cliente> = when (clienteUiState) {
        is ClienteUiState.Success -> clientes.map { it }
        else -> listOf("No hay clientes disponibles")
    } as List<Cliente>

    var expandedClients by remember { mutableStateOf(false) }

    // State for the client selected in the dropdown
    // This will hold actual Cliente objects or be empty
    val clientesActuales: List<Cliente> = when (clienteUiState) {
        is ClienteUiState.Success -> clientes
        else -> emptyList() // Return an empty list of Cliente
    }

    // This can be used for the display string in the TextField,
    // and for the items in the DropdownMenu.
    //val clientDisplayOptions: List<String> = when (clienteUiState) {
    //    is ClienteUiState.Success -> clientes.map { it.nombreCl }
    //    is ClienteUiState.Loading -> listOf("Cargando clientes...")
    //    else -> listOf("No hay clientes disponibles") // Or "Error al cargar", etc.
    //}

    val clienteParaVer = clienteModel.clienteParaDropDown

    // ... (expandedClients state)

    // For the ExposedDropdownMenuBox's TextField part:
    // val textFieldClientValue = clienteParaVenta.value?.nombreCl ?: "Selecciona un cliente"
    // Or, if you want to show "Cargando..." in the text field when loading:
    //val textFieldClientValue = when (clienteUiState) {
    //    is ClienteUiState.Success -> clienteParaVenta.value?.nombreCl ?: "Selecciona un cliente"
    //    is ClienteUiState.Loading -> "Cargando clientes..."
    //    is ClienteUiState.Error -> "Error al cargar clientes"
    //    ClienteUiState.Idle -> "Selecciona un cliente"
    //}

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
                clientesActuales.forEach { selectionOptionCliente -> // Iterate over List<Cliente>
                    DropdownMenuItem(
                        text = { Text(selectionOptionCliente.nombreCl) },
                        onClick = {
                            clienteParaVer.value = selectionOptionCliente // Store the Cliente object
                            // currentSelectedClientInDropdown = selectionOptionCliente // This variable might become redundant or needs re-evaluation
                            expandedClients = false
                            Toast.makeText(context, "${selectionOptionCliente.nombreCl} seleccionada", Toast.LENGTH_SHORT).show()
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
    }
}
