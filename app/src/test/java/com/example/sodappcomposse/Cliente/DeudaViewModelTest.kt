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
        coEvery { repository.updateDeudaCliente(7, any(), any()) } coAnswers {
            delay(1000)
            ClienteResult.Success(PostResponse(true, "ok"))
        }

        viewModel.pagarDeudaCliente(7, 500.0, 2)
        viewModel.pagarDeudaCliente(7, 300.0, 2)
        advanceUntilIdle()

        coVerify(exactly = 1) { repository.updateDeudaCliente(7, any(), any()) }
        coVerify(exactly = 1) { repository.updateDeudaCliente(7, 500.0, 2) }
        assertEquals(DeudaUiState.Success(R.string.venta_procesada_success), viewModel.uiState.value)
    }

    @Test
    fun `pagarDeudaCliente ignores new payments until the screen resets after a success`() = runTest {
        coEvery { repository.updateDeudaCliente(7, any(), any()) } returns ClienteResult.Success(PostResponse(true, "ok"))

        viewModel.pagarDeudaCliente(7, 500.0, 2)
        advanceUntilIdle()
        viewModel.pagarDeudaCliente(7, 300.0, 2)
        advanceUntilIdle()
        coVerify(exactly = 1) { repository.updateDeudaCliente(7, any(), any()) }

        viewModel.resetState()
        viewModel.pagarDeudaCliente(7, 300.0, 2)
        advanceUntilIdle()
        coVerify(exactly = 1) { repository.updateDeudaCliente(7, 300.0, 2) }
    }

    @Test
    fun `pagarDeudaCliente can pay again after the previous payment failed`() = runTest {
        coEvery { repository.updateDeudaCliente(7, 500.0, 2) } returns ClienteResult.Error(R.string.cuerpo_nulo_error)

        viewModel.pagarDeudaCliente(7, 500.0, 2)
        advanceUntilIdle()
        assertEquals(DeudaUiState.Error(R.string.cuerpo_nulo_error), viewModel.uiState.value)

        viewModel.pagarDeudaCliente(7, 500.0, 2)
        advanceUntilIdle()

        coVerify(exactly = 2) { repository.updateDeudaCliente(7, 500.0, 2) }
    }
    @Test
    fun `pagarDeudaCliente sends the chosen payment method`() = runTest {
        coEvery { repository.updateDeudaCliente(7, 500.0, 3) } returns ClienteResult.Success(PostResponse(true, "ok"))

        viewModel.pagarDeudaCliente(7, 500.0, 3)
        advanceUntilIdle()

        coVerify(exactly = 1) { repository.updateDeudaCliente(7, 500.0, 3) }
    }

    @Test
    fun `cargarFormasPago exposes the selectable payment methods`() = runTest {
        val formas = listOf(FormaPago(2, "Efectivo"), FormaPago(3, "Transferencia"))
        coEvery { repository.getFormasPago() } returns ClienteResult.Success(FormasPagoResponse(true, formas))

        viewModel.cargarFormasPago()
        advanceUntilIdle()

        assertEquals(formas, viewModel.formasPago.value)
    }

    @Test
    fun `cargarFormasPago failure leaves no methods and reports the error`() = runTest {
        coEvery { repository.getFormasPago() } returns ClienteResult.Error(R.string.error_red_verificar)

        viewModel.cargarFormasPago()
        advanceUntilIdle()

        assertEquals(emptyList<FormaPago>(), viewModel.formasPago.value)
        assertEquals(DeudaUiState.Error(R.string.error_red_verificar), viewModel.uiState.value)
    }
    @Test
    fun `cargarPagos exposes the client's payment history`() = runTest {
        val pagos = listOf(Pago(9, 300.5, "Efectivo", "2026-10-30 10:00:00"))
        coEvery { repository.getPagosCliente(7) } returns ClienteResult.Success(PagosResponse(true, pagos))

        viewModel.cargarPagos(7)
        advanceUntilIdle()

        assertEquals(pagos, viewModel.pagos.value)
    }

    @Test
    fun `cargarPagos failure leaves no payments and reports the error`() = runTest {
        coEvery { repository.getPagosCliente(7) } returns ClienteResult.Error(R.string.error_red_verificar)

        viewModel.cargarPagos(7)
        advanceUntilIdle()

        assertEquals(emptyList<Pago>(), viewModel.pagos.value)
        assertEquals(DeudaUiState.Error(R.string.error_red_verificar), viewModel.uiState.value)
    }
    @Test
    fun `cargarDeudores lists the debtors with the highest balance first`() = runTest {
        val ana = Deudor(1, "Ana", 1200.0)
        val juan = Deudor(2, "Juan", 4500.0)
        coEvery { repository.getDeudores() } returns ClienteResult.Success(DeudoresResponse(true, listOf(ana, juan)))

        viewModel.cargarDeudores()
        advanceUntilIdle()

        assertEquals(listOf(juan, ana), viewModel.deudores.value)
    }

    @Test
    fun `cargarDeudores failure leaves no debtors and reports the error`() = runTest {
        coEvery { repository.getDeudores() } returns ClienteResult.Error(R.string.error_red_verificar)

        viewModel.cargarDeudores()
        advanceUntilIdle()

        assertEquals(emptyList<Deudor>(), viewModel.deudores.value)
        assertEquals(DeudaUiState.Error(R.string.error_red_verificar), viewModel.uiState.value)
    }
}
