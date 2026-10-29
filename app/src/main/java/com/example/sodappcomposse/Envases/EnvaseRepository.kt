package com.example.sodappcomposse.Envases

import com.example.sodappcomposse.API.ApiServices
import com.example.sodappcomposse.Producto.ProductoResult
import com.example.sodappcomposse.R
import com.google.gson.JsonParser
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

interface EnvaseRepository {
    suspend fun getEnvasesCliente(clienteId: Int): ProductoResult<EnvasesClienteResponse>
    suspend fun setSaldoEnvases(clienteId: Int, productoId: Int, cantidad: Int): ProductoResult<EnvaseResponse>
    suspend fun llenarEnvases(nombreProducto: String, cantidad: Int): ProductoResult<EnvaseResponse>
}

class EnvaseRepositoryImpl @Inject constructor(
    private val apiServices: ApiServices
) : EnvaseRepository {

    override suspend fun getEnvasesCliente(clienteId: Int): ProductoResult<EnvasesClienteResponse> = handleApiCall {
        apiServices.getEnvasesCliente(clienteId)
    }

    override suspend fun setSaldoEnvases(clienteId: Int, productoId: Int, cantidad: Int): ProductoResult<EnvaseResponse> = handleApiCall {
        apiServices.setSaldoEnvases(SaldoEnvasesRequest(clienteId, productoId, cantidad))
    }

    override suspend fun llenarEnvases(nombreProducto: String, cantidad: Int): ProductoResult<EnvaseResponse> = handleApiCall {
        apiServices.llenarEnvases(LlenarEnvasesRequest(producto = nombreProducto, cantidad = cantidad))
    }

    private suspend fun <T> handleApiCall(call: suspend () -> retrofit2.Response<T>): ProductoResult<T> {
        return try {
            val response = call()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    ProductoResult.Success(body)
                } else {
                    ProductoResult.Error(R.string.cuerpo_nulo_error)
                }
            } else {
                serverMessageError(response) ?: ProductoResult.Error(R.string.error_servidor_format, arrayOf(response.code()))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            ProductoResult.Error(R.string.error_red_verificar)
        } catch (e: HttpException) {
            ProductoResult.Error(R.string.error_http_format, arrayOf(e.code()))
        } catch (e: Exception) {
            ProductoResult.Error(R.string.error_inesperado_format, arrayOf(e.message?.take(100) ?: ""))
        }
    }

    /**
     * Surfaces the backend's `{"success":false,"error":"<message>"}` (for example the 409
     * "No hay suficientes envases vacíos"). Returns null when the body carries no readable message.
     */
    private fun serverMessageError(response: retrofit2.Response<*>): ProductoResult.Error? {
        val error = try {
            val json = JsonParser.parseString(response.errorBody()?.string().orEmpty())
            json.asJsonObject.get("error")?.asString
        } catch (e: Exception) {
            null
        }
        if (error.isNullOrBlank()) return null
        return ProductoResult.Error(R.string.error_format, arrayOf(error))
    }
}
