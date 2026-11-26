package com.example.sodappcomposse.Mensajes

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

sealed interface MensajesUiState {
    object Idle : MensajesUiState
    object Loading : MensajesUiState
    data class Success(@StringRes val messageRes: Int) : MensajesUiState
    data class Error(@StringRes val messageRes: Int, val args: Array<Any> = emptyArray()) : MensajesUiState {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Error) return false
            return messageRes == other.messageRes && args.contentEquals(other.args)
        }
        override fun hashCode(): Int = 31 * messageRes + args.contentHashCode()
    }
}

/**
 * WhatsApp templates of the company. The screens that send messages read [plantillas]
 * (a type without template uses its default message); the owner's editor also saves and
 * restores them.
 */
@HiltViewModel
class MensajesViewModel @Inject constructor(
    private val repository: PlantillasRepository
) : ViewModel() {

    private val _plantillas = mutableStateOf(repository.plantillasEnCache())
    val plantillas: State<Map<TipoPlantilla, String>> = _plantillas

    private val _uiState: MutableState<MensajesUiState> = mutableStateOf(MensajesUiState.Idle)
    val uiState: State<MensajesUiState> = _uiState

    fun cargar() {
        viewModelScope.launch {
            _uiState.value = MensajesUiState.Loading
            _uiState.value = when (val result = repository.getPlantillas()) {
                is PlantillasResult.Success -> {
                    _plantillas.value = result.data
                    MensajesUiState.Idle
                }
                is PlantillasResult.Error -> MensajesUiState.Error(result.messageRes, result.args)
            }
        }
    }

    fun guardar(tipo: TipoPlantilla, texto: String) {
        if (texto.isBlank()) {
            _uiState.value = MensajesUiState.Error(R.string.mensaje_plantilla_vacia)
            return
        }
        ejecutar(R.string.mensaje_plantilla_guardada) { repository.guardarPlantilla(tipo, texto.trim()) }
    }

    fun restaurar(tipo: TipoPlantilla) {
        ejecutar(R.string.mensaje_plantilla_restaurada) { repository.restaurarPlantilla(tipo) }
    }

    fun resetState() {
        _uiState.value = MensajesUiState.Idle
    }

    private fun ejecutar(@StringRes exito: Int, call: suspend () -> PlantillasResult<Map<TipoPlantilla, String>>) {
        viewModelScope.launch {
            _uiState.value = MensajesUiState.Loading
            _uiState.value = when (val result = call()) {
                is PlantillasResult.Success -> {
                    _plantillas.value = result.data
                    MensajesUiState.Success(exito)
                }
                is PlantillasResult.Error -> MensajesUiState.Error(result.messageRes, result.args)
            }
        }
    }
}
