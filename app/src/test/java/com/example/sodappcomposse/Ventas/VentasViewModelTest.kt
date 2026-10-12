package com.example.sodappcomposse.Ventas

import com.example.sodappcomposse.API.PostResponse
import com.example.sodappcomposse.Cliente.ClienteRepository
import com.example.sodappcomposse.Producto.ProductoRepository
import com.example.sodappcomposse.R
import com.example.sodappcomposse.UserPreferencesRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
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
    private lateinit var viewModel: VentasViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        ventaRepository = mockk()
        clienteRepository = mockk(relaxed = true)
        val userPreferences: UserPreferencesRepository = mockk()
        every { userPreferences.userId } returns flowOf("7")
        viewModel = VentasViewModel(
            ventaRepository,
            clienteRepository,
            mockk<ProductoRepository>(relaxed = true),
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
    }

    @Test
    fun `an accepted sale reports success`() = runTest {
        coEvery { ventaRepository.postVenta(any()) } returns VentaResult.Success(PostResponse(true, "ok"))

        val saved = viewModel.postVenta(clienteId = 1, productos = emptyList())

        assertTrue(saved)
        assertEquals(VentasUiState.Success(R.string.venta_procesada_success), viewModel.ventasUiState)
    }
}
