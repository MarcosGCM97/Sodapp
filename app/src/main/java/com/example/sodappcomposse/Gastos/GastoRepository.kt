package com.example.sodappcomposse.Gastos

import androidx.annotation.StringRes
import com.example.sodappcomposse.API.ApiServices
import com.example.sodappcomposse.API.HTTP_FORBIDDEN
import com.example.sodappcomposse.R
import com.google.gson.JsonParser
import kotlinx.coroutines.CancellationException
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

sealed class GastoResult<out T> {
    data class Success<T>(val data: T) : GastoResult<T>()
    data class Error(@StringRes val messageRes: Int, val args: Array<Any> = emptyArray()) : GastoResult<Nothing>() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Error) return false
            return messageRes == other.messageRes && args.contentEquals(other.args)
        }
        override fun hashCode(): Int = 31 * messageRes + args.contentHashCode()
    }
}

/** Business expenses of the company (gastos.php). */
interface GastoRepository {
    /** Expenses of the inclusive period (yyyy-MM-dd), optionally of one category. */
    suspend fun listar(desde: String, hasta: String, categoria: CategoriaGasto?): GastoResult<GastosPeriodo>

    /** Creates the expense, or replaces it when it has an id. */
    suspend fun guardar(gasto: NuevoGasto): GastoResult<Gasto>

    suspend fun eliminar(id: Int): GastoResult<Unit>
}

class GastoRepositoryImpl @Inject constructor(
    private val apiServices: ApiServices
) : GastoRepository {

    override suspend fun listar(desde: String, hasta: String, categoria: CategoriaGasto?): GastoResult<GastosPeriodo> =
        llamar({ apiServices.getGastos(desde, hasta, categoria?.codigo) }) {
            GastosPeriodo(it.desde ?: desde, it.hasta ?: hasta, it.gastos.orEmpty(), it.total)
        }

    override suspend fun guardar(gasto: NuevoGasto): GastoResult<Gasto> {
        val request = gasto.toRequest()
        val call: suspend () -> Response<GastoResponse> =
            if (gasto.id == null) ({ apiServices.crearGasto(request) }) else ({ apiServices.actualizarGasto(request) })
        return when (val result = llamar(call) { it.gasto }) {
            is GastoResult.Success -> result.data?.let { GastoResult.Success(it) } ?: GastoResult.Error(R.string.cuerpo_nulo_error)
            is GastoResult.Error -> result
        }
    }

    override suspend fun eliminar(id: Int): GastoResult<Unit> =
        llamar({ apiServices.eliminarGasto(id) }) { }

    private fun NuevoGasto.toRequest(): GastoRequest {
        val conProducto = categoria.mueveStock
        return GastoRequest(
            id = id,
            categoria = categoria.codigo,
            monto = monto,
            fecha = fecha,
            descripcion = descripcion?.trim()?.takeIf { it.isNotEmpty() },
            repartoId = repartoId,
            productoId = productoId.takeIf { conProducto },
            cantidad = cantidad.takeIf { conProducto },
            // A refill has no state; the screen only offers it for a returnable product
            estado = estado?.codigo.takeIf { categoria == CategoriaGasto.COMPRA_PRODUCTO }
        )
    }

    private suspend fun <B, T> llamar(call: suspend () -> Response<B>, map: (B) -> T): GastoResult<T> {
        val response = try {
            call()
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            return GastoResult.Error(R.string.error_red_verificar)
        } catch (e: Exception) {
            return GastoResult.Error(R.string.error_inesperado_format, arrayOf(e.message?.take(100) ?: ""))
        }
        if (response.isSuccessful) {
            val body = response.body() ?: return GastoResult.Error(R.string.cuerpo_nulo_error)
            return GastoResult.Success(map(body))
        }
        if (response.code() == HTTP_FORBIDDEN) return GastoResult.Error(R.string.error_sin_permiso)
        return serverMessageError(response)
            ?: GastoResult.Error(R.string.error_servidor_format, arrayOf(response.code()))
    }

    /** Surfaces the backend's `{"success":false,"error":"<message>"}` (400, 404, 409). */
    private fun serverMessageError(response: Response<*>): GastoResult.Error? {
        val error = try {
            JsonParser.parseString(response.errorBody()?.string().orEmpty()).asJsonObject.get("error")?.asString
        } catch (e: Exception) {
            null
        }
        if (error.isNullOrBlank()) return null
        return GastoResult.Error(R.string.error_format, arrayOf(error))
    }
}
