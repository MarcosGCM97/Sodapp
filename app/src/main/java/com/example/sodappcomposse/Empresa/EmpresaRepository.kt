package com.example.sodappcomposse.Empresa

import androidx.annotation.StringRes
import com.example.sodappcomposse.API.ApiServices
import com.example.sodappcomposse.API.HTTP_FORBIDDEN
import com.example.sodappcomposse.R
import com.google.gson.JsonParser
import kotlinx.coroutines.CancellationException
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

sealed class EmpresaResult<out T> {
    data class Success<T>(val data: T) : EmpresaResult<T>()
    data class Error(@StringRes val messageRes: Int, val args: Array<Any> = emptyArray()) : EmpresaResult<Nothing>() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Error) return false
            return messageRes == other.messageRes && args.contentEquals(other.args)
        }
        override fun hashCode(): Int = 31 * messageRes + args.contentHashCode()
    }
}

/** Company data (empresa.php): every user reads it, only the owner saves it. */
interface EmpresaRepository {
    /** The saved company data, or null when the owner never saved it. */
    suspend fun getEmpresa(): EmpresaResult<Empresa?>
    /** Saves trimmed fields (blank phone/address are sent as null); returns what the server stored. */
    suspend fun guardarEmpresa(empresa: Empresa): EmpresaResult<Empresa?>
    /** Changes the owner's own password; both are sent as typed (a password is never trimmed). */
    suspend fun cambiarContrasena(actual: String, nueva: String): EmpresaResult<Unit>
}

class EmpresaRepositoryImpl @Inject constructor(
    private val apiServices: ApiServices
) : EmpresaRepository {

    override suspend fun getEmpresa(): EmpresaResult<Empresa?> =
        llamar { apiServices.getEmpresa() }

    override suspend fun guardarEmpresa(empresa: Empresa): EmpresaResult<Empresa?> =
        llamar {
            apiServices.guardarEmpresa(
                EmpresaRequest(
                    nombre = empresa.nombre.trim(),
                    telefono = empresa.telefono?.trim()?.ifEmpty { null },
                    direccion = empresa.direccion?.trim()?.ifEmpty { null }
                )
            )
        }

    override suspend fun cambiarContrasena(actual: String, nueva: String): EmpresaResult<Unit> =
        llamarApi({ apiServices.cambiarContrasena(CambiarContrasenaRequest(actual, nueva)) }) { }

    private suspend fun llamar(call: suspend () -> Response<EmpresaResponse>): EmpresaResult<Empresa?> =
        llamarApi(call) { it.empresa?.toEmpresa() }

    /** Shared error mapping: network, 403, the backend's 400 message, or the status code. */
    private suspend fun <B, T> llamarApi(call: suspend () -> Response<B>, map: (B) -> T): EmpresaResult<T> {
        val response = try {
            call()
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            return EmpresaResult.Error(R.string.error_red_verificar)
        } catch (e: Exception) {
            return EmpresaResult.Error(R.string.error_inesperado_format, arrayOf(e.message?.take(100) ?: ""))
        }
        if (response.isSuccessful) {
            val body = response.body() ?: return EmpresaResult.Error(R.string.cuerpo_nulo_error)
            return EmpresaResult.Success(map(body))
        }
        if (response.code() == HTTP_FORBIDDEN) return EmpresaResult.Error(R.string.error_sin_permiso)
        return serverMessageError(response)
            ?: EmpresaResult.Error(R.string.error_servidor_format, arrayOf(response.code()))
    }

    private fun EmpresaDto.toEmpresa() = Empresa(nombre = nombre.orEmpty(), telefono = telefono, direccion = direccion)

    /** Surfaces the backend's `{"success":false,"error":"<message>"}` (400). */
    private fun serverMessageError(response: Response<*>): EmpresaResult.Error? {
        val error = try {
            JsonParser.parseString(response.errorBody()?.string().orEmpty()).asJsonObject.get("error")?.asString
        } catch (e: Exception) {
            null
        }
        if (error.isNullOrBlank()) return null
        return EmpresaResult.Error(R.string.error_format, arrayOf(error))
    }
}
