package com.example.sodappcomposse.Ventas

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sodappcomposse.API.ApiServices
import com.example.sodappcomposse.API.RetrofitInstance
import com.example.sodappcomposse.Producto.ProductoVenta
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

sealed class VentasUiState{
    object Idle: VentasUiState()
    object Loading: VentasUiState()
    data class Error(val message: String): VentasUiState()
    data class Success(val message: String): VentasUiState()
}

class VentasViewModel(
    private val apiServices: ApiServices = RetrofitInstance.api
) : ViewModel() {
    private val TAG = "VentasViewModel"

    private val _ventas = mutableStateListOf<DataVenta>()
    val ventas: List<DataVenta> = _ventas

    var ventasUiState: VentasUiState = VentasUiState.Idle
        private set

    internal fun getVentas() {
        viewModelScope.launch {
            val response = apiServices.getVentas()

            try {
                if (response.isSuccessful) {
                    val ventaApi = response.body()!!
                    if (response.body() !== null) {
                        _ventas.clear()
                        _ventas.addAll(ventaApi)
                        ventasUiState = VentasUiState.Success("Ventas cargadas: ${_ventas.size}")
                    } else {
                        Log.e(TAG, "Respuesta exitosa pero cuerpo nulo.")
                        ventasUiState = VentasUiState.Error("Respuesta exitosa pero cuerpo nulo.")
                    }

                } else{
                    Log.e(TAG, "Error en la respuesta: ${response.code()} - ${response.message()}")
                    ventasUiState = VentasUiState.Error("Error en la respuesta: ${response.code()} - ${response.message()}")
                }
            } catch (e: HttpException) {
                Log.e(TAG, "Error HTTP en la solicitud: ${e.code()} - ${e.message()}", e)
                ventasUiState = VentasUiState.Error("Error HTTP: ${e.message()}")
            } catch (e: IOException) {
                Log.e(TAG, "Error de Red/IO en la solicitud: ${e.message}", e)
                ventasUiState = VentasUiState.Error("Error de Red: Verifica tu conexión.")
            } catch (e: Exception) {
                Log.e(TAG, "Error general en la solicitud: ${e.message}", e)
                ventasUiState = VentasUiState.Error("Error inesperado: ${e.message?.take(100)}")
            }
        }
    }

    internal fun postVenta(clienteId: Int, productos: List<ProductoVenta>){
        val ventaParaApi = VentaRequest(clienteId, productos)

        viewModelScope.launch {
            try {
                val response = apiServices.postVenta(ventaParaApi)
                if (response.isSuccessful && response.body() != null) {
                    val ventas = response.body()!!
                    //Procesar la lista de ventas
                    ventasUiState = VentasUiState.Success("Venta procesada exitosamente")
                } else {
                    //Manejar error de la API
                    Log.e(TAG, "Error en la respuesta: ${response.code()} - ${response.message()}")
                    ventasUiState =
                        VentasUiState.Error("Error en la respuesta: ${response.code()} - ${response.message()}")
                }
            }catch (e: Exception){
                Log.e(TAG, "Error en la solicitud: ${e.message}", e)
                ventasUiState = VentasUiState.Error("Error inesperado: ${e.message?.take(100)}")
            }
        }
    }
}