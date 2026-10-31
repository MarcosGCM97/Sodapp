package com.example.sodappcomposse.Envases

import com.example.sodappcomposse.Producto.ProductoResult
import com.example.sodappcomposse.Producto.ProductoUiState
import com.example.sodappcomposse.R
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
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
class EnvasesClienteViewModelTest {

    private lateinit var repository: EnvaseRepository
    private lateinit var viewModel: EnvasesClienteViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val saldo = listOf(EnvaseCliente(1, "Bidon 20L", 4), EnvaseCliente(2, "Bidon 12L", 0))

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        coEvery { repository.getEnvasesCliente(25) } returns ProductoResult.Success(EnvasesClienteResponse(true, saldo))
        viewModel = EnvasesClienteViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loading shows the client balance per returnable product`() = runTest {
        viewModel.cargarEnvases(25)
        advanceUntilIdle()

        assertEquals(saldo, viewModel.envases.value)
    }

    @Test
    fun `loading again refetches the balance instead of reusing it`() = runTest {
        viewModel.cargarEnvases(25)
        advanceUntilIdle()
        viewModel.cargarEnvases(25)
        advanceUntilIdle()

        coVerify(exactly = 2) { repository.getEnvasesCliente(25) }
    }

    @Test
    fun `a failed load reports the error`() = runTest {
        coEvery { repository.getEnvasesCliente(25) } returns ProductoResult.Error(R.string.error_format, arrayOf("Cliente no encontrado"))

        viewModel.cargarEnvases(25)
        advanceUntilIdle()

        assertEquals(ProductoUiState.Error(R.string.error_format, arrayOf("Cliente no encontrado")), viewModel.uiState.value)
    }

    @Test
    fun `setting a balance saves it and reloads the list`() = runTest {
        coEvery { repository.setSaldoEnvases(25, 1, 6) } returns ProductoResult.Success(EnvaseResponse(success = true))

        viewModel.guardarSaldo(25, 1, 6)
        advanceUntilIdle()

        coVerifyOrder {
            repository.setSaldoEnvases(25, 1, 6)
            repository.getEnvasesCliente(25)
        }
        assertEquals(ProductoUiState.Success(R.string.saldo_envases_guardado), viewModel.uiState.value)
    }

    @Test
    fun `a negative balance is rejected without calling the API`() = runTest {
        viewModel.guardarSaldo(25, 1, -1)
        advanceUntilIdle()

        coVerify(exactly = 0) { repository.setSaldoEnvases(any(), any(), any()) }
        assertEquals(ProductoUiState.Error(R.string.error_cantidad_negativa), viewModel.uiState.value)
    }
}
