package com.example.sodappcomposse.Componentes

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.compose.ui.res.stringResource
import com.example.sodappcomposse.R
import com.example.sodappcomposse.Cliente.AgendaViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnrememberedMutableState")
@Composable
fun AgendaScreen(
    navController: NavController,
    agendaModel: AgendaViewModel = hiltViewModel(),
) {
    // --- ESTILO: Usar el nombre del día actual como valor inicial ---
    val nombreDelDiaHoy = remember {
        LocalDate.now().dayOfWeek.getDisplayName(
            TextStyle.FULL,
            Locale("es", "ES")
        ).replaceFirstChar { it.titlecase(Locale.getDefault()) }
    }

    val fechaDeHoyString = remember {
        LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) // Formato "YYYY-MM-DD"
    }

    val diasSemana = listOf(
        stringResource(R.string.lunes),
        stringResource(R.string.martes),
        stringResource(R.string.miercoles),
        stringResource(R.string.jueves),
        stringResource(R.string.viernes),
        stringResource(R.string.sabado)
    )
    val lunes = stringResource(R.string.lunes)
    var diaSeleccionado by remember(lunes) {
        mutableStateOf(
            if (diasSemana.contains(nombreDelDiaHoy)) nombreDelDiaHoy else lunes
        )
    }

    val diasDeClientes by agendaModel.diasEntrega
    var expandedDias by remember { mutableStateOf(false) }

    // --- ESTILO: Llamada a la API que se basa en el día seleccionado ---
    // Este LaunchedEffect se ejecutará una sola vez al entrar a la pantalla.
    LaunchedEffect(Unit) {
        agendaModel.getDiasEntrega()
    }

    //val completedDeliveries by clienteModel.completedDeliveriesState.collectAsStateWithLifecycle() // Usa la dependencia correcta

    ScreenWithBackButtonWrapper(
        navController = navController,
        title = stringResource(R.string.agenda_reparto_titulo) // Título más descriptivo
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp), // Padding general para el contenido
            horizontalAlignment = Alignment.CenterHorizontally,
            // --- ESTILO: Espaciado consistente entre elementos ---
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded = expandedDias,
                onExpandedChange = { expandedDias = !expandedDias },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = diaSeleccionado,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.dia_semana_label)) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDias)
                    },
                    colors = ExposedDropdownMenuDefaults.textFieldColors(),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expandedDias,
                    onDismissRequest = { expandedDias = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    diasSemana.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                diaSeleccionado = selectionOption
                                expandedDias = false
                                // Log no necesita cambio de estilo
                                Log.d("AgendaScreen", "Dia seleccionado: $diaSeleccionado")
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (diasDeClientes.success == true) {
                    // Filtrar la lista de clientes aquí, en la UI
                    val clientesDelDia = diasDeClientes.clientes.filter {
                        it.diasEntrega.contains(diaSeleccionado)
                    }

                    if (clientesDelDia.isEmpty()) {
                        Text(
                            text = stringResource(R.string.no_clientes_agendados_format, diaSeleccionado),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 24.dp)
                        )
                    } else {
                        clientesDelDia.forEach { cliente ->
                            val deliveryId = "${cliente.nombre}-${cliente.direccion}-${fechaDeHoyString}"
                            //val isCompleted = completedDeliveries.contains(deliveryId)


                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                // --- ESTILO: Usar colores del tema para la elevación y el contenedor ---
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            start = 8.dp,
                                            end = 16.dp,
                                            top = 16.dp,
                                            bottom = 16.dp
                                        ),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    /*Checkbox(
                                        checked = isCompleted,
                                        onCheckedChange = {
                                            // Llamar a la función del ViewModel para cambiar el estado
                                            clienteModel.toggleDeliveryStatus(cliente.nombre, fechaDeHoyString)
                                        }
                                    )*/
                                    Column(
                                        modifier = Modifier.padding(start = 8.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = cliente.nombre,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = cliente.direccion, // Quitando "Dirección:"
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = stringResource(R.string.dias_entrega_format, cliente.diasEntrega.joinToString(", ")),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}