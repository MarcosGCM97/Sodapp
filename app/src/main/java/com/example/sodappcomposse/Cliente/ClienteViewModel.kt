package com.example.sodappcomposse.Cliente

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

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
@HiltViewModel
class ClientesViewModel @Inject constructor(
    private val clienteRepository: ClienteRepository
) : ViewModel() {
    private val TAG = "ClienteViewModel"

    private val _clientes = mutableStateListOf<Cliente>()
    val clientes: List<Cliente> = _clientes // Exponer como lista inmutable (pero observable)

    var clienteUiState: ClienteUiState by mutableStateOf(ClienteUiState.Idle)
        private set // Solo modificable desde el ViewModel

    val clienteParaDropDown : MutableState<Cliente?> = mutableStateOf(null)

    var clienteById : MutableState<Cliente?> = mutableStateOf(null)

    internal fun getClienteById(idCl: String){

        viewModelScope.launch {
            try {
                val response = clienteRepository.getClienteById(idCl.toInt())
                if (response.isSuccessful) {
                    response.body()?.let { clientesApi ->
                        clienteById.value = clientesApi.cliente
                        clienteUiState = ClienteUiState.Success("Cliente cargado")
                    } ?: run {
                        clienteUiState = ClienteUiState.Error("Respuesta exitosa pero cuerpo nulo.")
                    }
                } else {
                    clienteUiState = ClienteUiState.Error("Error servidor: ${response.code()}")
                }
            } catch (e: IOException) {
                clienteUiState = ClienteUiState.Error("Error de red: ${e.message}")
            } catch (e: HttpException) {
                clienteUiState = ClienteUiState.Error("Error HTTP: ${e.code()}")
            } catch (e: Exception) {
                clienteUiState = ClienteUiState.Error("Error inesperado: ${e.message}")
            }
        }
    }

    internal fun getClientes(){
        viewModelScope.launch {
            clienteUiState = ClienteUiState.Loading // Es buena practica
            try {
                val response = clienteRepository.getClientes()

                if (response.isSuccessful) {
                    response.body()?.let { clientesApi ->
                        _clientes.clear()
                        _clientes.addAll(clientesApi.clientes)
                        clienteUiState = ClienteUiState.Success("Clientes cargados: ${_clientes.size}")
                    } ?: run {
                        clienteUiState = ClienteUiState.Error("Respuesta exitosa pero cuerpo nulo.")
                    }
                } else {
                    clienteUiState = ClienteUiState.Error("Error servidor: ${response.code()}")
                }
            } catch (e: IOException) {
                clienteUiState = ClienteUiState.Error("Error de Red: Verifica tu conexión.")
            } catch (e: HttpException) {
                clienteUiState = ClienteUiState.Error("Error HTTP: ${e.code()}")
            } catch (e: Exception) {
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
                val objCliente = ClienteRequest(
                    nombreCl = nombre,
                    direccionCl = direccion,
                    numTelCl = telefono
                )
                val response = clienteRepository.postCliente(objCliente)
                if (response.isSuccessful && response.body() != null) {
                    _addClienteUiState.value = AddClienteUiState.Success("Cliente '$nombre' guardado exitosamente.")
                    getClientes()
                } else {
                    _addClienteUiState.value = AddClienteUiState.Error("Error en la respuesta: ${response.code()} - ${response.message()}")
                }
            } catch (e: IOException) {
                _addClienteUiState.value = AddClienteUiState.Error("Error de red: ${e.message}")
            } catch (e: HttpException) {
                _addClienteUiState.value = AddClienteUiState.Error("Error HTTP: ${e.code()}")
            } catch (e: Exception) {
                _addClienteUiState.value = AddClienteUiState.Error("Error inesperado: ${e.message}")
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
                val response = clienteRepository.updateCliente(
                    cliente.idCl,
                    cliente.nombreCl,
                    cliente.direccionCl,
                    cliente.numTelCl
                )
                if (response.isSuccessful) {
                    _addClienteUiState.value =
                        AddClienteUiState.Success("Cliente editado exitosamente.")
                    getClientes()
                } else {
                    _addClienteUiState.value =
                        AddClienteUiState.Error("Error al editar cliente: ${response.code()} - ${response.message()}")
                }
            } catch (e: IOException) {
                _addClienteUiState.value = AddClienteUiState.Error("Error de red al editar cliente: ${e.message}")
            } catch (e: HttpException) {
                _addClienteUiState.value = AddClienteUiState.Error("Error HTTP al editar cliente: ${e.code()}")
            } catch (e: Exception) {
                _addClienteUiState.value = AddClienteUiState.Error("Error al editar cliente: ${e.message}")
            }
        }
    }

    fun eliminarCliente(cliente: Cliente) {
        // Cambiamos el estado a Loading para mostrar feedback si es necesario
        _addClienteUiState.value = AddClienteUiState.Loading

        viewModelScope.launch {
            try {
                // Llamada a la API
                val response = clienteRepository.eliminarCliente(cliente.idCl)

                if (response.isSuccessful) {
                    // Si la eliminación en el servidor fue exitosa
                    _addClienteUiState.value =
                        AddClienteUiState.Success("Cliente '${cliente.nombreCl}' eliminado correctamente.")

                    // IMPORTANTE: Refrescar la lista local inmediatamente
                    getClientes()
                } else {
                    // Error devuelto por el servidor (ej: 404, 500)
                    _addClienteUiState.value =
                        AddClienteUiState.Error("Error al eliminar: ${response.code()} - ${response.message()}")
                }
            } catch (e: IOException) {
                _addClienteUiState.value = AddClienteUiState.Error("Error de red: Verifica tu conexión a internet.")
            } catch (e: HttpException) {
                _addClienteUiState.value = AddClienteUiState.Error("Error HTTP: ${e.code()}")
            } catch (e: Exception) {
                _addClienteUiState.value = AddClienteUiState.Error("Error inesperado: ${e.localizedMessage}")
            }
        }
    }
}
