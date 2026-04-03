package com.example.sodappcomposse.Ventas

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sodappcomposse.API.ApiServices
import com.example.sodappcomposse.API.RetrofitInstance
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
    private val apiServices: ApiServices, // Hilt lo inyecta solo
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    private val TAG = "VentasViewModel"

    private val _ventas = mutableStateListOf<VentaCompleta>()
    val ventas: List<VentaCompleta> = _ventas

    var ventasUiState: VentasUiState = VentasUiState.Idle
        private set

    private val _ventasPorClienteId = MutableStateFlow<List<VentaByClientId>>(emptyList())
    val ventasPorClienteId: StateFlow<List<VentaByClientId>> = _ventasPorClienteId.asStateFlow()

    fun getVentasByClienteId(idCl: String) {
        viewModelScope.launch {
            try {
                ventasUiState = VentasUiState.Loading

                // 1. Obtener el ID del usuario actual
                val idUsuario = userPreferencesRepository.userId.first() ?: "0"

                // 2. Pasar idUsuario a la llamada de la API
                val response = apiServices.getVentasByCienteId(idCl.toInt(), idUsuario)

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null && responseBody.success) {
                        _ventasPorClienteId.value = responseBody.ventas
                        ventasUiState = VentasUiState.Success("Ventas cargadas para usuario $idUsuario")
                    } else {
                        _ventasPorClienteId.value = emptyList()
                        ventasUiState = VentasUiState.Error("Error al obtener ventas del cliente.")
                    }
                } else {
                    ventasUiState = VentasUiState.Error("Error servidor: ${response.code()}")
                }
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
                val response = apiServices.getVentas(idUsuario)

                if (response.isSuccessful) {
                    val ventaApi = response.body()
                    if (ventaApi != null) {
                        _ventas.clear()
                        _ventas.addAll(ventaApi.ventas)
                        ventasUiState = VentasUiState.Success("Ventas de $idUsuario cargadas")
                    }
                } else {
                    ventasUiState = VentasUiState.Error("Error: ${response.code()}")
                }
            } catch (e: Exception) {
                ventasUiState = VentasUiState.Error("Error de red: ${e.message}")
            }
        }
    }

    internal suspend fun postVenta(clienteId: Int, productos: List<ProductoVenta>){
        val idUsuario = userPreferencesRepository.userId.first() ?: "0"

        val ventaParaApi = VentaRequest(clienteId, productos, idUsuario)

        viewModelScope.launch {
            try {
                val response = apiServices.postVenta(ventaParaApi)
                if (response.isSuccessful && response.body() != null) {
                    val ventas = response.body()!!
                    //Procesar la lista de ventas
                    ventasUiState = VentasUiState.Success("Venta procesada exitosamente")
                } else {
                    //Manejar error de la API
                    //Log.e(TAG, "Error en la respuesta: ${response.code()} - ${response.message()}")
                    ventasUiState =
                        VentasUiState.Error("Error en la respuesta: ${response.code()} - ${response.message()}")
                }
            }catch (e: Exception){
                //Log.e(TAG, "Error en la solicitud: ${e.message}", e)
                ventasUiState = VentasUiState.Error("Error inesperado: ${e.message?.take(100)}")
            }
        }
    }

    internal fun eliminarVenta(idVenta: Int, clienteId: Int, valorVenta: Double){
        viewModelScope.launch {
            try {
                val response = apiServices.deleteVenta(idVenta)
                if (response.isSuccessful) {
                    //Log.d(TAG, "Venta eliminada exitosamente")
                    ventasUiState = VentasUiState.Success("Venta eliminada exitosamente")

                    //actualizar deuda del cliente
                    apiServices.updateDeudaCliente(clienteId, valorVenta)

                    getVentasByClienteId(clienteId.toString())
                } else {
                    // Manejar errores de la API
                    //Log.e(TAG, "Error en la respuesta: ${response.code()} - ${response.message()}")
                    ventasUiState =
                        VentasUiState.Error("Error en la respuesta: ${response.code()} - ${response.message()}")
                }
            }catch (e: Exception) {
                //Log.e(TAG, "Error en la solicitud: ${e.message}", e)
                ventasUiState = VentasUiState.Error("Error inesperado: ${e.message?.take(100)}")
            }

        }
    }

    /*internal fun updateVenta(idVenta: Int, cantidad: Int){
        viewModelScope.launch {
            try {
                val response = apiServices.updateVenta(idVenta, cantidad)
                if (response.isSuccessful) {
                    // Actualización exitosa
                    //Log.d(TAG, "Venta actualizada exitosamente")
                    ventasUiState = VentasUiState.Success("Venta actualizada exitosamente")
                    getVentas()
                } else {
                    // Manejar errores de la API
                    //Log.e(TAG, "Error en la respuesta: ${response.code()} - ${response.message()}")
                    ventasUiState =
                        VentasUiState.Error("Error en la respuesta: ${response.code()} - ${response.message()}")
                }
            }catch (e: Exception) {
                    //Log.e(TAG, "Error en la solicitud: ${e.message}", e)
                    ventasUiState = VentasUiState.Error("Error inesperado: ${e.message?.take(100)}")
            }
        }
    }*/
}