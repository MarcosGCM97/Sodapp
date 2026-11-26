package com.example.sodappcomposse.Mensajes

import androidx.annotation.StringRes
import com.example.sodappcomposse.R
import com.google.gson.annotations.SerializedName

/**
 * WhatsApp message types the owner can customize (mensajes.php). [variables] are the
 * `{variable}` names allowed in each template, in the order the editor offers them.
 */
enum class TipoPlantilla(
    val api: String,
    val variables: List<String>,
    @StringRes val tituloRes: Int
) {
    DEUDA("deuda", listOf("cliente", "saldo", "empresa"), R.string.mensaje_tipo_deuda),
    ENVASES("envases", listOf("cliente", "envases", "empresa"), R.string.mensaje_tipo_envases),
    VENTA("venta", listOf("cliente", "productos", "total", "saldo", "empresa"), R.string.mensaje_tipo_venta);

    companion object {
        fun desdeApi(valor: String): TipoPlantilla? = entries.find { it.api == valor }
    }
}

/** GET, PUT and DELETE mensajes.php: only the types with a saved template. */
data class PlantillasResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("plantillas")
    val plantillas: Map<String, String>?
)

/** PUT mensajes.php body. */
data class PlantillaRequest(
    @SerializedName("tipo")
    val tipo: String,
    @SerializedName("texto")
    val texto: String
)
