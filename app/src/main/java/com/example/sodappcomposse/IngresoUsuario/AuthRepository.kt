package com.example.sodappcomposse.IngresoUsuario

import com.example.sodappcomposse.API.ApiServices
import com.example.sodappcomposse.API.UsuarioResponse
import com.example.sodappcomposse.R
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

interface AuthRepository {
    suspend fun login(usuarioRequest: UsuarioRequest): AuthResult
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
}
