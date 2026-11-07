package com.example.sodappcomposse.IngresoUsuario

import com.example.sodappcomposse.API.ApiServices
import com.example.sodappcomposse.API.UsuarioResponse
import com.example.sodappcomposse.R
import com.example.sodappcomposse.auth.SessionPermissions
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

sealed class AuthResult {
    data class Success(val data: UsuarioResponse) : AuthResult()
    data class Error(val messageRes: Int, val args: Array<Any> = emptyArray()) : AuthResult() {
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

sealed class SesionResult {
    data class Success(val permissions: SessionPermissions) : SesionResult()
    /** The server rejected the token: the session is over. */
    object Unauthorized : SesionResult()
    data class Error(val messageRes: Int, val args: Array<Any> = emptyArray()) : SesionResult() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Error) return false
            return messageRes == other.messageRes && args.contentEquals(other.args)
        }
        override fun hashCode(): Int = 31 * messageRes + args.contentHashCode()
    }
}

interface AuthRepository {
    suspend fun login(usuarioRequest: UsuarioRequest): AuthResult

    /** Current permissions of the saved session (sesion.php). */
    suspend fun getSesion(): SesionResult
}

class AuthRepositoryImpl @Inject constructor(
    private val apiServices: ApiServices
) : AuthRepository {
    override suspend fun login(usuarioRequest: UsuarioRequest): AuthResult {
        return try {
            val response = apiServices.login(usuarioRequest)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    AuthResult.Success(body)
                } else {
                    AuthResult.Error(R.string.cuerpo_nulo_error)
                }
            } else {
                AuthResult.Error(R.string.error_servidor_format, arrayOf(response.code()))
            }
        } catch (e: IOException) {
            AuthResult.Error(R.string.error_red_verificar)
        } catch (e: HttpException) {
            AuthResult.Error(R.string.error_http_format, arrayOf(e.code()))
        } catch (e: Exception) {
            AuthResult.Error(R.string.error_inesperado_format, arrayOf(e.message?.take(100) ?: ""))
        }
    }

    override suspend fun getSesion(): SesionResult {
        return try {
            val response = apiServices.getSesion()
            val body = response.body()
            when {
                response.code() == HTTP_UNAUTHORIZED -> SesionResult.Unauthorized
                !response.isSuccessful -> SesionResult.Error(R.string.error_servidor_format, arrayOf(response.code()))
                body == null || !body.success -> SesionResult.Error(R.string.cuerpo_nulo_error)
                else -> SesionResult.Success(body.sessionPermissions())
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            SesionResult.Error(R.string.error_red_verificar)
        } catch (e: Exception) {
            SesionResult.Error(R.string.error_inesperado_format, arrayOf(e.message?.take(100) ?: ""))
        }
    }

    private companion object {
        const val HTTP_UNAUTHORIZED = 401
    }
}
