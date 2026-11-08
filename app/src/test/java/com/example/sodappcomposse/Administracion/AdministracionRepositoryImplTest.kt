package com.example.sodappcomposse.Administracion

import com.example.sodappcomposse.API.ApiServices
import com.example.sodappcomposse.R
import com.google.gson.JsonParser
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AdministracionRepositoryImplTest {

    private lateinit var server: MockWebServer
    private lateinit var repository: AdministracionRepositoryImpl

    @Before
    fun setup() {
        server = MockWebServer()
        server.start()
        val api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiServices::class.java)
        repository = AdministracionRepositoryImpl(api)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun responde(code: Int, body: String) {
        server.enqueue(MockResponse().setResponseCode(code).setBody(body))
    }

    private fun ok() = responde(200, """{"success":true}""")

    private fun RecordedRequest.json() = JsonParser.parseString(body.readUtf8())

    @Test
    fun `getUsuarios lists the company users with the owner`() = runTest {
        responde(
            200,
            """{"success":true,"usuarios":[
                {"us_ide":5,"us_nom":"marcos","us_rol":null,"ro_nom":null,"us_act":1,"es_dueno":true},
                {"us_ide":7,"us_nom":"juan","us_rol":2,"ro_nom":"Vendedor","us_act":0,"es_dueno":false}]}"""
        )

        val result = repository.getUsuarios()

        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/usuarios.php", request.path)
        assertEquals(
            AdminResult.Success(
                listOf(
                    UsuarioAdmin(5, "marcos", null, null, 1, true),
                    UsuarioAdmin(7, "juan", 2, "Vendedor", 0, false)
                )
            ),
            result
        )
    }

    @Test
    fun `crearUsuario posts the trimmed name, the password and the role`() = runTest {
        responde(201, """{"success":true,"us_ide":9}""")

        val result = repository.crearUsuario("  juan  ", "secreto1", 2)

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/usuarios.php", request.path)
        assertEquals(JsonParser.parseString("""{"nombre":"juan","contrasena":"secreto1","rol":2}"""), request.json())
        assertEquals(AdminResult.Success(Unit), result)
    }

    @Test
    fun `cambiarRol puts only the id and the role`() = runTest {
        ok()

        repository.cambiarRol(7, 3)

        val request = server.takeRequest()
        assertEquals("PUT", request.method)
        assertEquals("/usuarios.php", request.path)
        assertEquals(JsonParser.parseString("""{"id":7,"rol":3}"""), request.json())
    }

    @Test
    fun `cambiarContrasena puts only the id and the password`() = runTest {
        ok()

        repository.cambiarContrasena(7, "nueva123")

        assertEquals(JsonParser.parseString("""{"id":7,"contrasena":"nueva123"}"""), server.takeRequest().json())
    }

    @Test
    fun `cambiarActivo puts only the id and the active flag`() = runTest {
        ok()

        repository.cambiarActivo(7, false)

        assertEquals(JsonParser.parseString("""{"id":7,"activo":false}"""), server.takeRequest().json())
    }

    @Test
    fun `getRoles returns the roles and the permission catalog`() = runTest {
        responde(
            200,
            """{"success":true,"roles":[{"ro_ide":2,"ro_nom":"Vendedor","permisos":["ventas.ver"],"usuarios":3}],
               "permisos_disponibles":[{"pm_cod":"ventas.ver","pm_des":"Ver ventas"}]}"""
        )

        val result = repository.getRoles()

        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/roles.php", request.path)
        assertEquals(
            AdminResult.Success(
                RolesAdmin(
                    roles = listOf(RolAdmin(2, "Vendedor", listOf("ventas.ver"), 3)),
                    permisosDisponibles = listOf(PermisoDisponible("ventas.ver", "Ver ventas"))
                )
            ),
            result
        )
    }

    @Test
    fun `crearRol posts the trimmed name and the permissions without an id`() = runTest {
        responde(201, """{"success":true,"ro_ide":4}""")

        repository.crearRol(" Cobrador ", listOf("cobros.ver", "cobros.cargar"))

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/roles.php", request.path)
        assertEquals(
            JsonParser.parseString("""{"nombre":"Cobrador","permisos":["cobros.ver","cobros.cargar"]}"""),
            request.json()
        )
    }

    @Test
    fun `actualizarRol puts the id, the name and the permissions`() = runTest {
        ok()

        repository.actualizarRol(4, "Cobrador", listOf("cobros.ver"))

        val request = server.takeRequest()
        assertEquals("PUT", request.method)
        assertEquals("/roles.php", request.path)
        assertEquals(JsonParser.parseString("""{"id":4,"nombre":"Cobrador","permisos":["cobros.ver"]}"""), request.json())
    }

    @Test
    fun `eliminarRol deletes by id in the query`() = runTest {
        ok()

        val result = repository.eliminarRol(4)

        val request = server.takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/roles.php?id=4", request.path)
        assertEquals(AdminResult.Success(Unit), result)
    }

    @Test
    fun `backend error messages are shown to the user`() = runTest {
        responde(409, """{"success":false,"error":"El rol tiene usuarios asignados"}""")
        assertEquals(
            AdminResult.Error(R.string.error_format, arrayOf("El rol tiene usuarios asignados")),
            repository.eliminarRol(4)
        )

        responde(400, """{"success":false,"error":"Contrasena demasiado corta"}""")
        assertEquals(
            AdminResult.Error(R.string.error_format, arrayOf("Contrasena demasiado corta")),
            repository.crearUsuario("juan", "123", 2)
        )

        responde(404, """{"success":false,"error":"Usuario no encontrado"}""")
        assertEquals(
            AdminResult.Error(R.string.error_format, arrayOf("Usuario no encontrado")),
            repository.cambiarRol(99, 2)
        )
    }

    @Test
    fun `403 is the missing permission message`() = runTest {
        responde(403, """{"success":false,"error":"Forbidden: missing permission","permiso":"admin"}""")

        assertEquals(AdminResult.Error(R.string.error_sin_permiso), repository.getUsuarios())
    }

    @Test
    fun `a server error without message keeps the generic message`() = runTest {
        responde(500, "")

        assertEquals(AdminResult.Error(R.string.error_servidor_format, arrayOf(500)), repository.getRoles())
    }

    @Test
    fun `a network failure is the network message`() = runTest {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))

        assertEquals(AdminResult.Error(R.string.error_red_verificar), repository.getUsuarios())
    }
}
