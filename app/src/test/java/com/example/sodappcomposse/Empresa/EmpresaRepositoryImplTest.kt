package com.example.sodappcomposse.Empresa

import com.example.sodappcomposse.API.ApiServices
import com.example.sodappcomposse.R
import com.google.gson.JsonParser
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class EmpresaRepositoryImplTest {

    private lateinit var server: MockWebServer
    private lateinit var repository: EmpresaRepositoryImpl

    @Before
    fun setup() {
        server = MockWebServer()
        server.start()
        val api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiServices::class.java)
        repository = EmpresaRepositoryImpl(api)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun responde(code: Int, body: String) {
        server.enqueue(MockResponse().setResponseCode(code).setBody(body))
    }

    @Test
    fun `getEmpresa reads the company data`() = runTest {
        responde(200, """{"success":true,"empresa":{"em_nom":"Soderia La Calera","em_tel":"351 555","em_dir":"San Martín 123"}}""")

        val result = repository.getEmpresa()

        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/empresa.php", request.path)
        assertEquals(EmpresaResult.Success(Empresa("Soderia La Calera", "351 555", "San Martín 123")), result)
    }

    @Test
    fun `getEmpresa without saved data is null`() = runTest {
        responde(200, """{"success":true,"empresa":null}""")

        assertEquals(EmpresaResult.Success(null), repository.getEmpresa())
    }

    @Test
    fun `guardarEmpresa puts the trimmed fields and returns the stored data`() = runTest {
        responde(200, """{"success":true,"empresa":{"em_nom":"Soderia La Calera","em_tel":null,"em_dir":"San Martín 123"}}""")

        val result = repository.guardarEmpresa(Empresa(" Soderia La Calera ", "  ", " San Martín 123 "))

        val request = server.takeRequest()
        assertEquals("PUT", request.method)
        assertEquals("/empresa.php", request.path)
        val body = JsonParser.parseString(request.body.readUtf8()).asJsonObject
        assertEquals("Soderia La Calera", body.get("nombre").asString)
        assertTrue(body.get("telefono") == null || body.get("telefono").isJsonNull)
        assertEquals("San Martín 123", body.get("direccion").asString)
        assertEquals(EmpresaResult.Success(Empresa("Soderia La Calera", null, "San Martín 123")), result)
    }

    @Test
    fun `400 surfaces the backend message`() = runTest {
        responde(400, """{"success":false,"error":"Nombre inválido: hasta 80 caracteres"}""")

        assertEquals(
            EmpresaResult.Error(R.string.error_format, arrayOf("Nombre inválido: hasta 80 caracteres")),
            repository.guardarEmpresa(Empresa("x", null, null))
        )
    }

    @Test
    fun `403 is the missing permission message`() = runTest {
        responde(403, """{"success":false,"error":"Forbidden: missing permission","permiso":"admin"}""")

        assertEquals(EmpresaResult.Error(R.string.error_sin_permiso), repository.guardarEmpresa(Empresa("x", null, null)))
    }

    @Test
    fun `a server error without message keeps the generic message`() = runTest {
        responde(500, "")

        assertEquals(EmpresaResult.Error(R.string.error_servidor_format, arrayOf(500)), repository.getEmpresa())
    }

    @Test
    fun `cambiarContrasena puts the current and the new password untrimmed`() = runTest {
        responde(200, """{"success":true}""")

        val result = repository.cambiarContrasena(actual = " vieja1", nueva = "nueva12 ")

        val request = server.takeRequest()
        assertEquals("PUT", request.method)
        assertEquals("/contrasena.php", request.path)
        val body = JsonParser.parseString(request.body.readUtf8()).asJsonObject
        assertEquals(" vieja1", body.get("actual").asString)
        assertEquals("nueva12 ", body.get("nueva").asString)
        assertEquals(EmpresaResult.Success(Unit), result)
    }

    @Test
    fun `cambiarContrasena surfaces the wrong current password message`() = runTest {
        responde(400, """{"success":false,"error":"La contraseña actual es incorrecta"}""")

        assertEquals(
            EmpresaResult.Error(R.string.error_format, arrayOf("La contraseña actual es incorrecta")),
            repository.cambiarContrasena("x", "nueva12")
        )
    }

    @Test
    fun `cambiarContrasena 403 is the missing permission message`() = runTest {
        responde(403, """{"success":false,"error":"Forbidden: missing permission","permiso":"admin"}""")

        assertEquals(EmpresaResult.Error(R.string.error_sin_permiso), repository.cambiarContrasena("x", "nueva12"))
    }

    @Test
    fun `a network failure is the network message`() = runTest {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))

        assertEquals(EmpresaResult.Error(R.string.error_red_verificar), repository.getEmpresa())
    }
}
