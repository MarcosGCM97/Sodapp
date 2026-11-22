package com.example.sodappcomposse.Gastos

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
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class GastoRepositoryImplTest {

    private lateinit var server: MockWebServer
    private lateinit var repository: GastoRepositoryImpl

    @Before
    fun setup() {
        server = MockWebServer()
        server.start()
        val api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiServices::class.java)
        repository = GastoRepositoryImpl(api)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun responde(code: Int, body: String) {
        server.enqueue(MockResponse().setResponseCode(code).setBody(body))
    }

    private fun RecordedRequest.json() = JsonParser.parseString(body.readUtf8()).asJsonObject

    private val gastoJson = """{"gs_ide":4,"gs_cat":"compra_producto","gs_mon":900,"gs_fec":"2026-11-20","gs_des":null,
        "gs_rep":null,"gs_pro":3,"pr_nom":"Bidón 20L","gs_can":4,"gs_est":"lleno","gs_us":2,"us_nom":"Ariza"}"""

    private val gasto = Gasto(
        id = 4, categoria = "compra_producto", monto = 900.0, fecha = "2026-11-20", descripcion = null,
        repartoId = null, productoId = 3, productoNombre = "Bidón 20L", cantidad = 4, estado = "lleno",
        usuarioId = 2, usuarioNombre = "Ariza"
    )

    @Test
    fun `listar sends the period and category and maps the gastos with the total`() = runTest {
        responde(200, """{"success":true,"desde":"2026-11-01","hasta":"2026-11-30","gastos":[$gastoJson],"total":900}""")

        val result = repository.listar("2026-11-01", "2026-11-30", CategoriaGasto.COMPRA_PRODUCTO)

        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/gastos.php?desde=2026-11-01&hasta=2026-11-30&categoria=compra_producto", request.path)
        assertEquals(GastoResult.Success(GastosPeriodo("2026-11-01", "2026-11-30", listOf(gasto), 900.0)), result)
    }

    @Test
    fun `listar without category sends no categoria`() = runTest {
        responde(200, """{"success":true,"desde":"2026-11-01","hasta":"2026-11-30","gastos":[],"total":0}""")

        val result = repository.listar("2026-11-01", "2026-11-30", null)

        assertEquals("/gastos.php?desde=2026-11-01&hasta=2026-11-30", server.takeRequest().path)
        assertEquals(GastoResult.Success(GastosPeriodo("2026-11-01", "2026-11-30", emptyList(), 0.0)), result)
    }

    @Test
    fun `crear posts the fields and leaves out the empty ones`() = runTest {
        responde(201, """{"success":true,"gasto":$gastoJson}""")

        val result = repository.guardar(
            NuevoGasto(
                categoria = CategoriaGasto.COMPRA_PRODUCTO, monto = 900.0, fecha = "2026-11-20", descripcion = "  ",
                productoId = 3, cantidad = 4, estado = EstadoEnvase.LLENO
            )
        )

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/gastos.php", request.path)
        val body = request.json()
        assertEquals(
            JsonParser.parseString("""{"categoria":"compra_producto","monto":900.0,"fecha":"2026-11-20","productoId":3,"cantidad":4,"estado":"lleno"}"""),
            body
        )
        assertEquals(GastoResult.Success(gasto), result)
    }

    @Test
    fun `guardar with an id puts the replacement and drops the product fields of a non-stock category`() = runTest {
        responde(200, """{"success":true,"gasto":$gastoJson}""")

        repository.guardar(
            NuevoGasto(
                id = 4, categoria = CategoriaGasto.VEHICULO, monto = 15000.5, fecha = "2026-11-20", descripcion = " Pinchadura ",
                repartoId = 8, productoId = 3, cantidad = 4, estado = EstadoEnvase.VACIO
            )
        )

        val request = server.takeRequest()
        assertEquals("PUT", request.method)
        assertEquals(
            JsonParser.parseString("""{"id":4,"categoria":"vehiculo","monto":15000.5,"fecha":"2026-11-20","descripcion":"Pinchadura","repartoId":8}"""),
            request.json()
        )
    }

    @Test
    fun `eliminar deletes by id`() = runTest {
        responde(200, """{"success":true}""")

        val result = repository.eliminar(4)

        val request = server.takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/gastos.php?id=4", request.path)
        assertEquals(GastoResult.Success(Unit), result)
    }

    @Test
    fun `a 409 surfaces the backend message`() = runTest {
        responde(409, """{"success":false,"error":"No hay suficientes envases vacíos"}""")

        val result = repository.guardar(NuevoGasto(categoria = CategoriaGasto.RECARGA_ENVASES, monto = 1.0, fecha = "2026-11-20", productoId = 3, cantidad = 9))

        assertEquals(GastoResult.Error(R.string.error_format, arrayOf("No hay suficientes envases vacíos")), result)
    }

    @Test
    fun `a 403 is the no permission message`() = runTest {
        responde(403, """{"success":false,"error":"Forbidden: missing permission","permiso":"gastos.ver"}""")

        assertEquals(GastoResult.Error(R.string.error_sin_permiso), repository.listar("2026-11-01", "2026-11-30", null))
    }

    @Test
    fun `a network failure is the connection message`() = runTest {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))

        assertEquals(GastoResult.Error(R.string.error_red_verificar), repository.eliminar(4))
    }

    @Test
    fun `only purchases and refills carry a product`() {
        assertEquals(
            listOf(CategoriaGasto.COMPRA_PRODUCTO, CategoriaGasto.RECARGA_ENVASES),
            CategoriaGasto.entries.filter { it.llevaProducto }
        )
        assertEquals(CategoriaGasto.VEHICULO, CategoriaGasto.desdeCodigo("vehiculo"))
        assertFalse(CategoriaGasto.desdeCodigo("desconocida") != null)
    }
}
