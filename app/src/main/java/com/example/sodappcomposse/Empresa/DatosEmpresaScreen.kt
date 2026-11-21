package com.example.sodappcomposse.Empresa

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.sodappcomposse.Componentes.ScreenWithBackButtonWrapper
import com.example.sodappcomposse.R
import com.example.sodappcomposse.auth.LocalSessionPermissions
import com.example.sodappcomposse.auth.puedeAdministrar

/** Owner-only form of the company data; the name signs the WhatsApp messages. */
@Composable
fun DatosEmpresaScreen(
    navController: NavController,
    viewModel: EmpresaViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState
    val empresa by viewModel.empresa
    val esDueno = puedeAdministrar(LocalSessionPermissions.current)

    var nombre by rememberSaveable { mutableStateOf("") }
    var telefono by rememberSaveable { mutableStateOf("") }
    var direccion by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(esDueno) {
        if (esDueno) viewModel.cargar()
    }

    // Fill the form with the saved data once it arrives (and after saving)
    LaunchedEffect(empresa) {
        empresa?.let {
            nombre = it.nombre
            telefono = it.telefono.orEmpty()
            direccion = it.direccion.orEmpty()
        }
    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is EmpresaUiState.Success -> {
                Toast.makeText(context, context.getString(state.messageRes), Toast.LENGTH_SHORT).show()
                viewModel.resetState()
            }
            is EmpresaUiState.Error -> {
                // Backend validation (400), the 403 message or a network error
                Toast.makeText(context, context.getString(state.messageRes, *state.args), Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> Unit
        }
    }

    ScreenWithBackButtonWrapper(
        navController = navController,
        title = stringResource(R.string.empresa_titulo),
        mostrarDatosEmpresa = false
    ) {
        if (!esDueno) {
            Text(stringResource(R.string.error_sin_permiso))
            return@ScreenWithBackButtonWrapper
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.empresa_ayuda),
                style = MaterialTheme.typography.bodyMedium
            )
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it.take(MAX_NOMBRE) },
                label = { Text(stringResource(R.string.empresa_nombre_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = telefono,
                onValueChange = { telefono = it.take(MAX_TELEFONO) },
                label = { Text(stringResource(R.string.empresa_telefono_label)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = direccion,
                onValueChange = { direccion = it.take(MAX_DIRECCION) },
                label = { Text(stringResource(R.string.empresa_direccion_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = { viewModel.guardar(nombre, telefono, direccion) },
                enabled = uiState !is EmpresaUiState.Loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.guardar))
            }
        }
    }
}

// Same limits as empresa.php
private const val MAX_NOMBRE = 80
private const val MAX_TELEFONO = 30
private const val MAX_DIRECCION = 150
