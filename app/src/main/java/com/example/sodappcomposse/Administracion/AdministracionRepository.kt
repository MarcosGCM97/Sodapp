package com.example.sodappcomposse.Administracion

import androidx.annotation.StringRes
import com.example.sodappcomposse.API.ApiServices
import com.example.sodappcomposse.API.HTTP_FORBIDDEN
import com.example.sodappcomposse.R
import com.google.gson.JsonParser
import kotlinx.coroutines.CancellationException
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

sealed class AdminResult<out T> {
    data class Success<T>(val data: T) : AdminResult<T>()
    data class Error(@StringRes val messageRes: Int, val args: Array<Any> = emptyArray()) : AdminResult<Nothing>() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Error) return false
            return messageRes == other.messageRes && args.contentEquals(other.args)
        }
        override fun hashCode(): Int = 31 * messageRes + args.contentHashCode()
    }
}

/** Owner-only administration of the company's users and roles (usuarios.php, roles.php). */
interface AdministracionRepository {
    suspend fun getUsuarios(): AdminResult<List<UsuarioAdmin>>
    suspend fun crearUsuario(nombre: String, contrasena: String, rolId: Int): AdminResult<Unit>
    suspend fun cambiarRol(usuarioId: Int, rolId: Int): AdminResult<Unit>
    suspend fun cambiarContrasena(usuarioId: Int, contrasena: String): AdminResult<Unit>
    suspend fun cambiarActivo(usuarioId: Int, activo: Boolean): AdminResult<Unit>
    suspend fun getRoles(): AdminResult<RolesAdmin>
    suspend fun crearRol(nombre: String, permisos: List<String>): AdminResult<Unit>
    suspend fun actualizarRol(rolId: Int, nombre: String, permisos: List<String>): AdminResult<Unit>
    suspend fun eliminarRol(rolId: Int): AdminResult<Unit>
}

class AdministracionRepositoryImpl @Inject constructor(
    private val apiServices: ApiServices
) : AdministracionRepository {

    override suspend fun getUsuarios(): AdminResult<List<UsuarioAdmin>> =
        llamar({ apiServices.getUsuarios() }) { it.usuarios.orEmpty() }

    override suspend fun crearUsuario(nombre: String, contrasena: String, rolId: Int): AdminResult<Unit> =
        accion { apiServices.crearUsuario(CrearUsuarioRequest(nombre.trim(), contrasena, rolId)) }

    override suspend fun cambiarRol(usuarioId: Int, rolId: Int): AdminResult<Unit> =
        accion { apiServices.actualizarUsuario(ActualizarUsuarioRequest(id = usuarioId, rol = rolId)) }

    override suspend fun cambiarContrasena(usuarioId: Int, contrasena: String): AdminResult<Unit> =
        accion { apiServices.actualizarUsuario(ActualizarUsuarioRequest(id = usuarioId, contrasena = contrasena)) }

    override suspend fun cambiarActivo(usuarioId: Int, activo: Boolean): AdminResult<Unit> =
        accion { apiServices.actualizarUsuario(ActualizarUsuarioRequest(id = usuarioId, activo = activo)) }

    override suspend fun getRoles(): AdminResult<RolesAdmin> =
        llamar({ apiServices.getRoles() }) {
            RolesAdmin(roles = it.roles.orEmpty(), permisosDisponibles = it.permisosDisponibles.orEmpty())
        }

    override suspend fun crearRol(nombre: String, permisos: List<String>): AdminResult<Unit> =
        accion { apiServices.crearRol(RolRequest(nombre = nombre.trim(), permisos = permisos)) }

    override suspend fun actualizarRol(rolId: Int, nombre: String, permisos: List<String>): AdminResult<Unit> =
        accion { apiServices.actualizarRol(RolRequest(id = rolId, nombre = nombre.trim(), permisos = permisos)) }

    override suspend fun eliminarRol(rolId: Int): AdminResult<Unit> =
        accion { apiServices.eliminarRol(rolId) }

    private suspend fun accion(call: suspend () -> Response<AdminResponse>): AdminResult<Unit> =
        llamar(call) { }

    private suspend fun <B, T> llamar(call: suspend () -> Response<B>, map: (B) -> T): AdminResult<T> {
        val response = try {
            call()
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            return AdminResult.Error(R.string.error_red_verificar)
        } catch (e: Exception) {
            return AdminResult.Error(R.string.error_inesperado_format, arrayOf(e.message?.take(100) ?: ""))
        }
        if (response.isSuccessful) {
            val body = response.body() ?: return AdminResult.Error(R.string.cuerpo_nulo_error)
            return AdminResult.Success(map(body))
        }
        if (response.code() == HTTP_FORBIDDEN) return AdminResult.Error(R.string.error_sin_permiso)
        return serverMessageError(response)
            ?: AdminResult.Error(R.string.error_servidor_format, arrayOf(response.code()))
    }

    /** Surfaces the backend's `{"success":false,"error":"<message>"}` (400, 404, 409). */
    private fun serverMessageError(response: Response<*>): AdminResult.Error? {
        val error = try {
            JsonParser.parseString(response.errorBody()?.string().orEmpty()).asJsonObject.get("error")?.asString
        } catch (e: Exception) {
            null
        }
        if (error.isNullOrBlank()) return null
        return AdminResult.Error(R.string.error_format, arrayOf(error))
    }
}
