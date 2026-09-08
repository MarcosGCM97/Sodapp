package com.example.sodappcomposse.Cliente

import com.example.sodappcomposse.API.ApiServices
import com.example.sodappcomposse.API.PostResponse
import com.example.sodappcomposse.R
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

sealed class ClienteResult<out T> {
    data class Success<T>(val data: T) : ClienteResult<T>()
    data class Error(val messageRes: Int, val args: Array<Any> = emptyArray()) : ClienteResult<Nothing>() {
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

interface ClienteRepository {
    suspend fun getClientes(forceRefresh: Boolean = false): ClienteResult<ClienteResponse>
    suspend fun getClienteById(id: Int): ClienteResult<ClienteResponseById>
    suspend fun postCliente(clienteRequest: ClienteRequest): ClienteResult<PostResponse>
    suspend fun updateCliente(cliente: Cliente): ClienteResult<PostResponse>
    suspend fun eliminarCliente(id: Int): ClienteResult<PostResponse>
    suspend fun updateDeudaCliente(id: Int, deuda: Double?): ClienteResult<PostResponse>
    fun clearCache()
}

class ClienteRepositoryImpl @Inject constructor(
    private val apiServices: ApiServices
) : ClienteRepository {
    private var cachedClientes: ClienteResult<ClienteResponse>? = null

    override suspend fun getClientes(forceRefresh: Boolean): ClienteResult<ClienteResponse> {
        if (forceRefresh || cachedClientes == null || cachedClientes is ClienteResult.Error) {
            cachedClientes = try {
                val response = apiServices.getClientes()
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        ClienteResult.Success(body)
                    } else {
                        ClienteResult.Error(R.string.cuerpo_nulo_error)
                    }
                } else {
                    ClienteResult.Error(R.string.error_servidor_format, arrayOf(response.code()))
                }
            } catch (e: IOException) {
                ClienteResult.Error(R.string.error_red_verificar)
            } catch (e: Exception) {
                ClienteResult.Error(R.string.error_inesperado_format, arrayOf(e.message?.take(100) ?: ""))
            }
        }
        return cachedClientes!!
    }

    override suspend fun getClienteById(id: Int): ClienteResult<ClienteResponseById> = handleApiCall {
        apiServices.getClienteById(id)
    }

    override suspend fun postCliente(clienteRequest: ClienteRequest): ClienteResult<PostResponse> {
        val result = handleApiCall { apiServices.postCliente(clienteRequest) }
        if (result is ClienteResult.Success) clearCache()
        return result
    }

    override suspend fun updateCliente(cliente: Cliente): ClienteResult<PostResponse> {
        val result = handleApiCall { apiServices.updateCliente(cliente) }
        if (result is ClienteResult.Success) clearCache()
        return result
    }

    override suspend fun eliminarCliente(id: Int): ClienteResult<PostResponse> {
        val result = handleApiCall { apiServices.eliminarCliente(id) }
        if (result is ClienteResult.Success) clearCache()
        return result
    }

    override suspend fun updateDeudaCliente(id: Int, deuda: Double?): ClienteResult<PostResponse> {
        val result = handleApiCall { apiServices.updateDeudaCliente(id, deuda) }
        if (result is ClienteResult.Success) clearCache()
        return result
    }

    override fun clearCache() {
        cachedClientes = null
    }

    private suspend fun <T> handleApiCall(call: suspend () -> retrofit2.Response<T>): ClienteResult<T> {
        return try {
            val response = call()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    ClienteResult.Success(body)
                } else {
                    ClienteResult.Error(R.string.cuerpo_nulo_error)
                }
            } else {
                ClienteResult.Error(R.string.error_servidor_format, arrayOf(response.code()))
            }
        } catch (e: IOException) {
            ClienteResult.Error(R.string.error_red_verificar)
        } catch (e: HttpException) {
            ClienteResult.Error(R.string.error_http_format, arrayOf(e.code()))
        } catch (e: Exception) {
            ClienteResult.Error(R.string.error_inesperado_format, arrayOf(e.message?.take(100) ?: ""))
        }
    }
}
