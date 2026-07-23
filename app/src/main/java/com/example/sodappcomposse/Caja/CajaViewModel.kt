package com.example.sodappcomposse.Caja

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject


sealed class CajaUiState{
    object Idle: CajaUiState()
    object Loading: CajaUiState()
    data class Error(val message: String): CajaUiState()
    data class Success(val message: String): CajaUiState()
}

@HiltViewModel
class CajaViewModel @Inject constructor(
    private val cajaRepository: CajaRepository
) : ViewModel() {
    private val TAG = "CajaViewModel"

    private val _caja = MutableStateFlow<DataCajaResponse>(DataCajaResponse(success = false, caja = emptyList()))
    val caja: StateFlow<DataCajaResponse> = _caja.asStateFlow()

    var _mesSeleccionadoUi = MutableStateFlow<Meses?>(null)
    val mesSeleccionadoUi: StateFlow<Meses?> = _mesSeleccionadoUi.asStateFlow()

    var cajaUiState: CajaUiState by mutableStateOf(CajaUiState.Idle)
        private set


    fun seleccionarMes(mes: Meses) {
        if (_mesSeleccionadoUi.value == mes) {
            return
        }
        _mesSeleccionadoUi.value = mes
    }

    fun getCajaPorMes() {
        val mesNum = _mesSeleccionadoUi.value?.numero ?: return

        cajaUiState = CajaUiState.Loading
        _caja.value = DataCajaResponse(success = false, caja = emptyList()) // Limpiar datos anteriores

        viewModelScope.launch {
            cajaUiState = CajaUiState.Loading
            try {
                val response = cajaRepository.getCajaPorMes(mesNum)

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        if (_mesSeleccionadoUi.value?.numero == mesNum) {
                            _caja.value = responseBody
                            cajaUiState = CajaUiState.Success("Datos cargados para ${_mesSeleccionadoUi.value?.name}")
                        }
                    } else {
                        if (_mesSeleccionadoUi.value?.numero == mesNum) {
                            cajaUiState = CajaUiState.Error("Respuesta exitosa pero cuerpo nulo.")
                        }
                    }
                } else {
                    if (_mesSeleccionadoUi.value?.numero == mesNum) {
                        cajaUiState = CajaUiState.Error("Error API: ${response.code()} - ${response.message()}")
                    }
                }
            } catch (e: IOException) {
                if (_mesSeleccionadoUi.value?.numero == mesNum) {
                    cajaUiState = CajaUiState.Error("Error de red: ${e.message?.take(100)}")
                }
            } catch (e: HttpException) {
                if (_mesSeleccionadoUi.value?.numero == mesNum) {
                    cajaUiState = CajaUiState.Error("Error HTTP: ${e.code()}")
                }
            } catch (e: Exception) {
                if (_mesSeleccionadoUi.value?.numero == mesNum) {
                    cajaUiState = CajaUiState.Error("Excepción: ${e.message?.take(100)}")
                }
            }
        }
    }
}

