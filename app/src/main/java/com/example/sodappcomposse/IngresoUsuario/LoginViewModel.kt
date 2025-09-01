package com.example.sodappcomposse.IngresoUsuario

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import com.example.sodappcomposse.API.ApiServices
import com.example.sodappcomposse.API.RetrofitInstance
import androidx.lifecycle.viewModelScope
import com.example.sodappcomposse.API.UsuarioResponse
import kotlinx.coroutines.launch
import retrofit2.HttpException

sealed interface LoginUiState {
    object Idle : LoginUiState
    object Loading : LoginUiState
    data class Success(val message: String) : LoginUiState
    data class Error(val message: String) : LoginUiState
}

class LoginViewModel(
    private val apiServices: ApiServices = RetrofitInstance.api
) : ViewModel() {
    private val TAG = "UsuarioViewModel"

    var loginUiState: LoginUiState by mutableStateOf(LoginUiState.Idle)
        private set

    private val _usuario = mutableStateListOf<UsuarioResponse>()
    val usuario: SnapshotStateList<UsuarioResponse> = _usuario

    internal fun login(nombre: String, contrasena: String) {
        if (nombre.isBlank() || contrasena.isBlank()) {
            loginUiState = LoginUiState.Error("Todos los campos son requeridos.")
            return
        }

        loginUiState = LoginUiState.Loading
        viewModelScope.launch {
            try {
                val userLogin = UsuarioRequest(nombreUs = nombre, contrasenaUs = contrasena)
                val response = apiServices.login(userLogin)

                if (response.isSuccessful && response.body() != null) {
                    val usuarioApi = response.body()!!

                    if(usuarioApi.success){
                        loginUiState = LoginUiState.Success(usuarioApi.message)
                    }else{
                        loginUiState = LoginUiState.Error(usuarioApi.message)
                    }
                } else {
                    // Manejar error de la API
                    loginUiState = LoginUiState.Error("Error en la respuesta: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                loginUiState = LoginUiState.Error("Error al iniciar sesión: ${e.message}")
            } catch (e: HttpException) {
                loginUiState = LoginUiState.Error("Error HTTP al iniciar sesión: ${e.code()} - ${e.message()}")
            }
        }
    }

    // Opcional: función para resetear el estado
    fun resetLoginState() {
        loginUiState = LoginUiState.Idle
        _usuario.clear()
    }
}