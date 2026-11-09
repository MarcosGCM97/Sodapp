package com.example.sodappcomposse.Administracion

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

sealed interface AdministracionUiState {
    object Idle : AdministracionUiState
    object Loading : AdministracionUiState
    data class Success(@StringRes val messageRes: Int) : AdministracionUiState
    data class Error(@StringRes val messageRes: Int, val args: Array<Any> = emptyArray()) : AdministracionUiState {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Error) return false
            return messageRes == other.messageRes && args.contentEquals(other.args)
        }
        override fun hashCode(): Int = 31 * messageRes + args.contentHashCode()
    }
}

@HiltViewModel
class AdministracionViewModel @Inject constructor(
    private val repository: AdministracionRepository
) : ViewModel() {

    private val _usuarios = mutableStateOf<List<UsuarioAdmin>>(emptyList())
    val usuarios: State<List<UsuarioAdmin>> = _usuarios

    private val _roles = mutableStateOf<List<RolAdmin>>(emptyList())
    val roles: State<List<RolAdmin>> = _roles

    private val _permisosDisponibles = mutableStateOf<List<PermisoDisponible>>(emptyList())
    val permisosDisponibles: State<List<PermisoDisponible>> = _permisosDisponibles

    private val _uiState: MutableState<AdministracionUiState> = mutableStateOf(AdministracionUiState.Idle)
    val uiState: State<AdministracionUiState> = _uiState

    /** Loads users, roles and the permission catalog. */
    fun cargar() {
        viewModelScope.launch {
            _uiState.value = AdministracionUiState.Loading
            _uiState.value = recargar() ?: AdministracionUiState.Idle
        }
    }

    fun crearUsuario(nombre: String, contrasena: String, rolId: Int) =
        ejecutar(R.string.usuario_creado) { repository.crearUsuario(nombre, contrasena, rolId) }

    fun cambiarRol(usuarioId: Int, rolId: Int) =
        ejecutar(R.string.usuario_actualizado) { repository.cambiarRol(usuarioId, rolId) }

    fun cambiarContrasena(usuarioId: Int, contrasena: String) =
        ejecutar(R.string.usuario_actualizado) { repository.cambiarContrasena(usuarioId, contrasena) }

    fun cambiarActivo(usuarioId: Int, activo: Boolean) =
        ejecutar(R.string.usuario_actualizado) { repository.cambiarActivo(usuarioId, activo) }

    fun crearRol(nombre: String, permisos: List<String>) =
        ejecutar(R.string.rol_guardado) { repository.crearRol(nombre, permisos) }

    fun actualizarRol(rolId: Int, nombre: String, permisos: List<String>) =
        ejecutar(R.string.rol_guardado) { repository.actualizarRol(rolId, nombre, permisos) }

    fun eliminarRol(rolId: Int) =
        ejecutar(R.string.rol_eliminado) { repository.eliminarRol(rolId) }

    fun resetState() {
        _uiState.value = AdministracionUiState.Idle
    }

    // Runs one change; on success reloads both lists (a role change moves user counts too)
    private fun ejecutar(@StringRes exito: Int, accion: suspend () -> AdminResult<Unit>) {
        viewModelScope.launch {
            _uiState.value = AdministracionUiState.Loading
            _uiState.value = when (val result = accion()) {
                is AdminResult.Success -> recargar() ?: AdministracionUiState.Success(exito)
                is AdminResult.Error -> AdministracionUiState.Error(result.messageRes, result.args)
            }
        }
    }

    /** Reloads both lists; returns the error state of the first failure, or null. */
    private suspend fun recargar(): AdministracionUiState.Error? {
        when (val result = repository.getUsuarios()) {
            is AdminResult.Success -> _usuarios.value = result.data
            is AdminResult.Error -> return AdministracionUiState.Error(result.messageRes, result.args)
        }
        when (val result = repository.getRoles()) {
            is AdminResult.Success -> {
                _roles.value = result.data.roles
                _permisosDisponibles.value = result.data.permisosDisponibles
            }
            is AdminResult.Error -> return AdministracionUiState.Error(result.messageRes, result.args)
        }
        return null
    }
}
