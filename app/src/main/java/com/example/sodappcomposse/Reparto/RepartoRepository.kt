package com.example.sodappcomposse.Reparto

import com.example.sodappcomposse.API.ApiServices
import com.example.sodappcomposse.Producto.ProductoResult
import com.example.sodappcomposse.R
import com.google.gson.JsonParser
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

interface RepartoRepository {
    /** Reparto of [fecha] (YYYY-MM-DD); Success(null) when that date has none yet. */
    suspend fun getReparto(fecha: String): ProductoResult<Reparto?>
    suspend fun crearReparto(fecha: String, origen: OrigenReparto): ProductoResult<RepartoResponse>
    suspend fun guardarPlantilla(repartoId: Int): ProductoResult<RepartoResponse>
    suspend fun agregarParada(repartoId: Int, clienteId: Int): ProductoResult<RepartoResponse>
    suspend fun quitarParada(paradaId: Int): ProductoResult<RepartoResponse>
    suspend fun guardarLineas(paradaId: Int, lineas: List<LineaRepartoRequest>): ProductoResult<RepartoResponse>
    /** [paradaIds] holds every stop of the reparto, in the new order. */
    suspend fun ordenarParadas(repartoId: Int, paradaIds: List<Int>): ProductoResult<RepartoResponse>
}

class RepartoRepositoryImpl @Inject constructor(
    private val apiServices: ApiServices
) : RepartoRepository {

    override suspend fun getReparto(fecha: String): ProductoResult<Reparto?> {
        val response = try {
            apiServices.getReparto(fecha)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return exceptionError(e)
        }
        // 404 is the normal answer for a date nobody planned yet
        if (response.code() == 404) return ProductoResult.Success(null)
        return when (val result = responseResult(response)) {
            is ProductoResult.Success -> ProductoResult.Success(result.data.reparto)
            is ProductoResult.Error -> result
        }
    }

    override suspend fun crearReparto(fecha: String, origen: OrigenReparto) = accion(
        RepartoAccionRequest(accion = RepartoAccionRequest.CREAR, fecha = fecha, origen = origen.valor)
    )

    override suspend fun guardarPlantilla(repartoId: Int) = accion(
        RepartoAccionRequest(accion = RepartoAccionRequest.GUARDAR_PLANTILLA, repartoId = repartoId)
    )

    override suspend fun agregarParada(repartoId: Int, clienteId: Int) = accion(
        RepartoAccionRequest(accion = RepartoAccionRequest.AGREGAR_PARADA, repartoId = repartoId, clienteId = clienteId)
    )

    override suspend fun quitarParada(paradaId: Int) = accion(
        RepartoAccionRequest(accion = RepartoAccionRequest.QUITAR_PARADA, paradaId = paradaId)
    )

    override suspend fun guardarLineas(paradaId: Int, lineas: List<LineaRepartoRequest>) = accion(
        RepartoAccionRequest(accion = RepartoAccionRequest.GUARDAR_LINEAS, paradaId = paradaId, lineas = lineas)
    )

    override suspend fun ordenarParadas(repartoId: Int, paradaIds: List<Int>) = accion(
        RepartoAccionRequest(accion = RepartoAccionRequest.ORDENAR, repartoId = repartoId, paradas = paradaIds)
    )

    private suspend fun accion(request: RepartoAccionRequest): ProductoResult<RepartoResponse> {
        return try {
            responseResult(apiServices.accionReparto(request))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            exceptionError(e)
        }
    }

    private fun <T> responseResult(response: retrofit2.Response<T>): ProductoResult<T> {
        if (response.isSuccessful) {
            val body = response.body() ?: return ProductoResult.Error(R.string.cuerpo_nulo_error)
            return ProductoResult.Success(body)
        }
        return serverMessageError(response) ?: ProductoResult.Error(R.string.error_servidor_format, arrayOf(response.code()))
    }

    private fun exceptionError(e: Exception): ProductoResult.Error = when (e) {
        is IOException -> ProductoResult.Error(R.string.error_red_verificar)
        is HttpException -> ProductoResult.Error(R.string.error_http_format, arrayOf(e.code()))
        else -> ProductoResult.Error(R.string.error_inesperado_format, arrayOf(e.message?.take(100) ?: ""))
    }

    /** Surfaces the backend's `{"success":false,"error":"<message>"}` (409, "no pendiente", ...). */
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
