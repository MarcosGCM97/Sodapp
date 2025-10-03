package com.example.sodappcomposse.Cliente

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@Composable
fun Clientes(
    clientesModel: ClientesViewModel = viewModel(),
    navController: NavController
){
    val TAG = "Clientes"
    val scrollState = rememberScrollState()

    Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            AddClienteForm(
                navController = navController
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddClienteForm(
    clienteModel: ClientesViewModel = viewModel(),
    navController: NavController
){
    val context = LocalContext.current

    val clienteUiState = clienteModel.clienteUiState
    val clientes = clienteModel.clientes

    var nombreCliente by remember { mutableStateOf("") }
    var direccionCliente by remember { mutableStateOf("") }
    var telefonoCliente by remember { mutableStateOf("") }
    var deudaCliente by remember { mutableStateOf(0) }


    // Observar el estado de agregar cliente desde el ViewModel
    val addState by clienteModel.addClienteUiState
    LaunchedEffect(addState) {
        when (val currentState = addState) {
            is AddClienteUiState.Success -> {
                Toast.makeText(context, currentState.message, Toast.LENGTH_LONG).show()
                nombreCliente = "" // Limpiar campos
                direccionCliente = ""
                telefonoCliente = ""
                //clienteModel.resetAddClienteState() // Resetear el estado para futuros guardados
            }
            is AddClienteUiState.Error -> {
                Toast.makeText(context, currentState.message, Toast.LENGTH_LONG).show()
                //clienteModel.resetAddClienteState()
            }
            is AddClienteUiState.Loading -> {
                // Podrías mostrar un indicador de carga aquí si el guardado tarda mucho
            }
            AddClienteUiState.Idle -> {
                // No hacer nada
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Cargar al cliente", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(
            value = nombreCliente,
            onValueChange = { nombreCliente = it },
            label = { Text("Nombre del Cliente") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Campo para la Dirección del Cliente
        OutlinedTextField(
            value = direccionCliente,
            onValueChange = { direccionCliente = it },
            label = { Text("Dirección") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Campo para el Teléfono del Cliente
        OutlinedTextField(
            value = telefonoCliente,
            onValueChange = { telefonoCliente = it },
            label = { Text("Teléfono") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), // Sugiere teclado numérico
            singleLine = true
        )

        // Botón para guardar el nuevo cliente
        Button(
            onClick = {
                clienteModel.agregarNuevoCliente(
                    nombre = nombreCliente,
                    direccion = direccionCliente,
                    telefono = telefonoCliente
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = nombreCliente.isNotBlank() && direccionCliente.isNotBlank() && telefonoCliente.isNotBlank() && addState !is AddClienteUiState.Loading
        ) {
            if (addState is AddClienteUiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Guardar Cliente")
            }
        }

        // Separador visual si quieres (opcional)
        Divider(modifier = Modifier.padding(vertical = 16.dp))

        Text("o buscar uno existente:", style = MaterialTheme.typography.titleMedium) // Añadido para dar contexto al buscador

        BuscarCliente(
            navController = navController
        )
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuscarCliente(
    clienteModel: ClientesViewModel = viewModel(),
    navController: NavController
){
    val context = LocalContext.current

    val clienteUiState = clienteModel.clienteUiState
    val clientes = clienteModel.clientes

    val clienteSeleccionado by clienteModel.clienteParaDropDown

    var mostrarDialogoAgenda by remember { mutableStateOf(false) }

    val diasSemana = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado")
    var diasSeleccionadosAgenda by remember { mutableStateOf(emptyList<String>()) }

    LaunchedEffect(clienteUiState) {
        when (clienteUiState) {
            is ClienteUiState.Success -> {
                //Log.d("AddClienteForm", "Success: ${clientes.size} clients loaded.")
                // onVentaAgregada() // Llama a esto si es apropiado aquí
            }
            is ClienteUiState.Error -> {
                //Log.d("AddClienteForm", "Error: ${clienteUiState.message}")
                Toast.makeText(context, clienteUiState.message, Toast.LENGTH_SHORT).show()
            }
            is ClienteUiState.Loading -> {
                //Log.d("AddClienteForm", "Loading clients...")
            }
            ClienteUiState.Idle -> {
                //Log.d("AddClienteForm", "Client state is Idle.")
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ){
        ClientesDropDown()
    }
    clienteSeleccionado?.let { cliente -> // Se muestra solo si hay un cliente seleccionado
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Detalles del Cliente:", style = MaterialTheme.typography.titleMedium)
                Text("Nombre: ${cliente.nombreCl}")
                Text("Dirección: ${cliente.direccionCl}")
                Text("Teléfono: ${cliente.numTelCl}")
                Text("Deuda: $${cliente.deudaCl}")

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start // Alinea el botón a la derecha
                ) {
                    Button(
                        onClick = {
                            navController.navigate("clienteEditarScreen/${cliente.idCl}")
                            clienteModel.clienteParaDropDown.value = null
                        }
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = "Editar")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Editar")
                    }
                    //Spacer(modifier = Modifier.weight(1f)) // Espacio entre los botones
                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            navController.navigate("deudaScreen/${cliente.idCl}")
                            clienteModel.clienteParaDropDown.value = null
                        }
                    ) {
                        Icon(Icons.Filled.ShoppingCart, contentDescription = "Deuda")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ver deuda")//Text("Ver deuda")
                    }
                    Spacer(modifier = Modifier.weight(1f)) // Espacio entre los botones
                    //Spacer(modifier = Modifier.width(8.dp)) // Espacio entre los botones
                    Button(
                        onClick = {
                            diasSeleccionadosAgenda = clienteModel.diasEntregaById.value
                            mostrarDialogoAgenda = true
                        }
                    ) {
                        Icon(Icons.Filled.DateRange, contentDescription = "Agendar")
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }
            }
        }
    }
    if (mostrarDialogoAgenda) {
        AlertDialog(
            onDismissRequest = {
                mostrarDialogoAgenda = false
            },
            title = {
                Text(text = "Elegir día de visita")
            },
            text = {
                Column { // Usamos una Column para organizar el texto y los RadioButtons
                    Text("Selecciona un día para agendar la visita:")
                    Spacer(modifier = Modifier.height(16.dp)) // Espacio antes de los radio buttons

                    // RadioButtons para los días de la semana
                    diasSemana.forEach { dia ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = diasSeleccionadosAgenda.contains(dia),
                                    onClick = {
                                        // Lógica para añadir/quitar el día del set
                                        diasSeleccionadosAgenda = if (diasSeleccionadosAgenda.contains(dia)) {
                                            diasSeleccionadosAgenda - dia // Quitar día
                                        } else {
                                            diasSeleccionadosAgenda + dia // Añadir día
                                        }
                                    },
                                    role = Role.Checkbox // Rol semántico
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = diasSeleccionadosAgenda.contains(dia),
                                onCheckedChange = null // null porque el Row maneja el click
                            )
                            Text(
                                text = dia,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarDialogoAgenda = false
                        if (clienteSeleccionado != null) {
                            // Convertir el Set a una List o un formato que tu ViewModel espere
                            clienteModel.updateDiasEntrega( // Nombre de función actualizado
                                clienteSeleccionado?.idCl,
                                diasSeleccionadosAgenda.toList() // Enviar como lista
                            )
                            val textoDias = if (diasSeleccionadosAgenda.isEmpty()) "ningún día" else diasSeleccionadosAgenda.joinToString()
                            Toast.makeText(context, "Visita agendada para ${clienteSeleccionado?.nombreCl} los días: $textoDias", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Error: No se seleccionó ningún cliente.", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        mostrarDialogoAgenda = false
                    }
                    ) {
                    Text("Cancelar")
                }
            }
        )
    }
}
/*
@Preview(showSystemUi = true)
@Composable
fun PreviewClientesScreen(){
    Clientes(
        navController = NavController(LocalContext.current)
    )
}*/