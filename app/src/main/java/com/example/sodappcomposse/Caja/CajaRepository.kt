package com.example.sodappcomposse.Caja

import com.example.sodappcomposse.API.ApiServices
import com.example.sodappcomposse.R
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

sealed class CajaResult {
    data class Success(val data: DataCajaResponse) : CajaResult()
    data class Error(val messageRes: Int, val args: Array<Any> = emptyArray()) : CajaResult() {
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

interface CajaRepository {
    suspend fun getCajaPorMes(mes: Int): CajaResult
}

class CajaRepositoryImpl @Inject constructor(
    private val apiServices: ApiServices
) : CajaRepository {
    override suspend fun getCajaPorMes(mes: Int): CajaResult {
        return try {
            val response = apiServices.getCajaPorMes(mes)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    if (body.success == true && !body.caja.isNullOrEmpty()) {
                        CajaResult.Success(body)
                    } else {
                        CajaResult.Error(R.string.no_ventas_mes_error)
                    }
                } else {
                    CajaResult.Error(R.string.cuerpo_nulo_error)
                }
            } else {
                CajaResult.Error(R.string.error_api_format, arrayOf(response.code()))
            }
        } catch (e: IOException) {
            CajaResult.Error(R.string.error_red_format, arrayOf(e.message?.take(100) ?: ""))
        } catch (e: HttpException) {
            CajaResult.Error(R.string.error_http_format, arrayOf(e.code()))
        } catch (e: Exception) {
            CajaResult.Error(R.string.error_inesperado_format, arrayOf(e.message?.take(100) ?: ""))
        }
    }
}
