package com.example.sodappcomposse.Cliente

import androidx.annotation.StringRes
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sodappcomposse.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AgendaUiState {
    object Idle : AgendaUiState
    object Loading : AgendaUiState
    data class Success(@StringRes val messageRes: Int, val args: Array<Any> = emptyArray()) : AgendaUiState {
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
    data class Error(@StringRes val messageRes: Int, val args: Array<Any> = emptyArray()) : AgendaUiState {
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
class AgendaViewModel @Inject constructor(
    private val agendaRepository: AgendaRepository
) : ViewModel() {

    private val _uiState = mutableStateOf<AgendaUiState>(AgendaUiState.Idle)
    val uiState: State<AgendaUiState> = _uiState

    val diasEntrega: MutableState<TodosLosDias> = mutableStateOf(TodosLosDias())
    val diasEntregaById: MutableState<List<String>> = mutableStateOf(emptyList())

    fun getDiasEntrega() {
        _uiState.value = AgendaUiState.Loading
        viewModelScope.launch {
            when (val result = agendaRepository.getDiasEntrega()) {
                is AgendaResult.Success -> {
                    diasEntrega.value = result.data
                    _uiState.value = AgendaUiState.Success(R.string.datos_cargados_success)
                }
                is AgendaResult.Error -> {
                    _uiState.value = AgendaUiState.Error(result.messageRes, result.args)
                }
            }
        }
    }

    fun getDiasEntregaById(idCl: Int) {
        _uiState.value = AgendaUiState.Loading
        viewModelScope.launch {
            when (val result = agendaRepository.getDiasEntregaById(idCl)) {
                is AgendaResult.Success -> {
                    diasEntregaById.value = result.data.diasEntrega
                    _uiState.value = AgendaUiState.Success(R.string.datos_cargados_success)
                }
                is AgendaResult.Error -> {
                    _uiState.value = AgendaUiState.Error(result.messageRes, result.args)
                }
            }
        }
    }

    fun updateDiasEntrega(clienteId: Int?, diasSeleccionados: List<String>) {
        if (clienteId == null) return
        
        _uiState.value = AgendaUiState.Loading
        val diasEntregaRequest = DiasEntrega(clienteId, diasSeleccionados)

        viewModelScope.launch {
            when (val result = agendaRepository.updateDiasEntrega(diasEntregaRequest)) {
                is AgendaResult.Success -> {
                    _uiState.value = AgendaUiState.Success(R.string.venta_procesada_success) // Reusing a success string or should use a specific one
                }
                is AgendaResult.Error -> {
                    _uiState.value = AgendaUiState.Error(result.messageRes, result.args)
                }
            }
        }
    }
}
