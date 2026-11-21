package com.example.sodappcomposse.Mensajes

import com.example.sodappcomposse.API.ApiServices
import com.example.sodappcomposse.R
import com.example.sodappcomposse.auth.AuthSession
import com.google.gson.JsonParser
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PlantillasRepositoryImplTest {

    private lateinit var server: MockWebServer
    private lateinit var repository: PlantillasRepositoryImpl
    private var token: String? = "jwt-ariza"

    @Before
    fun setup() {
        server = MockWebServer()
        server.start()
        val api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiServices::class.java)
        val authSession = mockk<AuthSession> { every { currentToken() } answers { token } }
        repository = PlantillasRepositoryImpl(api, authSession)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun responde(code: Int, body: String) {
        server.enqueue(MockResponse().setResponseCode(code).setBody(body))
    }

    private val guardadas = """{"success":true,"plantillas":{"deuda":"Hola {cliente}","venta":"Gracias {cliente}"}}"""
    private val mapa = mapOf(TipoPlantilla.DEUDA to "Hola {cliente}", TipoPlantilla.VENTA to "Gracias {cliente}")

    @Test
    fun `getPlantillas reads the saved templates by type`() = runTest {
        responde(200, guardadas)

        val result = repository.getPlantillas()

        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/mensajes.php", request.path)
        assertEquals(PlantillasResult.Success(mapa), result)
        assertEquals(mapa, repository.plantillasEnCache())
    }

    @Test
    fun `an empty object and unknown types give only the known templates`() = runTest {
        responde(200, """{"success":true,"plantillas":{}}""")
        assertEquals(PlantillasResult.Success(emptyMap<TipoPlantilla, String>()), repository.getPlantillas())

        responde(200, """{"success":true,"plantillas":{"resumen":"x","envases":"{envases}"}}""")
        assertEquals(PlantillasResult.Success(mapOf(TipoPlantilla.ENVASES to "{envases}")), repository.getPlantillas())
    }

    @Test
    fun `guardarPlantilla puts the type and text and returns every template`() = runTest {
        responde(200, guardadas)

        val result = repository.guardarPlantilla(TipoPlantilla.VENTA, "Gracias {cliente}")

        val request = server.takeRequest()
        assertEquals("PUT", request.method)
        assertEquals("/mensajes.php", request.path)
        val body = JsonParser.parseString(request.body.readUtf8()).asJsonObject
        assertEquals("venta", body.get("tipo").asString)
        assertEquals("Gracias {cliente}", body.get("texto").asString)
        assertEquals(PlantillasResult.Success(mapa), result)
        assertEquals(mapa, repository.plantillasEnCache())
    }

    @Test
    fun `restaurarPlantilla deletes the template of the type`() = runTest {
        responde(200, """{"success":true,"plantillas":{"deuda":"Hola {cliente}"}}""")

        val result = repository.restaurarPlantilla(TipoPlantilla.VENTA)

        val request = server.takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/mensajes.php?tipo=venta", request.path)
        assertEquals(PlantillasResult.Success(mapOf(TipoPlantilla.DEUDA to "Hola {cliente}")), result)
    }

    @Test
    fun `the cache belongs to the session that loaded it`() = runTest {
        responde(200, guardadas)
        repository.getPlantillas()

        token = "jwt-congre"

        assertEquals(emptyMap<TipoPlantilla, String>(), repository.plantillasEnCache())
    }

    @Test
    fun `a failed request keeps the cache of the session`() = runTest {
        responde(200, guardadas)
        repository.getPlantillas()
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))

        assertEquals(PlantillasResult.Error(R.string.error_red_verificar), repository.getPlantillas())
        assertEquals(mapa, repository.plantillasEnCache())
    }

    @Test
    fun `400 surfaces the backend message`() = runTest {
        responde(400, """{"success":false,"error":"Variable no permitida en deuda: {total}. Permitidas: {cliente}, {saldo}, {empresa}"}""")

        assertEquals(
            PlantillasResult.Error(R.string.error_format, arrayOf("Variable no permitida en deuda: {total}. Permitidas: {cliente}, {saldo}, {empresa}")),
            repository.guardarPlantilla(TipoPlantilla.DEUDA, "{total}")
        )
    }

    @Test
    fun `403 is the missing permission message`() = runTest {
        responde(403, """{"success":false,"error":"Forbidden: missing permission","permiso":"admin"}""")

        assertEquals(PlantillasResult.Error(R.string.error_sin_permiso), repository.restaurarPlantilla(TipoPlantilla.DEUDA))
    }

    @Test
    fun `a server error without message keeps the generic message`() = runTest {
        responde(500, "")

        assertEquals(PlantillasResult.Error(R.string.error_servidor_format, arrayOf(500)), repository.getPlantillas())
    }
}
