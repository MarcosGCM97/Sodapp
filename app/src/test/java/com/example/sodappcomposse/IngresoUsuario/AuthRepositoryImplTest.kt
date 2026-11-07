package com.example.sodappcomposse.IngresoUsuario

import com.example.sodappcomposse.API.ApiServices
import com.example.sodappcomposse.API.SesionResponse
import com.example.sodappcomposse.R
import com.example.sodappcomposse.auth.SessionPermissions
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.IOException

class AuthRepositoryImplTest {

    private lateinit var apiServices: ApiServices
    private lateinit var repository: AuthRepositoryImpl

    @Before
    fun setup() {
        apiServices = mockk()
        repository = AuthRepositoryImpl(apiServices)
    }

    @Test
    fun `getSesion returns the permissions of the current user`() = runTest {
        coEvery { apiServices.getSesion() } returns Response.success(
            SesionResponse(success = true, idUs = 7, nombreUs = "juan", esDueno = false, permisos = listOf("ventas.ver"))
        )

        assertEquals(
            SesionResult.Success(SessionPermissions(esDueno = false, permisos = setOf("ventas.ver"))),
            repository.getSesion()
        )
    }

    @Test
    fun `getSesion maps 401 to Unauthorized`() = runTest {
        coEvery { apiServices.getSesion() } returns
            Response.error(401, """{"success":false,"error":"Invalid or expired token"}""".toResponseBody())

        assertEquals(SesionResult.Unauthorized, repository.getSesion())
    }

    @Test
    fun `getSesion maps other server errors to Error`() = runTest {
        coEvery { apiServices.getSesion() } returns Response.error(500, "".toResponseBody())

        assertEquals(SesionResult.Error(R.string.error_servidor_format, arrayOf(500)), repository.getSesion())
    }

    @Test
    fun `getSesion maps a network failure to Error`() = runTest {
        coEvery { apiServices.getSesion() } throws IOException("offline")

        assertEquals(SesionResult.Error(R.string.error_red_verificar), repository.getSesion())
    }
}
