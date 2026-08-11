package com.example.sodappcomposse.Cliente

import com.example.sodappcomposse.API.ApiServices
import com.example.sodappcomposse.API.PostResponse
import com.example.sodappcomposse.R
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

sealed class AgendaResult<out T> {
    data class Success<T>(val data: T) : AgendaResult<T>()
    data class Error(val messageRes: Int, val args: Array<Any> = emptyArray()) : AgendaResult<Nothing>() {
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

interface AgendaRepository {
    suspend fun getDiasEntrega(): AgendaResult<TodosLosDias>
    suspend fun getDiasEntregaById(id: Int): AgendaResult<DiasEntregaByid>
    suspend fun updateDiasEntrega(diasEntrega: DiasEntrega): AgendaResult<PostResponse>
}

class AgendaRepositoryImpl @Inject constructor(
    private val apiServices: ApiServices
) : AgendaRepository {
    override suspend fun getDiasEntrega(): AgendaResult<TodosLosDias> = handleApiCall {
        apiServices.getDiasEntrega()
    }

    override suspend fun getDiasEntregaById(id: Int): AgendaResult<DiasEntregaByid> = handleApiCall {
        apiServices.getDiasEntregaById(id)
    }

    override suspend fun updateDiasEntrega(diasEntrega: DiasEntrega): AgendaResult<PostResponse> = handleApiCall {
        apiServices.updateDiasEntrega(diasEntrega)
    }

    private suspend fun <T> handleApiCall(call: suspend () -> retrofit2.Response<T>): AgendaResult<T> {
        return try {
            val response = call()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    AgendaResult.Success(body)
                } else {
                    AgendaResult.Error(R.string.cuerpo_nulo_error)
                }
            } else {
                AgendaResult.Error(R.string.error_servidor_format, arrayOf(response.code()))
            }
        } catch (e: IOException) {
            AgendaResult.Error(R.string.error_red_verificar)
        } catch (e: HttpException) {
            AgendaResult.Error(R.string.error_http_format, arrayOf(e.code()))
        } catch (e: Exception) {
            AgendaResult.Error(R.string.error_inesperado_format, arrayOf(e.message?.take(100) ?: ""))
        }
    }
}
