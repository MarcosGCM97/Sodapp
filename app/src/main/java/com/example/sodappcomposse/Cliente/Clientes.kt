package com.example.sodappcomposse.Cliente

import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.sodappcomposse.ClienteEditar
import com.example.sodappcomposse.Deuda
import com.example.sodappcomposse.Agenda
import com.example.sodappcomposse.R

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Clientes(
    clientesModel: ClientesViewModel = hiltViewModel(),
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

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddClienteForm(
    clienteModel: ClientesViewModel = hiltViewModel(),
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
                Toast.makeText(context, context.getString(currentState.messageRes, *currentState.args), Toast.LENGTH_LONG).show()
                nombreCliente = "" // Limpiar campos
                direccionCliente = ""
                telefonoCliente = ""
                //clienteModel.resetAddClienteState() // Resetear el estado para futuros guardados
            }
            is AddClienteUiState.Error -> {
                Toast.makeText(context, context.getString(currentState.messageRes, *currentState.args), Toast.LENGTH_LONG).show()
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
        Text(stringResource(R.string.cargar_cliente_titulo), style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(
            value = nombreCliente,
            onValueChange = { nombreCliente = it },
            label = { Text(stringResource(R.string.nombre_cliente_label)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Campo para la Dirección del Cliente
        OutlinedTextField(
            value = direccionCliente,
            onValueChange = { direccionCliente = it },
            label = { Text(stringResource(R.string.direccion_label)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Campo para el Teléfono del Cliente
        OutlinedTextField(
            value = telefonoCliente,
            onValueChange = { telefonoCliente = it },
            label = { Text(stringResource(R.string.telefono_label)) },
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
                Text(stringResource(R.string.guardar_cliente))
            }
        }

        // Separador visual si quieres (opcional)
        Divider(modifier = Modifier.padding(vertical = 16.dp))

        Text(stringResource(R.string.buscar_existente_label), style = MaterialTheme.typography.titleMedium) // Añadido para dar contexto al buscador

        BuscarCliente(
            navController = navController
        )
    }

}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuscarCliente(
    clienteModel: ClientesViewModel = hiltViewModel(),
    agendaModel: AgendaViewModel = hiltViewModel(),
    navController: NavController
){
    val context = LocalContext.current

    val clienteUiState = clienteModel.clienteUiState
    val clientes = clienteModel.clientes

    val clienteSeleccionado by clienteModel.clienteParaDropDown

    var mostrarDialogoAgenda by remember { mutableStateOf(false) }

    val diasSemana = listOf(
        stringResource(R.string.lunes),
        stringResource(R.string.martes),
        stringResource(R.string.miercoles),
        stringResource(R.string.jueves),
        stringResource(R.string.viernes),
        stringResource(R.string.sabado)
    )
    var diasSeleccionadosAgenda by remember { mutableStateOf(emptyList<String>()) }

    var mostrarDialogoConfirmacion by remember { mutableStateOf(false) }
    var clienteAEliminar by remember { mutableStateOf<Cliente?>(null) }

    LaunchedEffect(clienteUiState) {
        when (clienteUiState) {
            is ClienteUiState.Success -> {
                //Log.d("AddClienteForm", "Success: ${clientes.size} clients loaded.")
                // onVentaAgregada() // Llama a esto si es apropiado aquí
            }
            is ClienteUiState.Error -> {
                //Log.d("AddClienteForm", "Error: ${clienteUiState.message}")
                Toast.makeText(context, context.getString(clienteUiState.messageRes, *clienteUiState.args), Toast.LENGTH_SHORT).show()
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
                Text(stringResource(R.string.detalles_cliente_titulo), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.nombre_format, cliente.nombreCl))
                Text(stringResource(R.string.direccion_format, cliente.direccionCl))
                Text(stringResource(R.string.telefono_format, cliente.numTelCl))
                Text(stringResource(R.string.deuda_format, cliente.deudaCl))

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start // Alinea el botón a la derecha
                ) {
                    Button(
                        onClick = {
                            navController.navigate(ClienteEditar(id = cliente.idCl))
                            clienteModel.clienteParaDropDown.value = null
                        }
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.editar_desc))
                        Spacer(modifier = Modifier.width(4.dp))
                        //Text("Editar")
                    }
                    //Spacer(modifier = Modifier.weight(1f)) // Espacio entre los botones
                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            navController.navigate(Deuda(id = cliente.idCl.toString()))
                            clienteModel.clienteParaDropDown.value = null
                        }
                    ) {
                        Icon(Icons.Filled.ShoppingCart, contentDescription = stringResource(R.string.deuda_desc))
                        Spacer(modifier = Modifier.width(4.dp))
                        //Text("Ver deuda")//Text("Ver deuda")
                    }

                    Spacer(modifier = Modifier.weight(1f)) // Espacio entre los botones

                    Button(
                        onClick = {
                            diasSeleccionadosAgenda = agendaModel.diasEntregaById.value
                            mostrarDialogoAgenda = true
                        }
                    ) {
                        Icon(Icons.Filled.DateRange, contentDescription = stringResource(R.string.agendar_desc))
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    IconButton(
                        onClick = {
                            clienteAEliminar = cliente
                            mostrarDialogoConfirmacion = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(R.string.eliminar_desc),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))
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
                Text(text = stringResource(R.string.elegir_dia_titulo))
            },
            text = {
                Column { // Usamos una Column para organizar el texto y los RadioButtons
                    Text(stringResource(R.string.selecciona_dia_label))
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
                            agendaModel.updateDiasEntrega( // Nombre de función actualizado
                                clienteSeleccionado?.idCl,
                                diasSeleccionadosAgenda.toList() // Enviar como lista
                            )
                            val textoDias = if (diasSeleccionadosAgenda.isEmpty()) "ningún día" else diasSeleccionadosAgenda.joinToString()
                            Toast.makeText(context, context.getString(R.string.visita_agendada_msg, clienteSeleccionado?.nombreCl, textoDias), Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, context.getString(R.string.error_cliente_no_seleccionado), Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text(stringResource(R.string.aceptar))
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        mostrarDialogoAgenda = false
                    }
                    ) {
                    Text(stringResource(R.string.cancelar))
                }
            }
        )
    }

    if (mostrarDialogoConfirmacion && clienteAEliminar != null) {
        AlertDialog(
            onDismissRequest = {
                mostrarDialogoConfirmacion = false
                clienteAEliminar = null
            },
            title = {
                Text(text = stringResource(R.string.confirmar_eliminacion_titulo))
            },
            text = {
                Text(context.getString(R.string.confirmar_eliminar_cliente_msg, clienteAEliminar?.nombreCl))
            },
            confirmButton = {
                Button(
                    onClick = {
                        clienteAEliminar?.let { cli ->
                            // Llama al método de tu ViewModel (asegúrate de que exista)
                            clienteModel.eliminarCliente(cli)
                        }
                        mostrarDialogoConfirmacion = false
                        clienteAEliminar = null
                        clienteModel.clienteParaDropDown.value = null // Limpiar la selección actual
                        clienteModel.getClientes() // Refrescar la lista de clientes
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.eliminar_label))
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        mostrarDialogoConfirmacion = false
                        clienteAEliminar = null
                    }
                ) {
                    Text(stringResource(R.string.cancelar))
                }
            }
        )
    }
}
