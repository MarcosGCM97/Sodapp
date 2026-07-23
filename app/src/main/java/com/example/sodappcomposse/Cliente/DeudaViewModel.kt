package com.example.sodappcomposse.Cliente

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

sealed interface DeudaUiState {
    object Idle : DeudaUiState
    object Loading : DeudaUiState
    data class Success(val message: String) : DeudaUiState
    data class Error(val message: String) : DeudaUiState
}

@HiltViewModel
class DeudaViewModel @Inject constructor(
    private val clienteRepository: ClienteRepository
) : ViewModel() {

    private val _uiState = mutableStateOf<DeudaUiState>(DeudaUiState.Idle)
    val uiState: State<DeudaUiState> = _uiState

    fun pagarDeudaCliente(idCl: Int, deuda: Double) {
        _uiState.value = DeudaUiState.Loading
        viewModelScope.launch {
            try {
                val response = clienteRepository.updateDeudaCliente(idCl, deuda)
                if (response.isSuccessful) {
                    _uiState.value = DeudaUiState.Success("Pago procesado exitosamente")
                } else {
                    _uiState.value = DeudaUiState.Error("Error al procesar pago: ${response.code()}")
                }
            } catch (e: IOException) {
                _uiState.value = DeudaUiState.Error("Error de red: ${e.message}")
            } catch (e: HttpException) {
                _uiState.value = DeudaUiState.Error("Error HTTP: ${e.code()}")
            } catch (e: Exception) {
                _uiState.value = DeudaUiState.Error("Error inesperado: ${e.message}")
            }
        }
    }

    fun resetState() {
        _uiState.value = DeudaUiState.Idle
    }
}
