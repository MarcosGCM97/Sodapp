package com.example.sodappcomposse.Caja

import com.example.sodappcomposse.API.ApiServices
import com.example.sodappcomposse.R
import com.example.sodappcomposse.Ventas.Venta
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.IOException

class CajaRepositoryImplTest {

    private lateinit var apiServices: ApiServices
    private lateinit var repository: CajaRepositoryImpl

    @Before
    fun setup() {
        apiServices = mockk()
        repository = CajaRepositoryImpl(apiServices)
    }

    @Test
    fun `getCajaPorMes returns Success when API returns valid data`() = runTest {
        // Arrange
        val responseData = DataCajaResponse(
            success = true,
            caja = listOf(mockk<Venta>())
        )
        coEvery { apiServices.getCajaPorMes(1) } returns Response.success(responseData)

        // Act
        val result = repository.getCajaPorMes(1)

        // Assert
        assertTrue(result is CajaResult.Success)
        assertEquals(responseData, (result as CajaResult.Success).data)
    }

    @Test
    fun `getCajaPorMes returns Error when API success is false`() = runTest {
        // Arrange
        val responseData = DataCajaResponse(success = false, caja = emptyList())
        coEvery { apiServices.getCajaPorMes(1) } returns Response.success(responseData)

        // Act
        val result = repository.getCajaPorMes(1)

        // Assert
        assertTrue(result is CajaResult.Error)
        assertEquals(R.string.no_ventas_mes_error, (result as CajaResult.Error).messageRes)
    }

    @Test
    fun `getCajaPorMes returns Error when API returns null body`() = runTest {
        // Arrange
        coEvery { apiServices.getCajaPorMes(1) } returns Response.success(null)

        // Act
        val result = repository.getCajaPorMes(1)

        // Assert
        assertTrue(result is CajaResult.Error)
        assertEquals(R.string.cuerpo_nulo_error, (result as CajaResult.Error).messageRes)
    }

    @Test
    fun `getCajaPorMes returns Error when API returns failure code`() = runTest {
        // Arrange
        coEvery { apiServices.getCajaPorMes(1) } returns Response.error(404, "".toResponseBody())

        // Act
        val result = repository.getCajaPorMes(1)

        // Assert
        assertTrue(result is CajaResult.Error)
        assertEquals(R.string.error_api_format, (result as CajaResult.Error).messageRes)
        assertEquals(404, result.args[0])
    }

    @Test
    fun `getCajaPorMes returns Error when API throws IOException`() = runTest {
        // Arrange
        coEvery { apiServices.getCajaPorMes(1) } throws IOException("Network error")

        // Act
        val result = repository.getCajaPorMes(1)

        // Assert
        assertTrue(result is CajaResult.Error)
        assertEquals(R.string.error_red_format, (result as CajaResult.Error).messageRes)
        assertEquals("Network error", result.args[0])
    }
}
