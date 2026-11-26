package com.example.sodappcomposse.API

import androidx.annotation.StringRes
import com.example.sodappcomposse.R
import com.google.gson.JsonParser
import retrofit2.Response

/** The backend answers 403 when the user's role lacks the permission of the endpoint. */
const val HTTP_FORBIDDEN = 403

/**
 * Message for a failed HTTP answer: a 403 always becomes the "no permission" message,
 * any other code keeps the repository's own [fallback] message.
 */
@StringRes
fun httpErrorMessage(code: Int, @StringRes fallback: Int): Int =
    if (code == HTTP_FORBIDDEN) R.string.error_sin_permiso else fallback

/** The backend answers 409 when the data conflicts with existing data (e.g. a repeated name). */
const val HTTP_CONFLICT = 409

/** The `error` text of a backend `{"success":false,"error":"..."}` answer, or null without one. */
fun backendErrorMessage(response: Response<*>): String? {
    val error = try {
        JsonParser.parseString(response.errorBody()?.string().orEmpty()).asJsonObject.get("error")?.asString
    } catch (e: Exception) {
        null
    }
    return error?.takeIf { it.isNotBlank() }
}
