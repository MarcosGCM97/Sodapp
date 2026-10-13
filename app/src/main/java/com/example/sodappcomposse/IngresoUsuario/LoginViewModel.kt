package com.example.sodappcomposse.IngresoUsuario

import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sodappcomposse.API.UsuarioResponse
import com.example.sodappcomposse.Cliente.ClienteRepository
import com.example.sodappcomposse.Producto.ProductoRepository
import com.example.sodappcomposse.R
import com.example.sodappcomposse.UserPreferencesRepository
import com.example.sodappcomposse.auth.AuthSession
import kotlinx.coroutines.launch
import retrofit2.HttpException
import dagger.hilt.android.lifecycle.HiltViewModel // IMPORTANTE
import javax.inject.Inject // IMPORTANTE

sealed interface LoginUiState {
    object Idle : LoginUiState
    object Loading : LoginUiState
    data class Success(@StringRes val messageRes: Int, val args: Array<Any> = emptyArray()) : LoginUiState {
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
    data class Error(@StringRes val messageRes: Int, val args: Array<Any> = emptyArray()) : LoginUiState {
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
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val authSession: AuthSession,
    private val clienteRepository: ClienteRepository,
    private val productoRepository: ProductoRepository
) : ViewModel() {
    private val TAG = "UsuarioViewModel"

    val isLoggedIn = userPreferencesRepository.isLoggedIn

    // True when the server rejected the session token (401); observed by the navigation layer
    val sessionExpired = authSession.sessionExpired

    var loginUiState: LoginUiState by mutableStateOf(LoginUiState.Idle)
        private set

    private val _usuario = mutableStateListOf<UsuarioResponse>()
    val usuario: SnapshotStateList<UsuarioResponse> = _usuario

    internal fun login(nombre: String, contrasena: String) {
        if (nombre.isBlank() || contrasena.isBlank()) {
            loginUiState = LoginUiState.Error(R.string.campos_requeridos_error)
            return
        }

        loginUiState = LoginUiState.Loading
        viewModelScope.launch {
            val userLogin = UsuarioRequest(nombreUs = nombre, contrasenaUs = contrasena)
            when (val result = authRepository.login(userLogin)) {
                is AuthResult.Success -> {
                    val usuarioApi = result.data
                    val usuario = usuarioApi.usuario
                    val token = usuarioApi.token
                    loginUiState = when {
                        !usuarioApi.success ->
                            LoginUiState.Error(R.string.error_simple_format, arrayOf(usuarioApi.message))
                        usuario == null || token.isNullOrBlank() ->
                            LoginUiState.Error(R.string.error_simple_format, arrayOf("respuesta de login sin sesión"))
                        else -> {
                            // A different user may log in: never reuse data cached for the previous one
                            clearCachedUserData()
                            authSession.startSession(usuario.idUs.toString(), usuario.nombreUs, token)
                            LoginUiState.Success(R.string.login_success_format, arrayOf(usuario.nombreUs))
                        }
                    }
                }
                is AuthResult.Error -> {
                    loginUiState = LoginUiState.Error(result.messageRes, result.args)
                }
            }
        }
    }

    // Opcional: función para resetear el estado
    fun resetLoginState() {
        loginUiState = LoginUiState.Idle
        _usuario.clear()
    }

    fun logout() {
        clearCachedUserData()
        resetLoginState()
        viewModelScope.launch {
            authSession.endSession()
        }
    }

    // Called when the server answered 401: same cleanup as an explicit logout
    fun onSessionExpired() = logout()

    private fun clearCachedUserData() {
        clienteRepository.clearCache()
        productoRepository.clearCache()
    }
}
