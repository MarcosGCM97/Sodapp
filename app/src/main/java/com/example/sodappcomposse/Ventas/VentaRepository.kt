package com.example.sodappcomposse.Ventas

import com.example.sodappcomposse.API.ApiServices
import com.example.sodappcomposse.API.PostResponse
import com.example.sodappcomposse.R
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

sealed class VentaResult<out T> {
    data class Success<T>(val data: T) : VentaResult<T>()
    data class Error(val messageRes: Int, val args: Array<Any> = emptyArray()) : VentaResult<Nothing>() {
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

interface VentaRepository {
    suspend fun getVentas(usuarioId: String): VentaResult<VentaApiResponse>
    suspend fun getVentasByClienteId(id: Int, usuarioId: String): VentaResult<VentaApiResponseById>
    suspend fun postVenta(ventaRequest: VentaRequest): VentaResult<PostResponse>
    suspend fun deleteVenta(id: Int?): VentaResult<PostResponse>
}

class VentaRepositoryImpl @Inject constructor(
    private val apiServices: ApiServices
) : VentaRepository {
    override suspend fun getVentas(usuarioId: String): VentaResult<VentaApiResponse> = handleApiCall {
        apiServices.getVentas(usuarioId)
    }

    override suspend fun getVentasByClienteId(id: Int, usuarioId: String): VentaResult<VentaApiResponseById> = handleApiCall {
        apiServices.getVentasByClienteId(id, usuarioId)
    }

    override suspend fun postVenta(ventaRequest: VentaRequest): VentaResult<PostResponse> = handleApiCall {
        apiServices.postVenta(ventaRequest)
    }

    override suspend fun deleteVenta(id: Int?): VentaResult<PostResponse> = handleApiCall {
        apiServices.deleteVenta(id)
    }

    private suspend fun <T> handleApiCall(call: suspend () -> retrofit2.Response<T>): VentaResult<T> {
        return try {
            val response = call()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    VentaResult.Success(body)
                } else {
                    VentaResult.Error(R.string.cuerpo_nulo_error)
                }
            } else {
                VentaResult.Error(R.string.error_servidor_format, arrayOf(response.code()))
            }
        } catch (e: IOException) {
            VentaResult.Error(R.string.error_red_verificar)
        } catch (e: HttpException) {
            VentaResult.Error(R.string.error_http_format, arrayOf(e.code()))
        } catch (e: Exception) {
            VentaResult.Error(R.string.error_inesperado_format, arrayOf(e.message?.take(100) ?: ""))
        }
    }
}
