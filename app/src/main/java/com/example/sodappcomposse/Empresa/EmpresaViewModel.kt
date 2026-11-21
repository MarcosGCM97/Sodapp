package com.example.sodappcomposse.Empresa

import androidx.annotation.StringRes
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sodappcomposse.R
import com.example.sodappcomposse.auth.AuthSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface EmpresaUiState {
    object Idle : EmpresaUiState
    object Loading : EmpresaUiState
    data class Success(@StringRes val messageRes: Int) : EmpresaUiState
    data class Error(@StringRes val messageRes: Int, val args: Array<Any> = emptyArray()) : EmpresaUiState {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Error) return false
            return messageRes == other.messageRes && args.contentEquals(other.args)
        }
        override fun hashCode(): Int = 31 * messageRes + args.contentHashCode()
    }
}

/** Owner-only form of the company data; a saved name signs the WhatsApp messages at once. */
@HiltViewModel
class EmpresaViewModel @Inject constructor(
    private val repository: EmpresaRepository,
    private val authSession: AuthSession
) : ViewModel() {

    private val _empresa = mutableStateOf<Empresa?>(null)
    val empresa: State<Empresa?> = _empresa

    private val _uiState: MutableState<EmpresaUiState> = mutableStateOf(EmpresaUiState.Idle)
    val uiState: State<EmpresaUiState> = _uiState

    fun cargar() {
        viewModelScope.launch {
            _uiState.value = EmpresaUiState.Loading
            _uiState.value = when (val result = repository.getEmpresa()) {
                is EmpresaResult.Success -> {
                    _empresa.value = result.data
                    EmpresaUiState.Idle
                }
                is EmpresaResult.Error -> EmpresaUiState.Error(result.messageRes, result.args)
            }
        }
    }

    fun guardar(nombre: String, telefono: String, direccion: String) {
        if (nombre.isBlank()) {
            _uiState.value = EmpresaUiState.Error(R.string.empresa_nombre_requerido)
            return
        }
        viewModelScope.launch {
            _uiState.value = EmpresaUiState.Loading
            _uiState.value = when (val result = repository.guardarEmpresa(Empresa(nombre, telefono, direccion))) {
                is EmpresaResult.Success -> {
                    _empresa.value = result.data
                    authSession.updateCompanyName(result.data?.nombre, authSession.currentToken())
                    EmpresaUiState.Success(R.string.empresa_guardada)
                }
                is EmpresaResult.Error -> EmpresaUiState.Error(result.messageRes, result.args)
            }
        }
    }

    fun resetState() {
        _uiState.value = EmpresaUiState.Idle
    }
}
