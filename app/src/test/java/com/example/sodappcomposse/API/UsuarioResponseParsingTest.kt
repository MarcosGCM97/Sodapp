package com.example.sodappcomposse.API

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UsuarioResponseParsingTest {

    private val gson = Gson()

    @Test
    fun `login response exposes the user and the jwt token`() {
        val json = """
            {"success":true,"message":"Bienvenido","usuario":{"us_ide":5,"us_nom":"marcos"},"token":"header.payload.signature"}
        """.trimIndent()

        val response = gson.fromJson(json, UsuarioResponse::class.java)

        assertEquals(5, response.usuario?.idUs)
        assertEquals("marcos", response.usuario?.nombreUs)
        assertEquals("header.payload.signature", response.token)
    }

    @Test
    fun `failed login response has no user and no token`() {
        val json = """{"success":false,"message":"Credenciales incorrectas"}"""

        val response = gson.fromJson(json, UsuarioResponse::class.java)

        assertNull(response.usuario)
        assertNull(response.token)
    }
}
