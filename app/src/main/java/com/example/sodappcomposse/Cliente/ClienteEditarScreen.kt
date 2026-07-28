package com.example.sodappcomposse.Cliente

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.sodappcomposse.Componentes.ScreenWithBackButtonWrapper


@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnrememberedMutableState")
@Composable
fun ClienteEditarScreen(
    navController: NavController,
    idCliente: Int?,
    clienteModel: ClientesViewModel = hiltViewModel()
){
    val contexto = LocalContext.current

    LaunchedEffect(Unit) {
        clienteModel.getClientes()
    }

    var nombreCliente by remember { mutableStateOf("") }
    var dirCliente by remember { mutableStateOf("") }
    var telCliente by remember { mutableStateOf("") }

    val clienteElegido = clienteModel.clientes.find { it.idCl == idCliente }

    LaunchedEffect(clienteElegido) {
        clienteElegido?.let {
            nombreCliente = it.nombreCl
            telCliente = it.numTelCl
            dirCliente = it.direccionCl
        }
    }

    ScreenWithBackButtonWrapper(
        navController = navController,
        title = "Editar Cliente"
    ){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            OutlinedTextField(
                value = nombreCliente,
                onValueChange = { nombreCliente = it },
                label = { Text("Nombre") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = telCliente,
                onValueChange = { telCliente = it },
                label = { Text("Teléfono") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = dirCliente,
                onValueChange = { dirCliente = it },
                label = { Text("Dirección") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                modifier = Modifier
                    .width(200.dp),
                onClick = {
                    val clienteEditado = Cliente(
                        idCl = clienteElegido?.idCl ?: 0,
                        nombreCl = nombreCliente,
                        numTelCl = telCliente,
                        direccionCl = dirCliente,
                        deudaCl = clienteElegido?.deudaCl ?: 0.0
                    )
                    clienteModel.editarCliente(clienteEditado)
                    navController.popBackStack()
                }
            ) {
                Text("Guardar")
            }
        }
    }


}