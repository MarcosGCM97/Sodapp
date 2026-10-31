package com.example.sodappcomposse.Producto

import com.example.sodappcomposse.API.ApiServices
import com.example.sodappcomposse.API.PostResponse
import com.google.gson.Gson
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class ProductoRepositoryImplTest {

    private lateinit var apiServices: ApiServices
    private lateinit var repository: ProductoRepositoryImpl

    @Before
    fun setup() {
        apiServices = mockk()
        repository = ProductoRepositoryImpl(apiServices)
        coEvery { apiServices.updateProducto(any(), any(), any(), any(), any()) } returns Response.success(PostResponse(true, "ok"))
    }

    @Test
    fun `an edit without container fields omits retornable and vacios`() = runTest {
        repository.updateProducto("Soda", 900.0, 7)

        coVerify(exactly = 1) { apiServices.updateProducto("Soda", 900.0, 7, null, null) }
    }

    @Test
    fun `an edit with container fields sends retornable as 0 or 1 and vacios`() = runTest {
        repository.updateProducto("Bidon 20L", 4500.0, 3, retornable = true, vacios = 4)
        repository.updateProducto("Bidon 12L", 3000.0, 2, retornable = false, vacios = 0)

        coVerify(exactly = 1) { apiServices.updateProducto("Bidon 20L", 4500.0, 3, 1, 4) }
        coVerify(exactly = 1) { apiServices.updateProducto("Bidon 12L", 3000.0, 2, 0, 0) }
    }

    @Test
    fun `a new product request carries retornable and vacios`() {
        val json = Gson().toJson(ProductoRequest(nombrePr = "Bidon 20L", precioUni = 4500.0, stock = 3, retornable = true, vacios = 2))

        assertEquals(
            """{"nombrePr":"Bidon 20L","precioUni":4500.0,"stock":3,"retornable":true,"vacios":2}""",
            json
        )
    }

    @Test
    fun `a new product request defaults to non returnable with no empties`() {
        val json = Gson().toJson(ProductoRequest(nombrePr = "Soda", precioUni = 900.0, stock = 7))

        assertEquals(
            """{"nombrePr":"Soda","precioUni":900.0,"stock":7,"retornable":false,"vacios":0}""",
            json
        )
    }
}
