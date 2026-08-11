package com.example.sodappcomposse.Cliente

import androidx.annotation.StringRes
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sodappcomposse.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface DeudaUiState {
    object Idle : DeudaUiState
    object Loading : DeudaUiState
    data class Success(@StringRes val messageRes: Int, val args: Array<Any> = emptyArray()) : DeudaUiState {
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
    data class Error(@StringRes val messageRes: Int, val args: Array<Any> = emptyArray()) : DeudaUiState {
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

@HiltViewModel
class DeudaViewModel @Inject constructor(
    private val clienteRepository: ClienteRepository
) : ViewModel() {

    private val _uiState = mutableStateOf<DeudaUiState>(DeudaUiState.Idle)
    val uiState: State<DeudaUiState> = _uiState

    fun pagarDeudaCliente(idCl: Int, deuda: Double) {
        _uiState.value = DeudaUiState.Loading
        viewModelScope.launch {
            when (val result = clienteRepository.updateDeudaCliente(idCl, deuda)) {
                is ClienteResult.Success -> {
                    _uiState.value = DeudaUiState.Success(R.string.venta_procesada_success)
                }
                is ClienteResult.Error -> {
                    _uiState.value = DeudaUiState.Error(result.messageRes, result.args)
                }
            }
        }
    }

    fun resetState() {
        _uiState.value = DeudaUiState.Idle
    }
}
