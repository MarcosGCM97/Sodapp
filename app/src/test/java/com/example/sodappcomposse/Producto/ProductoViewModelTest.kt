package com.example.sodappcomposse.Producto

import com.example.sodappcomposse.API.PostResponse
import com.example.sodappcomposse.Envases.EnvaseRepository
import com.example.sodappcomposse.Envases.EnvaseResponse
import com.example.sodappcomposse.R
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProductoViewModelTest {

    private lateinit var productoRepository: ProductoRepository
    private lateinit var envaseRepository: EnvaseRepository
    private lateinit var viewModel: ProductoViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val bidon = Producto(id = 1, nombrePr = "Bidon 20L", precioUni = 4500.0, stock = 3, retornable = 1, vacios = 5)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        productoRepository = mockk(relaxed = true)
        envaseRepository = mockk()
        coEvery { productoRepository.updateProducto(any(), any(), any(), any(), any()) } returns ProductoResult.Success(PostResponse(true, "ok"))
        coEvery { productoRepository.postProducto(any()) } returns ProductoResult.Success(PostResponse(true, "ok"))
        coEvery { productoRepository.getProductos(any()) } returns ProductoResult.Success(ProductoResponse(true, listOf(bidon)))
        viewModel = ProductoViewModel(productoRepository, envaseRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `adjusting stock keeps sending only name, price and quantity`() = runTest {
        viewModel.ajustarStock(bidon, 9)
        advanceUntilIdle()

        coVerify(exactly = 1) { productoRepository.updateProducto("Bidon 20L", 4500.0, 9, null, null) }
    }

    @Test
    fun `adjusting empty containers sends the new empties and keeps the full stock`() = runTest {
        viewModel.ajustarVacios(bidon, 8)
        advanceUntilIdle()

        coVerify(exactly = 1) { productoRepository.updateProducto("Bidon 20L", 4500.0, 3, null, 8) }
    }

    @Test
    fun `editing a product without container fields keeps the previous request`() = runTest {
        viewModel.editarProducto(Producto(nombrePr = "Soda", precioUni = 900.0, stock = 7))
        advanceUntilIdle()

        coVerify(exactly = 1) { productoRepository.updateProducto("Soda", 900.0, 7, null, null) }
    }

    @Test
    fun `editing a product with container fields sends retornable and vacios`() = runTest {
        viewModel.editarProducto(Producto(nombrePr = "Bidon 20L", precioUni = 4500.0, stock = 3), retornable = true, vacios = 4)
        advanceUntilIdle()

        coVerify(exactly = 1) { productoRepository.updateProducto("Bidon 20L", 4500.0, 3, true, 4) }
    }

    @Test
    fun `creating a product sends retornable and vacios`() = runTest {
        viewModel.agregarNuevoProducto("Bidon 20L", "4500", 3, retornable = true, vacios = 2)
        advanceUntilIdle()

        coVerify(exactly = 1) { productoRepository.postProducto(ProductoRequest("Bidon 20L", 4500.0, 3, true, 2)) }
    }

    @Test
    fun `creating a product without container fields sends it as non returnable`() = runTest {
        viewModel.agregarNuevoProducto("Soda", "900", 7)
        advanceUntilIdle()

        coVerify(exactly = 1) { productoRepository.postProducto(ProductoRequest("Soda", 900.0, 7, false, 0)) }
    }

    @Test
    fun `refilling calls the refill endpoint and reloads the products`() = runTest {
        coEvery { envaseRepository.llenarEnvases("Bidon 20L", 3) } returns ProductoResult.Success(EnvaseResponse(success = true))

        viewModel.llenarEnvases(bidon, 3)
        advanceUntilIdle()

        coVerify(exactly = 1) { envaseRepository.llenarEnvases("Bidon 20L", 3) }
        verify(atLeast = 1) { productoRepository.clearCache() }
        coVerify(atLeast = 1) { productoRepository.getProductos(any()) }
    }

    @Test
    fun `a rejected refill shows the server message`() = runTest {
        coEvery { envaseRepository.llenarEnvases("Bidon 20L", 30) } returns
            ProductoResult.Error(R.string.error_format, arrayOf("No hay suficientes envases vacíos"))

        viewModel.llenarEnvases(bidon, 30)
        advanceUntilIdle()

        assertEquals(
            ProductoUiState.Error(R.string.error_format, arrayOf("No hay suficientes envases vacíos")),
            viewModel.productoUiState
        )
    }

    @Test
    fun `a refill of zero or less is not sent`() = runTest {
        viewModel.llenarEnvases(bidon, 0)
        advanceUntilIdle()

        coVerify(exactly = 0) { envaseRepository.llenarEnvases(any(), any()) }
    }

    @Test
    fun `opening the history loads the movements of the product`() = runTest {
        val movimientos = listOf(Movimiento(id = 2, tipo = "venta", cantidad = -1, stock = 2), Movimiento(id = 1, tipo = "alta", cantidad = 3, stock = 3))
        coEvery { productoRepository.getMovimientos(1) } returns ProductoResult.Success(MovimientosResponse(true, movimientos))

        viewModel.verHistorial(bidon)
        advanceUntilIdle()

        assertEquals(MovimientosUiState.Success("Bidon 20L", movimientos), viewModel.movimientosUiState)
    }

    @Test
    fun `a failed history load keeps the product list untouched`() = runTest {
        coEvery { productoRepository.getMovimientos(1) } returns ProductoResult.Error(R.string.error_red_verificar)
        viewModel.getProductos()
        advanceUntilIdle()
        val estadoProductos = viewModel.productoUiState

        viewModel.verHistorial(bidon)
        advanceUntilIdle()

        assertEquals(MovimientosUiState.Error("Bidon 20L", R.string.error_red_verificar), viewModel.movimientosUiState)
        assertEquals(estadoProductos, viewModel.productoUiState)
    }

    @Test
    fun `closing the history clears it`() = runTest {
        coEvery { productoRepository.getMovimientos(1) } returns ProductoResult.Success(MovimientosResponse(true, emptyList()))
        viewModel.verHistorial(bidon)
        advanceUntilIdle()

        viewModel.cerrarHistorial()

        assertEquals(MovimientosUiState.Idle, viewModel.movimientosUiState)
    }
}
