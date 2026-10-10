package com.example.sodappcomposse.Cliente

import com.example.sodappcomposse.API.PostResponse
import com.example.sodappcomposse.R
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AgendaViewModelTest {

    private lateinit var repository: AgendaRepository
    private lateinit var viewModel: AgendaViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val clienteA = 1
    private val clienteB = 2
    private val diasA = listOf("Lunes")
    private val diasB = listOf("Viernes")

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        viewModel = AgendaViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `a slower load for a previous client does not overwrite the current client days`() = runTest {
        coEvery { repository.getDiasEntregaById(clienteA) } coAnswers {
            delay(1000)
            AgendaResult.Success(DiasEntregaByid(true, diasA))
        }
        coEvery { repository.getDiasEntregaById(clienteB) } returns
            AgendaResult.Success(DiasEntregaByid(true, diasB))

        viewModel.getDiasEntregaById(clienteA)
        viewModel.getDiasEntregaById(clienteB)
        advanceUntilIdle()

        assertEquals(diasB, viewModel.diasEntregaById.value)
    }

    @Test
    fun `reload after saving a client does not overwrite a newer client days`() = runTest {
        coEvery { repository.updateDiasEntrega(any()) } coAnswers {
            delay(100)
            AgendaResult.Success(PostResponse(true, "ok"))
        }
        coEvery { repository.getDiasEntregaById(clienteA) } coAnswers {
            delay(1000)
            AgendaResult.Success(DiasEntregaByid(true, diasA))
        }
        coEvery { repository.getDiasEntregaById(clienteB) } returns
            AgendaResult.Success(DiasEntregaByid(true, diasB))

        viewModel.updateDiasEntrega(clienteA, diasA)
        // Save finished, the reload for A is now in flight
        advanceTimeBy(150)
        viewModel.getDiasEntregaById(clienteB)
        advanceUntilIdle()

        assertEquals(diasB, viewModel.diasEntregaById.value)
    }

    @Test
    fun `selecting another client while saving skips the reload of the saved client`() = runTest {
        coEvery { repository.updateDiasEntrega(any()) } coAnswers {
            delay(100)
            AgendaResult.Success(PostResponse(true, "ok"))
        }
        coEvery { repository.getDiasEntregaById(clienteA) } returns
            AgendaResult.Success(DiasEntregaByid(true, diasA))
        coEvery { repository.getDiasEntregaById(clienteB) } coAnswers {
            delay(10)
            AgendaResult.Success(DiasEntregaByid(true, diasB))
        }

        viewModel.getDiasEntregaById(clienteA)
        advanceUntilIdle()
        viewModel.updateDiasEntrega(clienteA, diasA)
        viewModel.getDiasEntregaById(clienteB)
        advanceUntilIdle()

        assertEquals(diasB, viewModel.diasEntregaById.value)
    }

    @Test
    fun `a failed reload after a successful save keeps the save success`() = runTest {
        coEvery { repository.updateDiasEntrega(any()) } returns
            AgendaResult.Success(PostResponse(true, "ok"))
        coEvery { repository.getDiasEntregaById(clienteA) } returns
            AgendaResult.Error(R.string.cuerpo_nulo_error)
        var recibido: AgendaResult<PostResponse>? = null

        viewModel.updateDiasEntrega(clienteA, diasA) { recibido = it }
        advanceUntilIdle()

        assertEquals(AgendaUiState.Success(R.string.venta_procesada_success), viewModel.uiState.value)
        assertTrue(recibido is AgendaResult.Success)
    }

    @Test
    fun `a failed save reports the error through state and callback`() = runTest {
        coEvery { repository.updateDiasEntrega(any()) } returns
            AgendaResult.Error(R.string.cuerpo_nulo_error)
        var recibido: AgendaResult<PostResponse>? = null

        viewModel.updateDiasEntrega(clienteA, diasA) { recibido = it }
        advanceUntilIdle()

        assertEquals(AgendaUiState.Error(R.string.cuerpo_nulo_error), viewModel.uiState.value)
        assertTrue(recibido is AgendaResult.Error)
    }
}
