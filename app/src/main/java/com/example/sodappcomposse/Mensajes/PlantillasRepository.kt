package com.example.sodappcomposse.Mensajes

import androidx.annotation.StringRes
import com.example.sodappcomposse.API.ApiServices
import com.example.sodappcomposse.API.HTTP_FORBIDDEN
import com.example.sodappcomposse.R
import com.example.sodappcomposse.auth.AuthSession
import com.google.gson.JsonParser
import kotlinx.coroutines.CancellationException
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

sealed class PlantillasResult<out T> {
    data class Success<T>(val data: T) : PlantillasResult<T>()
    data class Error(@StringRes val messageRes: Int, val args: Array<Any> = emptyArray()) : PlantillasResult<Nothing>() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Error) return false
            return messageRes == other.messageRes && args.contentEquals(other.args)
        }
        override fun hashCode(): Int = 31 * messageRes + args.contentHashCode()
    }
}

/**
 * WhatsApp templates of the company (mensajes.php): every user reads them, only the owner
 * saves or restores them. Every call answers with all the saved templates by type.
 */
interface PlantillasRepository {
    suspend fun getPlantillas(): PlantillasResult<Map<TipoPlantilla, String>>
    suspend fun guardarPlantilla(tipo: TipoPlantilla, texto: String): PlantillasResult<Map<TipoPlantilla, String>>
    /** Deletes the template, so the app goes back to the default message of [tipo]. */
    suspend fun restaurarPlantilla(tipo: TipoPlantilla): PlantillasResult<Map<TipoPlantilla, String>>
    /** Last templates loaded by the current session; empty for another session or none yet. */
    fun plantillasEnCache(): Map<TipoPlantilla, String>
}

class PlantillasRepositoryImpl @Inject constructor(
    private val apiServices: ApiServices,
    private val authSession: AuthSession
) : PlantillasRepository {

    // Tagged with the session token so another login never sends this company's templates
    @Volatile
    private var cache: Pair<String?, Map<TipoPlantilla, String>>? = null

    override suspend fun getPlantillas() = llamar { apiServices.getPlantillas() }

    override suspend fun guardarPlantilla(tipo: TipoPlantilla, texto: String) =
        llamar { apiServices.guardarPlantilla(PlantillaRequest(tipo.api, texto)) }

    override suspend fun restaurarPlantilla(tipo: TipoPlantilla) =
        llamar { apiServices.restaurarPlantilla(tipo.api) }

    override fun plantillasEnCache(): Map<TipoPlantilla, String> =
        cache?.takeIf { it.first == authSession.currentToken() }?.second.orEmpty()

    private suspend fun llamar(call: suspend () -> Response<PlantillasResponse>): PlantillasResult<Map<TipoPlantilla, String>> {
        val token = authSession.currentToken()
        val response = try {
            call()
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            return PlantillasResult.Error(R.string.error_red_verificar)
        } catch (e: Exception) {
            return PlantillasResult.Error(R.string.error_inesperado_format, arrayOf(e.message?.take(100) ?: ""))
        }
        if (response.isSuccessful) {
            val body = response.body() ?: return PlantillasResult.Error(R.string.cuerpo_nulo_error)
            val plantillas = body.plantillas.orEmpty()
                .mapNotNull { (tipo, texto) -> TipoPlantilla.desdeApi(tipo)?.let { it to texto } }
                .toMap()
            cache = token to plantillas
            return PlantillasResult.Success(plantillas)
        }
        if (response.code() == HTTP_FORBIDDEN) return PlantillasResult.Error(R.string.error_sin_permiso)
        return serverMessageError(response)
            ?: PlantillasResult.Error(R.string.error_servidor_format, arrayOf(response.code()))
    }

    /** Surfaces the backend's `{"success":false,"error":"<message>"}` (400). */
    private fun serverMessageError(response: Response<*>): PlantillasResult.Error? {
        val error = try {
            JsonParser.parseString(response.errorBody()?.string().orEmpty()).asJsonObject.get("error")?.asString
        } catch (e: Exception) {
            null
        }
        if (error.isNullOrBlank()) return null
        return PlantillasResult.Error(R.string.error_format, arrayOf(error))
    }
}
