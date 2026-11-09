package com.example.sodappcomposse.API

import com.google.gson.Gson
import com.example.sodappcomposse.auth.SessionPermissions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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

    @Test
    fun `login response exposes the owner flag and the permission codes`() {
        val json = """
            {"success":true,"message":"ok","usuario":{"us_ide":7,"us_nom":"juan","us_emp":5,"us_rol":2,"us_act":1},
             "token":"t","es_dueno":false,"permisos":["ventas.ver","clientes.ver"]}
        """.trimIndent()

        val response = gson.fromJson(json, UsuarioResponse::class.java)

        assertFalse(response.esDueno)
        assertEquals(
            SessionPermissions(esDueno = false, permisos = setOf("ventas.ver", "clientes.ver")),
            response.sessionPermissions()
        )
    }

    @Test
    fun `login response of the owner marks it as owner`() {
        val json = """{"success":true,"message":"ok","usuario":{"us_ide":5,"us_nom":"marcos"},"token":"t","es_dueno":true,"permisos":[]}"""

        val response = gson.fromJson(json, UsuarioResponse::class.java)

        assertTrue(response.sessionPermissions().esDueno)
    }

    @Test
    fun `login response without role fields means no permissions and not owner`() {
        val json = """{"success":true,"message":"ok","usuario":{"us_ide":5,"us_nom":"marcos"},"token":"t"}"""

        val response = gson.fromJson(json, UsuarioResponse::class.java)

        assertEquals(SessionPermissions(), response.sessionPermissions())
    }

    @Test
    fun `sesion response exposes the current permissions`() {
        val json = """{"success":true,"us_ide":7,"us_nom":"juan","es_dueno":false,"permisos":["repartos.ver"]}"""

        val response = gson.fromJson(json, SesionResponse::class.java)

        assertEquals(7, response.idUs)
        assertEquals(SessionPermissions(esDueno = false, permisos = setOf("repartos.ver")), response.sessionPermissions())
    }

    @Test
    fun `sesion response without permissions defaults to none`() {
        val json = """{"success":true,"us_ide":7,"us_nom":"juan"}"""

        val response = gson.fromJson(json, SesionResponse::class.java)

        assertEquals(SessionPermissions(), response.sessionPermissions())
    }
}
