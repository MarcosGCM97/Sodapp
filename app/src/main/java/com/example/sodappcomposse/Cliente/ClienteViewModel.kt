package com.example.sodappcomposse.Cliente

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sodappcomposse.API.ApiServices
import com.example.sodappcomposse.API.RetrofitInstance
import com.example.sodappcomposse.UserPreferencesRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// Define estos estados si quieres dar feedback más específico al usuario
sealed interface AddClienteUiState {
    object Idle : AddClienteUiState
    object Loading : AddClienteUiState
    data class Success(val message: String) : AddClienteUiState
    data class Error(val message: String) : AddClienteUiState
}

sealed class ClienteUiState {
    object Idle : ClienteUiState() //Estado inicial
    object Loading : ClienteUiState() //Cargando
    data class Error(val message: String) : ClienteUiState() //Error
    data class Success(val message: String) : ClienteUiState() //Éxito
}

@RequiresApi(Build.VERSION_CODES.O)
class ClientesViewModel(
    private val apiServices: ApiServices = RetrofitInstance.api,
    //private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    private val TAG = "ClienteViewModel"

    private val _clientes = mutableStateListOf<Cliente>()
    val clientes: List<Cliente> = _clientes // Exponer como lista inmutable (pero observable)

    var clienteUiState: ClienteUiState by mutableStateOf(ClienteUiState.Idle)
        private set // Solo modificable desde el ViewModel

    val clienteParaDropDown : MutableState<Cliente?> = mutableStateOf(null)

    var clienteById : MutableState<Cliente?> = mutableStateOf(null)

    val diasEntrega: MutableState<TodosLosDias> = mutableStateOf(TodosLosDias())
    val diasEntregaById: MutableState<List<String>> = mutableStateOf(emptyList())

    /*init {
        // Llama a la función de limpieza cuando el ViewModel se crea por primera vez.
        cleanupOldDeliveries()
    }*/

    fun getClienteById(idCl: String){

        viewModelScope.launch {
            val response = apiServices.getClienteById(idCl.toInt())

            if (response.isSuccessful) {
                val clientesApi = response.body()!!
                if (response.body() !== null) {
                    clienteById.value = clientesApi
                    clienteUiState =
                        ClienteUiState.Success("Cliente cargado: ${_clientes.size}")
                } else {
                    //Log.e(TAG, "Respuesta exitosa pero cuerpo nulo.")
                    clienteUiState = ClienteUiState.Error("Respuesta exitosa pero cuerpo nulo.")
                }
            }
        }
    }

    internal fun getClientes(){
        //if (clienteUiState is ClienteUiState.Loading) return // Evitar llamadas múltiples si ya está cargando

        viewModelScope.launch {
            clienteUiState = ClienteUiState.Loading // Es buena practica
            try {
                val response = apiServices.getClientes()

                if (response.isSuccessful) {
                    val clientesApi = response.body()!!
                    if (response.body() !== null) {
                        _clientes.clear()
                        _clientes.addAll(clientesApi)
                        clienteUiState =
                            ClienteUiState.Success("Clientes cargados: ${_clientes.size}")
                    } else {
                        //Log.e(TAG, "Respuesta exitosa pero cuerpo nulo.")
                        clienteUiState = ClienteUiState.Error("Respuesta exitosa pero cuerpo nulo.")
                    }

                } else {
                    //Log.e(TAG, "Error en la respuesta: ${response.code()} - ${response.message()}")
                }
            }catch (e: HttpException) {
                //Log.e(TAG, "Error HTTP en la solicitud: ${e.code()} - ${e.message()}", e)
                clienteUiState = ClienteUiState.Error("Error HTTP: ${e.message()}")
            } catch (e: IOException) {
                //Log.e(TAG, "Error de Red/IO en la solicitud: ${e.message}", e)
                clienteUiState = ClienteUiState.Error("Error de Red: Verifica tu conexión.")
            } catch (e: Exception) {
                //Log.e(TAG, "Error general en la solicitud: ${e.message}", e)
                clienteUiState = ClienteUiState.Error("Error inesperado: ${e.message?.take(100)}")
            }
        }
    }

    // Estado para el resultado de agregar un cliente (opcional, para mostrar feedback)
    private val _addClienteUiState = mutableStateOf<AddClienteUiState>(AddClienteUiState.Idle)
    val addClienteUiState: State<AddClienteUiState> = _addClienteUiState

    internal fun agregarNuevoCliente(nombre: String, direccion: String, telefono: String) {
        if (nombre.isBlank() || direccion.isBlank() || telefono.isBlank()) {
            _addClienteUiState.value = AddClienteUiState.Error("Todos los campos son requeridos.")
            return
        }

        _addClienteUiState.value = AddClienteUiState.Loading
        viewModelScope.launch {
            try {
                var objCliente = ClienteRequest(
                    nombreCl = nombre,
                    direccionCl = direccion,
                    numTelCl = telefono
                )
                val response = apiServices.postCliente(objCliente)
                if (response.isSuccessful && response.body() != null){
                    val clientes = response.body()!!
                    //Procesar la lista de clientes
                    _addClienteUiState.value = AddClienteUiState.Success("Cliente '$nombre' guardado exitosamente.")

                    getClientes()
                }
                else{
                    //Manejar error de la API
                    //Log.e(TAG, "Error en la respuesta: ${response.code()} - ${response.message()}")
                    _addClienteUiState.value = AddClienteUiState.Error("Error en la respuesta: ${response.code()} - ${response.message()}")
                }

            } catch (e: Exception) {
                //Log.e("ClientesViewModel", "Error al guardar cliente: ${e.message}", e)
                _addClienteUiState.value = AddClienteUiState.Error("Error al guardar: ${e.message}")
            } catch (e: HttpException) {
                //Log.e("ClientesViewModel", "Error HTTP al guardar cliente: ${e.code()} - ${e.message()}", e)
                _addClienteUiState.value = AddClienteUiState.Error("Error HTTP: ${e.code()} - ${e.message()}")
            }
        }
    }

    fun pagarDeudaCliente(idCl: Int, deuda: Double){
        viewModelScope.launch {
            try {
                val response = apiServices.updateDeudaCliente(idCl, deuda)
                if (response.isSuccessful) {
                    //Log.d("ClientesViewModel", "Deuda pagada exitosamente.")
                    _addClienteUiState.value =
                        AddClienteUiState.Success("Deuda pagada exitosamente.")
                } else {
                    //Log.e("ClientesViewModel", "Error al pagar la deuda: ${response.code()} - ${response.message()}")
                    _addClienteUiState.value =
                        AddClienteUiState.Error("Error al pagar la deuda: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                //Log.e("ClientesViewModel", "Error al pagar la deuda: ${e.message}", e)
                _addClienteUiState.value =
                    AddClienteUiState.Error("Error al pagar la deuda: ${e.message}")
            } catch (e: HttpException) {
                //Log.e("ClientesViewModel", "Error HTTP al pagar la deuda: ${e.code()} - ${e.message()}", e)
                _addClienteUiState.value =
                    AddClienteUiState.Error("Error HTTP al pagar la deuda: ${e.code()} - ${e.message()}")
            }
        }
    }

    // Function to reset the add client state if needed
    fun resetAddClienteState() {
        _addClienteUiState.value = AddClienteUiState.Idle
    }

    // Funciones para la lógica de edición (ejemplos)
    fun editarCliente(cliente: Cliente) {
        viewModelScope.launch {
            try {
                val response = apiServices.updateCliente(
                    cliente.idCl,
                    cliente.nombreCl,
                    cliente.direccionCl,
                    cliente.numTelCl
                )
                if (response.isSuccessful) {
                    //Log.d("ClientesViewModel", "Cliente editado exitosamente.")
                    _addClienteUiState.value =
                        AddClienteUiState.Success("Cliente editado exitosamente.")
                    getClientes()
                } else {
                    //Log.e("ClientesViewModel", "Error al editar cliente: ${response.code()} - ${response.message()}")
                    _addClienteUiState.value =
                        AddClienteUiState.Error("Error al editar cliente: ${response.code()} - ${response.message()}")
                }
            } catch (e: HttpException){
                //Log.e("ClientesViewModel", "Error HTTP al editar cliente: ${e.code()} - ${e.message()}", e)
                _addClienteUiState.value =
                    AddClienteUiState.Error("Error HTTP al editar cliente: ${e.code()} - ${e.message()}")
            } catch (e: IOException) {
                //Log.e("ClientesViewModel", "Error de red al editar cliente: ${e.message}", e)
                _addClienteUiState.value =
                    AddClienteUiState.Error("Error de red al editar cliente: ${e.message}")
            } catch (e: Exception) {
                //Log.e("ClientesViewModel", "Error al editar cliente: ${e.message}", e)
                _addClienteUiState.value =
                    AddClienteUiState.Error("Error al editar cliente: ${e.message}")
            }
        }
    }

    fun getDiasEntrega(){//SEGUIR ACAAAAA
        viewModelScope.launch {
            try {
                val response = apiServices.getDiasEntrega()
                if(response.isSuccessful){
                    diasEntrega.value = response.body()!!
                    Log.d("ClientesViewModel", response.body().toString())
                }else{
                    Log.e("ClientesViewModel", "Error al obtener días de entrega: ${response.code()} - ${response.message()}")
                    _addClienteUiState.value =
                        AddClienteUiState.Error("Error al obtener días de entrega: ${response.code()} - ${response.message()}")
                }
            }catch (e: Exception){
                Log.e("ClientesViewModel", "Error al obtener días de entrega: ${e.message}", e)
                _addClienteUiState.value =
                    AddClienteUiState.Error("Error al obtener días de entrega: ${e.message}")
            }
        }
    }

    fun getDiasEntregaById(idCl: Int){
        viewModelScope.launch {
            try {
                val response = apiServices.getDiasEntregaById(idCl)
                if(response.isSuccessful){
                    diasEntregaById.value = response.body()!!.diasEntrega
                    Log.d("ClientesViewModel", response.body().toString())
                }else{
                    Log.e("ClientesViewModel", "Error al obtener días de entrega: ${response.code()} - ${response.message()}")
                    _addClienteUiState.value =
                        AddClienteUiState.Error("Error al obtener días de entrega: ${response.code()} - ${response.message()}")
                }
            }catch (e: Exception){
                Log.e("ClientesViewModel", "Error al obtener días de entrega: ${e.message}", e)
                _addClienteUiState.value =
                    AddClienteUiState.Error("Error al obtener días de entrega: ${e.message}")
            }
        }
    }

    fun updateDiasEntrega(clienteId: Int?, diasSeleccionados: List<String>) {
        if (clienteId == null) {
            return
        }
        _addClienteUiState.value = AddClienteUiState.Loading
        var diasEntrega = DiasEntrega(clienteId, diasSeleccionados)

        viewModelScope.launch {
            try {
                val response = apiServices.updateDiasEntrega(diasEntrega)
                if (response.isSuccessful) {
                    Log.d("ClientesViewModel", "Días de entrega actualizados exitosamente.")
                    _addClienteUiState.value =
                        AddClienteUiState.Success("Días de entrega actualizados exitosamente.")
                    getClientes()
                }
                else {
                    Log.e("ClientesViewModel", "Error al actualizar días de entrega: ${response.code()} - ${response.message()}")
                    _addClienteUiState.value =
                        AddClienteUiState.Error("Error al actualizar días de entrega: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("ClientesViewModel", "Error al actualizar días de entrega: ${e.message}", e)
                _addClienteUiState.value =
                    AddClienteUiState.Error("Error al actualizar días de entrega: ${e.message}")
            }
        }
    }
/*
    // Exponer las entregas completadas como un StateFlow para que la UI pueda observarlo
    val completedDeliveriesState: StateFlow<Set<String>> = userPreferencesRepository.completedDeliveries
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptySet()
        )

    // Función que la UI llamará para cambiar el estado de una entrega
    fun toggleDeliveryStatus(clienteId: String, fecha: String) { // Ahora recibe 'fecha'
        viewModelScope.launch {
            // El identificador se construye con la fecha que viene de la UI
            val deliveryId = "$clienteId-$fecha"
            val currentCompleted = completedDeliveriesState.value

            if (currentCompleted.contains(deliveryId)) {
                userPreferencesRepository.removeCompletedDelivery(deliveryId)
            } else {
                userPreferencesRepository.addCompletedDelivery(deliveryId)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun cleanupOldDeliveries() {
        viewModelScope.launch {
            val hoy = LocalDate.now()
            // Solo mantenemos las entregas de los últimos 7 días, por ejemplo.
            val fechaLimite = hoy.minusDays(7)

            val entregasActuales = userPreferencesRepository.completedDeliveries.first() // Obtenemos el valor actual
            val entregasLimpias = entregasActuales.filter { deliveryId ->
                try {
                    // Extraemos la parte de la fecha del ID, ej: "12-2025-10-01" -> "2025-10-01"
                    val fechaString = deliveryId.substringAfterLast("-", "")
                    if (fechaString.isNotEmpty()){
                        val fechaEntrega = LocalDate.parse(fechaString, DateTimeFormatter.ISO_LOCAL_DATE)
                        // Mantenemos la entrega si es posterior o igual a la fecha límite
                        fechaEntrega.isAfter(fechaLimite) || fechaEntrega.isEqual(fechaLimite)
                    } else {
                        false // Si el ID no tiene el formato esperado, lo descartamos
                    }
                } catch (e: Exception) {
                    false // Si hay error al parsear, descartamos la entrada
                }
            }.toSet()

            // Si el set limpio es diferente al original, lo guardamos.
            if (entregasLimpias != entregasActuales) {
                userPreferencesRepository.saveCleanedDeliveries(entregasLimpias) // Necesitas esta nueva función
            }
        }
    }*/
}
/*
    fun resetClienteUiStateToIdle() { // Función para resetear el estado si es necesario
        clienteUiState = ClienteUiState.Idle
    }
}
*/