package com.example.sodappcomposse.Ruta

import com.example.sodappcomposse.API.ApiServices
import com.example.sodappcomposse.Producto.ProductoResult
import com.example.sodappcomposse.R
import com.google.gson.JsonParser
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

/** Route module (`ruta.php`): the plant, the route of a reparto and the trip start / end. */
interface RutaRepository {
    /** Plant of the user; Success(null) when it was never configured. */
    suspend fun getPlanta(): ProductoResult<Planta?>
    suspend fun guardarPlanta(planta: Planta): ProductoResult<RutaResponse>
    suspend fun getRuta(repartoId: Int): ProductoResult<Ruta?>
    /** Sets the trip start the first time only. */
    suspend fun iniciar(repartoId: Int): ProductoResult<RutaResponse>
    /** Sets the return to the plant; rejected while a stop is pendiente. */
    suspend fun finalizar(repartoId: Int): ProductoResult<RutaResponse>
}

class RutaRepositoryImpl @Inject constructor(
    private val apiServices: ApiServices
) : RutaRepository {

    override suspend fun getPlanta() = llamar({ apiServices.getPlanta() }) { it.planta }

    override suspend fun guardarPlanta(planta: Planta) = llamar({
        apiServices.accionRuta(
            RutaAccionRequest(
                accion = RutaAccionRequest.GUARDAR_PLANTA,
                direccion = planta.direccion,
                localidad = planta.localidad,
                lat = planta.lat,
                lng = planta.lng
            )
        )
    }) { it }

    override suspend fun getRuta(repartoId: Int) = llamar({ apiServices.getRuta(repartoId) }) { it.ruta }

    override suspend fun iniciar(repartoId: Int) = llamar({
        apiServices.accionRuta(RutaAccionRequest(accion = RutaAccionRequest.INICIAR, repartoId = repartoId))
    }) { it }

    override suspend fun finalizar(repartoId: Int) = llamar({
        apiServices.accionRuta(RutaAccionRequest(accion = RutaAccionRequest.FINALIZAR, repartoId = repartoId))
    }) { it }

    private suspend fun <T> llamar(
        call: suspend () -> Response<RutaResponse>,
        map: (RutaResponse) -> T
    ): ProductoResult<T> {
        val response = try {
            call()
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            return ProductoResult.Error(R.string.error_red_verificar)
        } catch (e: HttpException) {
            return ProductoResult.Error(R.string.error_http_format, arrayOf(e.code()))
        } catch (e: Exception) {
            return ProductoResult.Error(R.string.error_inesperado_format, arrayOf(e.message?.take(100) ?: ""))
        }
        if (response.isSuccessful) {
            val body = response.body() ?: return ProductoResult.Error(R.string.cuerpo_nulo_error)
            return ProductoResult.Success(map(body))
        }
        return serverMessageError(response) ?: ProductoResult.Error(R.string.error_servidor_format, arrayOf(response.code()))
    }

    /** Surfaces the backend's `{"success":false,"error":"<message>"}` (409 "Quedan paradas pendientes", ...). */
    private fun serverMessageError(response: Response<*>): ProductoResult.Error? {
        val error = try {
            JsonParser.parseString(response.errorBody()?.string().orEmpty()).asJsonObject.get("error")?.asString
        } catch (e: Exception) {
            null
        }
        if (error.isNullOrBlank()) return null
        return ProductoResult.Error(R.string.error_format, arrayOf(error))
    }
}
