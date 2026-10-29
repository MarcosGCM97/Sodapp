package com.example.sodappcomposse.Ventas

import com.example.sodappcomposse.API.PostResponse
import com.example.sodappcomposse.Cliente.ClienteRepository
import com.example.sodappcomposse.Producto.ProductoRepository
import com.example.sodappcomposse.Producto.ProductoVenta
import com.example.sodappcomposse.Producto.conDevueltos
import com.example.sodappcomposse.R
import com.example.sodappcomposse.UserPreferencesRepository
import com.google.gson.Gson
import com.example.sodappcomposse.Cliente.ClienteResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VentasViewModelTest {

    private lateinit var ventaRepository: VentaRepository
    private lateinit var clienteRepository: ClienteRepository
    private lateinit var productoRepository: ProductoRepository
    private lateinit var viewModel: VentasViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        ventaRepository = mockk()
        clienteRepository = mockk(relaxed = true)
        productoRepository = mockk(relaxed = true)
        val userPreferences: UserPreferencesRepository = mockk()
        every { userPreferences.userId } returns flowOf("7")
        viewModel = VentasViewModel(
            ventaRepository,
            clienteRepository,
            productoRepository,
            userPreferences
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `a rejected sale reports failure and keeps the error state`() = runTest {
        coEvery { ventaRepository.postVenta(any()) } returns VentaResult.Error(R.string.venta_procesada_success)

        val saved = viewModel.postVenta(clienteId = 1, productos = emptyList())

        assertFalse(saved)
        assertTrue(viewModel.ventasUiState is VentasUiState.Error)
        verify(exactly = 0) { clienteRepository.clearCache() }
        verify(exactly = 0) { productoRepository.clearCache() }
    }

    @Test
    fun `an accepted sale reports success`() = runTest {
        coEvery { ventaRepository.postVenta(any()) } returns VentaResult.Success(PostResponse(true, "ok"))

        val saved = viewModel.postVenta(clienteId = 1, productos = emptyList())

        assertTrue(saved)
        assertEquals(VentasUiState.Success(R.string.venta_procesada_success), viewModel.ventasUiState)
    }

    @Test
    fun `each sold product carries its returned containers to the API`() = runTest {
        val enviada = slot<VentaRequest>()
        coEvery { ventaRepository.postVenta(capture(enviada)) } returns VentaResult.Success(PostResponse(true, "ok"))

        viewModel.postVenta(
            clienteId = 1,
            productos = listOf(
                ProductoVenta(nombre = "Bidon 20L", cantidad = 3, precio = 4500.0, devueltos = 2),
                ProductoVenta(nombre = "Soda", cantidad = 1, precio = 900.0)
            )
        )

        assertEquals(listOf(2, 0), enviada.captured.productos.map { it.devueltos })
    }

    @Test
    fun `the sale body sends devueltos per item and 0 for non returnable products`() {
        val json = Gson().toJson(
            VentaRequest(
                1,
                listOf(
                    ProductoVenta(nombre = "Bidon 20L", cantidad = 3, precio = 4500.0, devueltos = 2),
                    ProductoVenta(nombre = "Soda", cantidad = 1, precio = 900.0)
                ),
                "7"
            )
        )

        assertEquals(
            """{"clienteId":1,"productos":[{"nombre":"Bidon 20L","cantidad":3,"precio":4500.0,"devueltos":2},""" +
                """{"nombre":"Soda","cantidad":1,"precio":900.0,"devueltos":0}],"usuarioId":"7"}""",
            json
        )
    }

    @Test
    fun `returned containers never go below zero`() {
        val item = ProductoVenta(nombre = "Bidon 20L", cantidad = 3, precio = 4500.0)

        assertEquals(2, item.conDevueltos(2).devueltos)
        assertEquals(0, item.conDevueltos(-1).devueltos)
    }

    @Test
    fun `a deleted sale still updates the debt and drops the cached product list`() = runTest {
        coEvery { ventaRepository.deleteVenta(10) } returns VentaResult.Success(PostResponse(true, "ok"))
        coEvery { clienteRepository.updateDeudaCliente(1, 4500.0) } returns ClienteResult.Success(PostResponse(true, "ok"))
        coEvery { ventaRepository.getVentasByClienteId(1, "7") } returns VentaResult.Success(VentaApiResponseById(true, emptyList()))

        viewModel.eliminarVenta(idVenta = 10, clienteId = 1, valorVenta = 4500.0)
        advanceUntilIdle()

        coVerify(exactly = 1) { clienteRepository.updateDeudaCliente(1, 4500.0) }
        verify(exactly = 1) { productoRepository.clearCache() }
    }

    @Test
    fun `an accepted sale drops the cached product list so stock is reloaded`() = runTest {
        coEvery { ventaRepository.postVenta(any()) } returns VentaResult.Success(PostResponse(true, "ok"))

        viewModel.postVenta(clienteId = 1, productos = emptyList())

        verify(exactly = 1) { productoRepository.clearCache() }
    }
}
