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
class ClientesViewModelTest {

    private lateinit var repository: ClienteRepository
    private lateinit var viewModel: ClientesViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val cliente = Cliente(idCl = 7, nombreCl = "Ana", numTelCl = "123", direccionCl = "Calle 1")

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        coEvery { repository.getClientes(any()) } returns
            ClienteResult.Success(ClienteResponse(true, listOf(cliente)))
        viewModel = ClientesViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `editarCliente exposes Loading while the update is in flight`() = runTest {
        coEvery { repository.updateCliente(cliente) } coAnswers {
            delay(1000)
            ClienteResult.Success(PostResponse(true, "ok"))
        }

        viewModel.editarCliente(cliente)

        assertEquals(AddClienteUiState.Loading, viewModel.addClienteUiState.value)
        advanceUntilIdle()
        assertEquals(AddClienteUiState.Success(R.string.cliente_editado_success), viewModel.addClienteUiState.value)
    }

    @Test
    fun `editarCliente ignores duplicate submits while an update is in flight`() = runTest {
        coEvery { repository.updateCliente(cliente) } coAnswers {
            delay(1000)
            ClienteResult.Success(PostResponse(true, "ok"))
        }

        viewModel.editarCliente(cliente)
        viewModel.editarCliente(cliente)
        advanceUntilIdle()

        coVerify(exactly = 1) { repository.updateCliente(cliente) }
    }

    @Test
    fun `editarCliente can be retried after a failed update`() = runTest {
        coEvery { repository.updateCliente(cliente) } returns ClienteResult.Error(R.string.cuerpo_nulo_error)

        viewModel.editarCliente(cliente)
        advanceUntilIdle()
        assertEquals(AddClienteUiState.Error(R.string.cuerpo_nulo_error), viewModel.addClienteUiState.value)

        viewModel.editarCliente(cliente)
        advanceUntilIdle()

        coVerify(exactly = 2) { repository.updateCliente(cliente) }
    }
}
