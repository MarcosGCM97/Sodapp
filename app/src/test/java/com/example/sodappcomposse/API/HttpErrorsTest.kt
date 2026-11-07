package com.example.sodappcomposse.API

import com.example.sodappcomposse.R
import org.junit.Assert.assertEquals
import org.junit.Test

class HttpErrorsTest {

    @Test
    fun `403 maps to the no permission message`() {
        assertEquals(R.string.error_sin_permiso, httpErrorMessage(403, R.string.error_servidor_format))
    }

    @Test
    fun `other codes keep the fallback message`() {
        assertEquals(R.string.error_servidor_format, httpErrorMessage(500, R.string.error_servidor_format))
        assertEquals(R.string.error_api_format, httpErrorMessage(404, R.string.error_api_format))
    }
}
