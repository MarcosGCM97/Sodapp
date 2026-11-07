package com.example.sodappcomposse.API

import androidx.annotation.StringRes
import com.example.sodappcomposse.R

/** The backend answers 403 when the user's role lacks the permission of the endpoint. */
const val HTTP_FORBIDDEN = 403

/**
 * Message for a failed HTTP answer: a 403 always becomes the "no permission" message,
 * any other code keeps the repository's own [fallback] message.
 */
@StringRes
fun httpErrorMessage(code: Int, @StringRes fallback: Int): Int =
    if (code == HTTP_FORBIDDEN) R.string.error_sin_permiso else fallback
