package com.example.sodappcomposse.Caja

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sodappcomposse.API.ApiServices
import com.example.sodappcomposse.API.RetrofitInstance
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


sealed class CajaUiState{
    object Idle: CajaUiState()
    object Loading: CajaUiState()
    data class Error(val message: String): CajaUiState()
    data class Success(val message: String): CajaUiState()
}

@HiltViewModel
class CajaViewModel @Inject constructor(
    private val apiServices: ApiServices // Hilt lo inyecta solo
) : ViewModel() {
    private val TAG = "CajaViewModel"

    private val _caja = MutableStateFlow<DataCajaResponse>(DataCajaResponse(success = false ,emptyList()))
    val caja: StateFlow<DataCajaResponse> = _caja.asStateFlow()

    var _mesSeleccionadoUi = MutableStateFlow<Meses?>(null)
    val mesSeleccionadoUi: StateFlow<Meses?> = _mesSeleccionadoUi.asStateFlow()

    var cajaUiState: CajaUiState = CajaUiState.Idle
        private set


    fun seleccionarMes(mes: Meses) {
        if (_mesSeleccionadoUi.value == mes) {
            Log.d(TAG, "VM: Mes seleccionado ya es: ${mes.name}")
            return
        }
        _mesSeleccionadoUi.value = mes
    }

    fun getCajaPorMes() {
        val mesNum = _mesSeleccionadoUi.value?.numero ?: return

        cajaUiState = CajaUiState.Loading
        _caja.value = DataCajaResponse(success = false, emptyList()) // Limpiar datos anteriores

        viewModelScope.launch {
            cajaUiState = CajaUiState.Loading
            try {
                val response = apiServices.getCajaPorMes(mesNum)

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        if (_mesSeleccionadoUi.value?.numero == mesNum) {
                            _caja.value = responseBody
                            cajaUiState = CajaUiState.Success("Datos cargados para ${_mesSeleccionadoUi.value?.name}")
                        } else {
                            Log.w(TAG, "VM: Datos recibidos para $mesNum, pero el mes seleccionado cambió a ${_mesSeleccionadoUi.value?.name}. Descartando actualización de UI.")
                        }
                    } else {
                        if (_mesSeleccionadoUi.value?.numero == mesNum) {
                            cajaUiState = CajaUiState.Error("Respuesta exitosa pero cuerpo nulo.")
                        }
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Sin cuerpo de error"
                    if (_mesSeleccionadoUi.value?.numero == mesNum) {
                        cajaUiState = CajaUiState.Error("Error API: ${response.code()} - ${response.message()}")
                    }
                }
            } catch (e: Exception) {
                if (_mesSeleccionadoUi.value?.numero == mesNum) {
                    cajaUiState = CajaUiState.Error("Excepción: ${e.message?.take(100)}")
                }
            }
        }
    }
}

