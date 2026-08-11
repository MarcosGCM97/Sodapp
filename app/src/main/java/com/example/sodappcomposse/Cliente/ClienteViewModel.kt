package com.example.sodappcomposse.Cliente

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sodappcomposse.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

// Define estos estados si quieres dar feedback más específico al usuario
sealed interface AddClienteUiState {
    object Idle : AddClienteUiState
    object Loading : AddClienteUiState
    data class Success(@StringRes val messageRes: Int, val args: Array<Any> = emptyArray()) : AddClienteUiState {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Success) return false
            if (messageRes != other.messageRes) return false
            if (!args.contentEquals(other.args)) return false
            return true
        }
        override fun hashCode(): Int {
            var result = messageRes
            result = 31 * result + args.contentHashCode()
            return result
        }
    }
    data class Error(@StringRes val messageRes: Int, val args: Array<Any> = emptyArray()) : AddClienteUiState {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Error) return false
            if (messageRes != other.messageRes) return false
            if (!args.contentEquals(other.args)) return false
            return true
        }
        override fun hashCode(): Int {
            var result = messageRes
            result = 31 * result + args.contentHashCode()
            return result
        }
    }
}

sealed class ClienteUiState {
    object Idle : ClienteUiState() //Estado inicial
    object Loading : ClienteUiState() //Cargando
    data class Error(@StringRes val messageRes: Int, val args: Array<Any> = emptyArray()) : ClienteUiState() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Error) return false
            if (messageRes != other.messageRes) return false
            if (!args.contentEquals(other.args)) return false
            return true
        }
        override fun hashCode(): Int {
            var result = messageRes
            result = 31 * result + args.contentHashCode()
            return result
        }
    }
    data class Success(@StringRes val messageRes: Int, val args: Array<Any> = emptyArray()) : ClienteUiState() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Success) return false
            if (messageRes != other.messageRes) return false
            if (!args.contentEquals(other.args)) return false
            return true
        }
        override fun hashCode(): Int {
            var result = messageRes
            result = 31 * result + args.contentHashCode()
            return result
        }
    }
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
                        clienteUiState = ClienteUiState.Success(R.string.cliente_cargado_success)
                    } ?: run {
                        clienteUiState = ClienteUiState.Error(R.string.cuerpo_nulo_error)
                    }
                } else {
                    clienteUiState = ClienteUiState.Error(R.string.error_servidor_format, arrayOf(response.code()))
                }
            } catch (e: IOException) {
                clienteUiState = ClienteUiState.Error(R.string.error_red_format, arrayOf(e.message ?: ""))
            } catch (e: HttpException) {
                clienteUiState = ClienteUiState.Error(R.string.error_http_format, arrayOf(e.code()))
            } catch (e: Exception) {
                clienteUiState = ClienteUiState.Error(R.string.error_inesperado_format, arrayOf(e.message ?: ""))
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
                        clienteUiState = ClienteUiState.Success(R.string.clientes_cargados_format, arrayOf(_clientes.size))
                    } ?: run {
                        clienteUiState = ClienteUiState.Error(R.string.cuerpo_nulo_error)
                    }
                } else {
                    clienteUiState = ClienteUiState.Error(R.string.error_servidor_format, arrayOf(response.code()))
                }
            } catch (e: IOException) {
                clienteUiState = ClienteUiState.Error(R.string.error_red_verificar)
            } catch (e: HttpException) {
                clienteUiState = ClienteUiState.Error(R.string.error_http_format, arrayOf(e.code()))
            } catch (e: Exception) {
                clienteUiState = ClienteUiState.Error(R.string.error_inesperado_format, arrayOf(e.message?.take(100) ?: ""))
            }
        }
    }

    // Estado para el resultado de agregar un cliente (opcional, para mostrar feedback)
    private val _addClienteUiState = mutableStateOf<AddClienteUiState>(AddClienteUiState.Idle)
    val addClienteUiState: State<AddClienteUiState> = _addClienteUiState

    internal fun agregarNuevoCliente(nombre: String, direccion: String, telefono: String) {
        if (nombre.isBlank() || direccion.isBlank() || telefono.isBlank()) {
            _addClienteUiState.value = AddClienteUiState.Error(R.string.campos_requeridos_error)
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
                    _addClienteUiState.value = AddClienteUiState.Success(R.string.cliente_guardado_success, arrayOf(nombre))
                    getClientes()
                } else {
                    _addClienteUiState.value = AddClienteUiState.Error(R.string.error_respuesta_format, arrayOf(response.code(), response.message()))
                }
            } catch (e: IOException) {
                _addClienteUiState.value = AddClienteUiState.Error(R.string.error_red_format, arrayOf(e.message ?: ""))
            } catch (e: HttpException) {
                _addClienteUiState.value = AddClienteUiState.Error(R.string.error_http_format, arrayOf(e.code()))
            } catch (e: Exception) {
                _addClienteUiState.value = AddClienteUiState.Error(R.string.error_inesperado_format, arrayOf(e.message ?: ""))
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
                        AddClienteUiState.Success(R.string.cliente_editado_success)
                    getClientes()
                } else {
                    _addClienteUiState.value =
                        AddClienteUiState.Error(R.string.error_editar_cliente_format, arrayOf(response.code(), response.message()))
                }
            } catch (e: IOException) {
                _addClienteUiState.value = AddClienteUiState.Error(R.string.error_red_editar_cliente, arrayOf(e.message ?: ""))
            } catch (e: HttpException) {
                _addClienteUiState.value = AddClienteUiState.Error(R.string.error_http_editar_cliente, arrayOf(e.code()))
            } catch (e: Exception) {
                _addClienteUiState.value = AddClienteUiState.Error(R.string.error_inesperado_format, arrayOf(e.message ?: ""))
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
                        AddClienteUiState.Success(R.string.cliente_eliminado_success, arrayOf(cliente.nombreCl))

                    // IMPORTANTE: Refrescar la lista local inmediatamente
                    getClientes()
                } else {
                    // Error devuelto por el servidor (ej: 404, 500)
                    _addClienteUiState.value =
                        AddClienteUiState.Error(R.string.error_eliminar_format, arrayOf(response.code(), response.message()))
                }
            } catch (e: IOException) {
                _addClienteUiState.value = AddClienteUiState.Error(R.string.error_red_conexion)
            } catch (e: HttpException) {
                _addClienteUiState.value = AddClienteUiState.Error(R.string.error_http_format, arrayOf(e.code()))
            } catch (e: Exception) {
                _addClienteUiState.value = AddClienteUiState.Error(R.string.error_inesperado_format, arrayOf(e.localizedMessage ?: ""))
            }
        }
    }
}
