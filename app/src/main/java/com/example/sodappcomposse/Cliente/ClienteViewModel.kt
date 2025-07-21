package com.example.sodappcomposse.Cliente

import android.util.Log
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

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

class ClientesViewModel(
    private val apiServices: ApiServices = RetrofitInstance.api
) : ViewModel() {
    private val TAG = "ClienteViewModel"

    private val _clientes = mutableStateListOf<Cliente>()
    val clientes: List<Cliente> = _clientes // Exponer como lista inmutable (pero observable)

    var clienteUiState: ClienteUiState by mutableStateOf(ClienteUiState.Idle)
        private set // Solo modificable desde el ViewModel

    val clienteParaVenta : MutableState<Cliente?> = mutableStateOf(null)

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
                        Log.e(TAG, "Respuesta exitosa pero cuerpo nulo.")
                        clienteUiState = ClienteUiState.Error("Respuesta exitosa pero cuerpo nulo.")
                    }

                } else {
                    //Manejar error de la API
                    Log.e(TAG, "Error en la respuesta: ${response.code()} - ${response.message()}")
                }
            }catch (e: HttpException) {
                Log.e(TAG, "Error HTTP en la solicitud: ${e.code()} - ${e.message()}", e)
                clienteUiState = ClienteUiState.Error("Error HTTP: ${e.message()}")
            } catch (e: IOException) {
                // Error de red (sin conexión, timeout, etc.)
                Log.e(TAG, "Error de Red/IO en la solicitud: ${e.message}", e)
                clienteUiState = ClienteUiState.Error("Error de Red: Verifica tu conexión.")
            } catch (e: Exception) {
                // Otros errores (ej. parsing JSON si la estructura no coincide con Cliente)
                Log.e(TAG, "Error general en la solicitud: ${e.message}", e)
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
                    Log.e(TAG, "Error en la respuesta: ${response.code()} - ${response.message()}")
                    _addClienteUiState.value = AddClienteUiState.Error("Error en la respuesta: ${response.code()} - ${response.message()}")
                }

            } catch (e: Exception) {
                Log.e("ClientesViewModel", "Error al guardar cliente: ${e.message}", e)
                _addClienteUiState.value = AddClienteUiState.Error("Error al guardar: ${e.message}")
            } catch (e: HttpException) {
                Log.e("ClientesViewModel", "Error HTTP al guardar cliente: ${e.code()} - ${e.message()}", e)
                _addClienteUiState.value = AddClienteUiState.Error("Error HTTP: ${e.code()} - ${e.message()}")
            }
        }
    }

    // Function to reset the add client state if needed
    fun resetAddClienteState() {
        _addClienteUiState.value = AddClienteUiState.Idle
    }

    private val _clienteSeleccionadoParaEdicion = mutableStateOf<Cliente?>(null) // Asume que tienes una data class Cliente
    val clienteSeleccionadoParaEdicion: State<Cliente?> = _clienteSeleccionadoParaEdicion

    // Función para actualizar el cliente seleccionado
    fun seleccionarClienteParaEdicion(cliente: Cliente?) {
        _clienteSeleccionadoParaEdicion.value = cliente
    }

    // Funciones para la lógica de edición (ejemplos)
    fun actualizarCliente(clienteEditado: Cliente) {
        viewModelScope.launch {
            // Lógica para llamar a tu API o base de datos para actualizar el cliente
            Log.d("ClientesViewModel", "Actualizando cliente: ${clienteEditado.nombreCl}")
            // Después de actualizar, podrías refrescar la lista de clientes
            // y posiblemente limpiar el clienteSeleccionadoParaEdicion o actualizarlo con la nueva info
            // getClientes()
            // _clienteSeleccionadoParaEdicion.value = clienteActualizadoDesdeApi
        }
    }
}
/*
    fun resetClienteUiStateToIdle() { // Función para resetear el estado si es necesario
        clienteUiState = ClienteUiState.Idle
    }
}
*/