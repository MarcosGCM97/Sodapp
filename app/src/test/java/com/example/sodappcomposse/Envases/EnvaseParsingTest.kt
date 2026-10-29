package com.example.sodappcomposse.Envases

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Test

class EnvaseParsingTest {

    @Test
    fun `the client balance response maps pr_ide, pr_nom and ce_can`() {
        val json = """{"success":true,"envases":[{"pr_ide":"1","pr_nom":"Bidon 20L","ce_can":"4"},{"pr_ide":2,"pr_nom":"Bidon 12L","ce_can":-1}]}"""

        val response = Gson().fromJson(json, EnvasesClienteResponse::class.java)

        assertEquals(
            listOf(EnvaseCliente(1, "Bidon 20L", 4), EnvaseCliente(2, "Bidon 12L", -1)),
            response.envases
        )
    }

    @Test
    fun `the refill and balance requests use the API field names`() {
        val gson = Gson()

        assertEquals(
            """{"accion":"llenar","producto":"Bidon 20L","cantidad":3}""",
            gson.toJson(LlenarEnvasesRequest(producto = "Bidon 20L", cantidad = 3))
        )
        assertEquals(
            """{"clienteId":25,"productoId":1,"cantidad":6}""",
            gson.toJson(SaldoEnvasesRequest(25, 1, 6))
        )
    }
}
