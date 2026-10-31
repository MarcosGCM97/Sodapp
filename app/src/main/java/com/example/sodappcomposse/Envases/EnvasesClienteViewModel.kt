package com.example.sodappcomposse.Envases

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sodappcomposse.Producto.ProductoResult
import com.example.sodappcomposse.Producto.ProductoUiState
import com.example.sodappcomposse.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Container balance of one client, per returnable product. */
@HiltViewModel
class EnvasesClienteViewModel @Inject constructor(
    private val envaseRepository: EnvaseRepository
) : ViewModel() {

    private val _envases = mutableStateOf<List<EnvaseCliente>>(emptyList())
    val envases: State<List<EnvaseCliente>> = _envases

    private val _uiState = mutableStateOf<ProductoUiState>(ProductoUiState.Idle)
    val uiState: State<ProductoUiState> = _uiState

    /** Always refetches: sales registered or deleted change the balance on the backend. */
    fun cargarEnvases(clienteId: Int) {
        viewModelScope.launch {
            when (val result = envaseRepository.getEnvasesCliente(clienteId)) {
                is ProductoResult.Success -> _envases.value = result.data.envases
                is ProductoResult.Error -> _uiState.value = ProductoUiState.Error(result.messageRes, result.args)
            }
        }
    }

    /** Sets the absolute balance (manual initial load or correction) and reloads the list. */
    fun guardarSaldo(clienteId: Int, productoId: Int, cantidad: Int) {
        if (cantidad < 0) {
            _uiState.value = ProductoUiState.Error(R.string.error_cantidad_negativa)
            return
        }
        _uiState.value = ProductoUiState.Loading
        viewModelScope.launch {
            when (val result = envaseRepository.setSaldoEnvases(clienteId, productoId, cantidad)) {
                is ProductoResult.Success -> {
                    _uiState.value = ProductoUiState.Success(R.string.saldo_envases_guardado)
                    cargarEnvases(clienteId)
                }
                is ProductoResult.Error -> _uiState.value = ProductoUiState.Error(result.messageRes, result.args)
            }
        }
    }

    fun resetState() {
        _uiState.value = ProductoUiState.Idle
    }
}
