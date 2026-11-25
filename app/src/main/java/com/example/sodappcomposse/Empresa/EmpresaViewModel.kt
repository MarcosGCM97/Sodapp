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

    // Own password change dialog: separate from the company form so each shows its own result
    private val _contrasenaState: MutableState<EmpresaUiState> = mutableStateOf(EmpresaUiState.Idle)
    val contrasenaState: State<EmpresaUiState> = _contrasenaState

    /** Checks the dialog fields locally (same minimum as the backend), then changes the password. */
    fun cambiarContrasena(actual: String, nueva: String, repetida: String) {
        val error = when {
            actual.isEmpty() -> EmpresaUiState.Error(R.string.contrasena_actual_requerida)
            // Characters like the backend's mb_strlen, not UTF-16 units
            nueva.codePointCount(0, nueva.length) < CONTRASENA_MIN -> EmpresaUiState.Error(R.string.contrasena_nueva_corta_format, arrayOf(CONTRASENA_MIN))
            nueva != repetida -> EmpresaUiState.Error(R.string.contrasena_no_coinciden)
            else -> null
        }
        if (error != null) {
            _contrasenaState.value = error
            return
        }
        viewModelScope.launch {
            _contrasenaState.value = EmpresaUiState.Loading
            _contrasenaState.value = when (val result = repository.cambiarContrasena(actual, nueva)) {
                is EmpresaResult.Success -> EmpresaUiState.Success(R.string.contrasena_cambiada)
                is EmpresaResult.Error -> EmpresaUiState.Error(result.messageRes, result.args)
            }
        }
    }

    fun resetContrasenaState() {
        _contrasenaState.value = EmpresaUiState.Idle
    }

    private companion object {
        // Same as USUARIO_CONTRASENA_MIN in the backend (lib.php)
        const val CONTRASENA_MIN = 6
    }
}
