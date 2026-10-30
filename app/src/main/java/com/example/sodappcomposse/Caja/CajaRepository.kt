package com.example.sodappcomposse.Caja

import androidx.annotation.StringRes
import com.example.sodappcomposse.API.ApiServices
import com.example.sodappcomposse.R
import kotlinx.coroutines.CancellationException
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

sealed class CobrosResult {
    data class Success(val totales: List<CobroForma>) : CobrosResult()
    data class Error(@StringRes val messageRes: Int, val args: List<Any> = emptyList()) : CobrosResult()
}

interface CajaRepository {
    suspend fun getCajaPorMes(mes: Int): CajaResult
    suspend fun getCobrosPorMes(mes: Int): CobrosResult
}

class CajaRepositoryImpl @Inject constructor(
    private val apiServices: ApiServices
) : CajaRepository {
    override suspend fun getCobrosPorMes(mes: Int): CobrosResult {
        return try {
            val response = apiServices.getCobrosPorMes(mes)
            val body = response.body()
            when {
                !response.isSuccessful -> CobrosResult.Error(R.string.error_api_format, listOf(response.code()))
                body == null -> CobrosResult.Error(R.string.cuerpo_nulo_error)
                // A month without payments is a valid result, not an error
                else -> CobrosResult.Success(body.totales ?: emptyList())
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            CobrosResult.Error(R.string.error_red_format, listOf(e.message?.take(100) ?: ""))
        } catch (e: HttpException) {
            CobrosResult.Error(R.string.error_http_format, listOf(e.code()))
        } catch (e: Exception) {
            CobrosResult.Error(R.string.error_inesperado_format, listOf(e.message?.take(100) ?: ""))
        }
    }

    override suspend fun getCajaPorMes(mes: Int): CajaResult {
        return try {
            val response = apiServices.getCajaPorMes(mes)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    if (body.success == true) {
                        // An empty month is a valid result, not an error
                        CajaResult.Success(body.copy(caja = body.caja ?: emptyList()))
                    } else {
                        CajaResult.Error(R.string.no_ventas_mes_error)
                    }
                } else {
                    CajaResult.Error(R.string.cuerpo_nulo_error)
                }
            } else {
                CajaResult.Error(R.string.error_api_format, arrayOf(response.code()))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            CajaResult.Error(R.string.error_red_format, arrayOf(e.message?.take(100) ?: ""))
        } catch (e: HttpException) {
            CajaResult.Error(R.string.error_http_format, arrayOf(e.code()))
        } catch (e: Exception) {
            CajaResult.Error(R.string.error_inesperado_format, arrayOf(e.message?.take(100) ?: ""))
        }
    }
}
