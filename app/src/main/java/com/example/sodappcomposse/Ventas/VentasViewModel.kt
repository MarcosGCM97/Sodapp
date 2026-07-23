package com.example.sodappcomposse.Ventas

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sodappcomposse.Cliente.ClienteRepository
import com.example.sodappcomposse.Producto.ProductoVenta
import com.example.sodappcomposse.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.flow.first

sealed class VentasUiState{
    object Idle: VentasUiState()
    object Loading: VentasUiState()
    data class Error(val message: String): VentasUiState()
    data class Success(val message: String): VentasUiState()
}
@HiltViewModel
class VentasViewModel @Inject constructor(
    private val ventaRepository: VentaRepository,
    private val clienteRepository: ClienteRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    private val TAG = "VentasViewModel"

    private val _ventas = mutableStateListOf<Venta>()
    val ventas: List<Venta> = _ventas

    var ventasUiState: VentasUiState by mutableStateOf(VentasUiState.Idle)
        private set

    private val _ventasPorClienteId = MutableStateFlow<List<Venta>>(emptyList())
    val ventasPorClienteId: StateFlow<List<Venta>> = _ventasPorClienteId.asStateFlow()

    fun getVentasByClienteId(idCl: String) {
        viewModelScope.launch {
            try {
                ventasUiState = VentasUiState.Loading

                // 1. Obtener el ID del usuario actual
                val idUsuario = userPreferencesRepository.userId.first() ?: "0"

                // 2. Pasar idUsuario a la llamada de la API
                val response = ventaRepository.getVentasByClienteId(idCl.toInt(), idUsuario)

                if (response.isSuccessful) {
                    response.body()?.let { responseBody ->
                        _ventasPorClienteId.value = responseBody.ventas
                        ventasUiState = VentasUiState.Success("Ventas cargadas para usuario $idUsuario")
                    } ?: run {
                        ventasUiState = VentasUiState.Error("Respuesta exitosa pero cuerpo nulo.")
                    }
                } else {
                    ventasUiState = VentasUiState.Error("Error servidor: ${response.code()}")
                }
            } catch (e: IOException) {
                _ventasPorClienteId.value = emptyList()
                ventasUiState = VentasUiState.Error("Error de red: ${e.message}")
            } catch (e: HttpException) {
                _ventasPorClienteId.value = emptyList()
                ventasUiState = VentasUiState.Error("Error HTTP: ${e.code()}")
            } catch (e: Exception) {
                _ventasPorClienteId.value = emptyList()
                ventasUiState = VentasUiState.Error("Excepción: ${e.message}")
            }
        }
    }

    internal fun getVentas() {
        viewModelScope.launch {
            try {
                ventasUiState = VentasUiState.Loading

                // 1. Obtener el ID del usuario actual
                val idUsuario = userPreferencesRepository.userId.first() ?: "0"

                // 2. Llamar a la API enviando el usuario
                val response = ventaRepository.getVentas(idUsuario)

                if (response.isSuccessful) {
                    response.body()?.let { ventaApi ->
                        _ventas.clear()
                        _ventas.addAll(ventaApi.ventas)
                        ventasUiState = VentasUiState.Success("Ventas de $idUsuario cargadas")
                    } ?: run {
                        ventasUiState = VentasUiState.Error("Cuerpo de respuesta nulo")
                    }
                } else {
                    ventasUiState = VentasUiState.Error("Error: ${response.code()}")
                }
            } catch (e: IOException) {
                ventasUiState = VentasUiState.Error("Error de red: ${e.message}")
            } catch (e: HttpException) {
                ventasUiState = VentasUiState.Error("Error HTTP: ${e.code()}")
            } catch (e: Exception) {
                ventasUiState = VentasUiState.Error("Error inesperado: ${e.message}")
            }
        }
    }

    internal suspend fun postVenta(clienteId: Int, productos: List<ProductoVenta>){
        val idUsuario = userPreferencesRepository.userId.first() ?: "0"

        val ventaParaApi = VentaRequest(clienteId, productos, idUsuario)

        viewModelScope.launch {
            try {
                val response = ventaRepository.postVenta(ventaParaApi)
                if (response.isSuccessful && response.body() != null) {
                    // Producir efecto de éxito
                    ventasUiState = VentasUiState.Success("Venta procesada exitosamente")
                } else {
                    ventasUiState = VentasUiState.Error("Error en la respuesta: ${response.code()} - ${response.message()}")
                }
            } catch (e: IOException) {
                ventasUiState = VentasUiState.Error("Error de red: ${e.message}")
            } catch (e: HttpException) {
                ventasUiState = VentasUiState.Error("Error HTTP: ${e.code()}")
            } catch (e: Exception) {
                ventasUiState = VentasUiState.Error("Error inesperado: ${e.message?.take(100)}")
            }
        }
    }

    internal fun eliminarVenta(idVenta: Int, clienteId: Int, valorVenta: Double){
        viewModelScope.launch {
            try {
                val response = ventaRepository.deleteVenta(idVenta)
                if (response.isSuccessful) {
                    ventasUiState = VentasUiState.Success("Venta eliminada exitosamente")
                    // actualizar deuda del cliente
                    clienteRepository.updateDeudaCliente(clienteId, valorVenta)
                    getVentasByClienteId(clienteId.toString())
                } else {
                    ventasUiState = VentasUiState.Error("Error en la respuesta: ${response.code()} - ${response.message()}")
                }
            } catch (e: IOException) {
                ventasUiState = VentasUiState.Error("Error de red: ${e.message}")
            } catch (e: HttpException) {
                ventasUiState = VentasUiState.Error("Error HTTP: ${e.code()}")
            } catch (e: Exception) {
                ventasUiState = VentasUiState.Error("Error inesperado: ${e.message?.take(100)}")
            }
        }
    }
}
