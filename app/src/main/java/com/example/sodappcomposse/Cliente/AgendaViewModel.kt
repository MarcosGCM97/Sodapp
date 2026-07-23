package com.example.sodappcomposse.Cliente

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

sealed interface AgendaUiState {
    object Idle : AgendaUiState
    object Loading : AgendaUiState
    data class Success(val message: String) : AgendaUiState
    data class Error(val message: String) : AgendaUiState
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
            try {
                val response = agendaRepository.getDiasEntrega()
                if (response.isSuccessful) {
                    diasEntrega.value = response.body() ?: TodosLosDias()
                    _uiState.value = AgendaUiState.Success("Días de entrega cargados")
                } else {
                    _uiState.value = AgendaUiState.Error("Error: ${response.code()}")
                }
            } catch (e: IOException) {
                _uiState.value = AgendaUiState.Error("Error de red: ${e.message}")
            } catch (e: HttpException) {
                _uiState.value = AgendaUiState.Error("Error HTTP: ${e.code()}")
            } catch (e: Exception) {
                _uiState.value = AgendaUiState.Error("Error inesperado: ${e.message}")
            }
        }
    }

    fun getDiasEntregaById(idCl: Int) {
        _uiState.value = AgendaUiState.Loading
        viewModelScope.launch {
            try {
                val response = agendaRepository.getDiasEntregaById(idCl)
                if (response.isSuccessful) {
                    diasEntregaById.value = response.body()?.diasEntrega ?: emptyList()
                    _uiState.value = AgendaUiState.Success("Días del cliente cargados")
                } else {
                    _uiState.value = AgendaUiState.Error("Error: ${response.code()}")
                }
            } catch (e: IOException) {
                _uiState.value = AgendaUiState.Error("Error de red: ${e.message}")
            } catch (e: HttpException) {
                _uiState.value = AgendaUiState.Error("Error HTTP: ${e.code()}")
            } catch (e: Exception) {
                _uiState.value = AgendaUiState.Error("Error inesperado: ${e.message}")
            }
        }
    }

    fun updateDiasEntrega(clienteId: Int?, diasSeleccionados: List<String>) {
        if (clienteId == null) return
        
        _uiState.value = AgendaUiState.Loading
        val diasEntregaRequest = DiasEntrega(clienteId, diasSeleccionados)

        viewModelScope.launch {
            try {
                val response = agendaRepository.updateDiasEntrega(diasEntregaRequest)
                if (response.isSuccessful) {
                    _uiState.value = AgendaUiState.Success("Agenda actualizada exitosamente")
                } else {
                    _uiState.value = AgendaUiState.Error("Error al actualizar: ${response.code()}")
                }
            } catch (e: IOException) {
                _uiState.value = AgendaUiState.Error("Error de red: ${e.message}")
            } catch (e: HttpException) {
                _uiState.value = AgendaUiState.Error("Error HTTP: ${e.code()}")
            } catch (e: Exception) {
                _uiState.value = AgendaUiState.Error("Error inesperado: ${e.message}")
            }
        }
    }
}
