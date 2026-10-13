package com.example.sodappcomposse.Cliente

import com.example.sodappcomposse.API.PostResponse
import com.example.sodappcomposse.R
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
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
class DeudaViewModelTest {

    private lateinit var repository: ClienteRepository
    private lateinit var viewModel: DeudaViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        viewModel = DeudaViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `pagarDeudaCliente ignores new payments while one is in flight`() = runTest {
        coEvery { repository.updateDeudaCliente(7, any()) } coAnswers {
            delay(1000)
            ClienteResult.Success(PostResponse(true, "ok"))
        }

        viewModel.pagarDeudaCliente(7, 500.0)
        viewModel.pagarDeudaCliente(7, 300.0)
        advanceUntilIdle()

        coVerify(exactly = 1) { repository.updateDeudaCliente(7, any()) }
        coVerify(exactly = 1) { repository.updateDeudaCliente(7, 500.0) }
        assertEquals(DeudaUiState.Success(R.string.venta_procesada_success), viewModel.uiState.value)
    }

    @Test
    fun `pagarDeudaCliente can pay again after the previous payment failed`() = runTest {
        coEvery { repository.updateDeudaCliente(7, 500.0) } returns ClienteResult.Error(R.string.cuerpo_nulo_error)

        viewModel.pagarDeudaCliente(7, 500.0)
        advanceUntilIdle()
        assertEquals(DeudaUiState.Error(R.string.cuerpo_nulo_error), viewModel.uiState.value)

        viewModel.pagarDeudaCliente(7, 500.0)
        advanceUntilIdle()

        coVerify(exactly = 2) { repository.updateDeudaCliente(7, 500.0) }
    }
}
