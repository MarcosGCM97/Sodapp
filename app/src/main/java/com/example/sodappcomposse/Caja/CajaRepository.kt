package com.example.sodappcomposse.Caja

import com.example.sodappcomposse.API.httpErrorMessage
import androidx.annotation.StringRes
import com.example.sodappcomposse.API.ApiServices
import com.example.sodappcomposse.R
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

sealed class ReporteResult {
    data class Success(val reporte: Reporte) : ReporteResult()
    data class Error(@StringRes val messageRes: Int, val args: List<Any> = emptyList()) : ReporteResult()
}

interface CajaRepository {
    suspend fun getReporte(periodo: Periodo): ReporteResult
}

class CajaRepositoryImpl @Inject constructor(
    private val apiServices: ApiServices
) : CajaRepository {

    override suspend fun getReporte(periodo: Periodo): ReporteResult {
        val comparacion = periodo.anterior()
        return try {
            val response = apiServices.getReporte(
                desde = periodo.desde.format(FORMATO_API),
                hasta = periodo.hasta.format(FORMATO_API),
                compDesde = comparacion.desde.format(FORMATO_API),
                compHasta = comparacion.hasta.format(FORMATO_API)
            )
            val body = response.body()
            when {
                !response.isSuccessful -> ReporteResult.Error(httpErrorMessage(response.code(), R.string.error_api_format), listOf(response.code()))
                body == null -> ReporteResult.Error(R.string.cuerpo_nulo_error)
                // Never show figures that are incomplete or belong to another range
                body.success != true || body.resumen == null || body.anterior == null ||
                    body.desde != periodo.desde.format(FORMATO_API) || body.hasta != periodo.hasta.format(FORMATO_API) ->
                    ReporteResult.Error(R.string.reporte_respuesta_invalida)
                else -> ReporteResult.Success(
                    Reporte(
                        periodo = periodo,
                        resumen = body.resumen,
                        anterior = body.anterior,
                        cobros = body.cobros.orEmpty(),
                        productos = body.productos.orEmpty(),
                        clientes = body.clientes.orEmpty(),
                        dias = body.dias.orEmpty(),
                        detalle = body.detalle.orEmpty(),
                        primerCobro = parseFechaHoraApi(body.primerCobro)
                    )
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            ReporteResult.Error(R.string.error_red_format, listOf(e.message?.take(100) ?: ""))
        } catch (e: HttpException) {
            ReporteResult.Error(R.string.error_http_format, listOf(e.code()))
        } catch (e: Exception) {
            ReporteResult.Error(R.string.error_inesperado_format, listOf(e.message?.take(100) ?: ""))
        }
    }
}
