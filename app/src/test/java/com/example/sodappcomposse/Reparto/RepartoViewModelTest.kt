package com.example.sodappcomposse.Reparto

import com.example.sodappcomposse.Cliente.ClienteRepository
import com.example.sodappcomposse.Producto.ProductoRepository
import com.example.sodappcomposse.Producto.ProductoResult
import com.example.sodappcomposse.Producto.ProductoUiState
import com.example.sodappcomposse.R
import io.mockk.coEvery
import io.mockk.coVerify
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class RepartoViewModelTest {

    private lateinit var repartoRepository: RepartoRepository
    private lateinit var viewModel: RepartoViewModel

    private val lunes = LocalDate.of(2026, 11, 2)
    private val ok = ProductoResult.Success(RepartoResponse(success = true))

    private fun parada(id: Int, orden: Int, estado: String = ParadaReparto.ESTADO_PENDIENTE) =
        ParadaReparto(id = id, orden = orden, estado = estado, clienteId = id + 100, nombreCliente = "Cliente $id", direccion = "Calle $id")

    private val reparto = Reparto(id = 7, fecha = "2026-11-02", dia = 1, paradas = listOf(parada(40, 1), parada(41, 2), parada(42, 3)))

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        repartoRepository = mockk()
        viewModel = RepartoViewModel(repartoRepository, mockk<ClienteRepository>(), mockk<ProductoRepository>())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private suspend fun cargarReparto() {
        coEvery { repartoRepository.getReparto("2026-11-02") } returns ProductoResult.Success(reparto)
        viewModel.cargar(lunes)
    }

    @Test
    fun `cargar shows the reparto of the date with its stops in order`() = runTest {
        cargarReparto()
        advanceUntilIdle()

        assertEquals(lunes, viewModel.fecha.value)
        assertEquals(reparto, viewModel.reparto.value)
        assertFalse(viewModel.cargando.value)
    }

    @Test
    fun `a date without reparto shows none and no error`() = runTest {
        coEvery { repartoRepository.getReparto(any()) } returns ProductoResult.Success(null)

        viewModel.cargar(lunes)
        advanceUntilIdle()

        assertNull(viewModel.reparto.value)
        assertFalse(viewModel.cargando.value)
        assertEquals(ProductoUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `cambiarDia loads the next date`() = runTest {
        cargarReparto()
        advanceUntilIdle()
        coEvery { repartoRepository.getReparto("2026-11-03") } returns ProductoResult.Success(null)

        viewModel.cambiarDia(1)
        advanceUntilIdle()

        assertEquals(lunes.plusDays(1), viewModel.fecha.value)
        assertNull(viewModel.reparto.value)
    }

    @Test
    fun `crear sends the date and origin and shows the created reparto`() = runTest {
        coEvery { repartoRepository.getReparto(any()) } returns ProductoResult.Success(null)
        viewModel.cargar(lunes)
        advanceUntilIdle()
        coEvery { repartoRepository.crearReparto("2026-11-02", OrigenReparto.DIAS) } returns
            ProductoResult.Success(RepartoResponse(success = true, reparto = reparto))

        viewModel.crear(OrigenReparto.DIAS)
        advanceUntilIdle()

        assertEquals(reparto, viewModel.reparto.value)
        assertEquals(ProductoUiState.Success(R.string.reparto_creado), viewModel.uiState.value)
    }

    @Test
    fun `crear error from the server is shown and nothing changes`() = runTest {
        coEvery { repartoRepository.getReparto(any()) } returns ProductoResult.Success(null)
        viewModel.cargar(lunes)
        advanceUntilIdle()
        val error = ProductoResult.Error(R.string.error_format, arrayOf("No hay un reparto anterior de ese día"))
        coEvery { repartoRepository.crearReparto(any(), any()) } returns error

        viewModel.crear(OrigenReparto.ANTERIOR)
        advanceUntilIdle()

        assertNull(viewModel.reparto.value)
        assertEquals(ProductoUiState.Error(R.string.error_format, arrayOf("No hay un reparto anterior de ese día")), viewModel.uiState.value)
    }

    @Test
    fun `mover sends every stop in the new order and shows it`() = runTest {
        cargarReparto()
        advanceUntilIdle()
        coEvery { repartoRepository.ordenarParadas(7, listOf(40, 42, 41)) } returns ok

        viewModel.mover(42, -1)
        advanceUntilIdle()

        assertEquals(listOf(40, 42, 41), viewModel.reparto.value!!.paradas.map { it.id })
        coVerify(exactly = 1) { repartoRepository.ordenarParadas(7, listOf(40, 42, 41)) }
    }

    @Test
    fun `mover past the ends does nothing`() = runTest {
        cargarReparto()
        advanceUntilIdle()

        viewModel.mover(40, -1)
        viewModel.mover(42, 1)
        advanceUntilIdle()

        assertEquals(listOf(40, 41, 42), viewModel.reparto.value!!.paradas.map { it.id })
        coVerify(exactly = 0) { repartoRepository.ordenarParadas(any(), any()) }
    }

    @Test
    fun `failed reorder reloads the saved order`() = runTest {
        cargarReparto()
        advanceUntilIdle()
        coEvery { repartoRepository.ordenarParadas(any(), any()) } returns ProductoResult.Error(R.string.error_red_verificar)

        viewModel.mover(41, 1)
        advanceUntilIdle()

        assertEquals(listOf(40, 41, 42), viewModel.reparto.value!!.paradas.map { it.id })
        assertEquals(ProductoUiState.Error(R.string.error_red_verificar), viewModel.uiState.value)
    }

    @Test
    fun `agregarParada adds the client and reloads`() = runTest {
        cargarReparto()
        advanceUntilIdle()
        coEvery { repartoRepository.agregarParada(7, 150) } returns ok

        viewModel.agregarParada(150)
        advanceUntilIdle()

        coVerify(exactly = 1) { repartoRepository.agregarParada(7, 150) }
        coVerify(exactly = 2) { repartoRepository.getReparto("2026-11-02") }
        assertEquals(ProductoUiState.Success(R.string.parada_agregada), viewModel.uiState.value)
    }

    @Test
    fun `quitarParada removes the stop and reloads`() = runTest {
        cargarReparto()
        advanceUntilIdle()
        coEvery { repartoRepository.quitarParada(41) } returns ok

        viewModel.quitarParada(41)
        advanceUntilIdle()

        coVerify(exactly = 1) { repartoRepository.quitarParada(41) }
        coVerify(exactly = 2) { repartoRepository.getReparto("2026-11-02") }
        assertEquals(ProductoUiState.Success(R.string.parada_quitada), viewModel.uiState.value)
    }

    @Test
    fun `guardarLineas sends only the products with quantity`() = runTest {
        cargarReparto()
        advanceUntilIdle()
        val conCantidad = LineaRepartoRequest(productoId = 3, cantidad = 2, precio = 450.0)
        coEvery { repartoRepository.guardarLineas(40, listOf(conCantidad)) } returns ok

        viewModel.guardarLineas(40, listOf(conCantidad, LineaRepartoRequest(productoId = 4, cantidad = 0)))
        advanceUntilIdle()

        coVerify(exactly = 1) { repartoRepository.guardarLineas(40, listOf(conCantidad)) }
        assertEquals(ProductoUiState.Success(R.string.lineas_reparto_guardadas), viewModel.uiState.value)
    }

    @Test
    fun `guardarLineas rejects negative quantities without calling the server`() = runTest {
        cargarReparto()
        advanceUntilIdle()

        viewModel.guardarLineas(40, listOf(LineaRepartoRequest(productoId = 3, cantidad = -1)))
        advanceUntilIdle()

        coVerify(exactly = 0) { repartoRepository.guardarLineas(any(), any()) }
        assertEquals(ProductoUiState.Error(R.string.error_cantidad_negativa), viewModel.uiState.value)
    }

    @Test
    fun `guardarLineas rejects a price of zero without calling the server`() = runTest {
        cargarReparto()
        advanceUntilIdle()

        viewModel.guardarLineas(40, listOf(LineaRepartoRequest(productoId = 3, cantidad = 2, precio = 0.0)))
        advanceUntilIdle()

        coVerify(exactly = 0) { repartoRepository.guardarLineas(any(), any()) }
        assertEquals(ProductoUiState.Error(R.string.error_precio_invalido), viewModel.uiState.value)
    }

    @Test
    fun `entregar sends only delivered lines and offers to collect the delivered amount`() = runTest {
        cargarReparto()
        advanceUntilIdle()
        val entregada = LineaRepartoRequest(productoId = 3, cantidad = 2, devueltos = 1)
        coEvery { repartoRepository.entregarParada(40, listOf(entregada)) } returns
            ProductoResult.Success(RepartoResponse(success = true, monto = 900.0, ventas = listOf(81)))

        viewModel.entregar(reparto.paradas[0], listOf(entregada, LineaRepartoRequest(productoId = 4, cantidad = 0, devueltos = 0)))
        advanceUntilIdle()

        coVerify(exactly = 1) { repartoRepository.entregarParada(40, listOf(entregada)) }
        coVerify(exactly = 2) { repartoRepository.getReparto("2026-11-02") }
        assertEquals(CobroEntrega(clienteId = 140, nombreCliente = "Cliente 40", monto = 900.0), viewModel.cobroPendiente.value)
        assertEquals(ProductoUiState.Success(R.string.parada_entregada), viewModel.uiState.value)
    }

    @Test
    fun `entregar with nothing delivered is rejected without calling the server`() = runTest {
        cargarReparto()
        advanceUntilIdle()

        viewModel.entregar(reparto.paradas[0], listOf(LineaRepartoRequest(productoId = 3, cantidad = 0, devueltos = 0)))
        advanceUntilIdle()

        coVerify(exactly = 0) { repartoRepository.entregarParada(any(), any()) }
        assertEquals(ProductoUiState.Error(R.string.error_entrega_vacia), viewModel.uiState.value)
    }

    @Test
    fun `entregar rejects returned containers without a delivered quantity`() = runTest {
        cargarReparto()
        advanceUntilIdle()

        viewModel.entregar(reparto.paradas[0], listOf(
            LineaRepartoRequest(productoId = 3, cantidad = 1),
            LineaRepartoRequest(productoId = 4, cantidad = 0, devueltos = 2)
        ))
        advanceUntilIdle()

        coVerify(exactly = 0) { repartoRepository.entregarParada(any(), any()) }
        assertEquals(ProductoUiState.Error(R.string.error_devueltos_sin_entrega), viewModel.uiState.value)
    }

    @Test
    fun `failed delivery shows the error and offers no collection`() = runTest {
        cargarReparto()
        advanceUntilIdle()
        coEvery { repartoRepository.entregarParada(any(), any()) } returns
            ProductoResult.Error(R.string.error_format, arrayOf("La parada no está pendiente"))

        viewModel.entregar(reparto.paradas[0], listOf(LineaRepartoRequest(productoId = 3, cantidad = 1)))
        advanceUntilIdle()

        assertNull(viewModel.cobroPendiente.value)
        assertEquals(ProductoUiState.Error(R.string.error_format, arrayOf("La parada no está pendiente")), viewModel.uiState.value)
    }

    @Test
    fun `cerrarCobro hides the collection offer`() = runTest {
        cargarReparto()
        advanceUntilIdle()
        coEvery { repartoRepository.entregarParada(any(), any()) } returns ProductoResult.Success(RepartoResponse(success = true, monto = 900.0))
        viewModel.entregar(reparto.paradas[0], listOf(LineaRepartoRequest(productoId = 3, cantidad = 1)))
        advanceUntilIdle()

        viewModel.cerrarCobro()

        assertNull(viewModel.cobroPendiente.value)
    }

    @Test
    fun `noEntregar and reabrir change the stop status and reload`() = runTest {
        cargarReparto()
        advanceUntilIdle()
        coEvery { repartoRepository.noEntregarParada(41) } returns ok
        coEvery { repartoRepository.reabrirParada(41) } returns ok

        viewModel.noEntregar(41)
        advanceUntilIdle()
        assertEquals(ProductoUiState.Success(R.string.parada_no_entregada), viewModel.uiState.value)
        viewModel.reabrir(41)
        advanceUntilIdle()

        coVerify(exactly = 1) { repartoRepository.noEntregarParada(41) }
        coVerify(exactly = 1) { repartoRepository.reabrirParada(41) }
        coVerify(exactly = 3) { repartoRepository.getReparto("2026-11-02") }
        assertEquals(ProductoUiState.Success(R.string.parada_reabierta), viewModel.uiState.value)
    }

    @Test
    fun `guardarPlantilla saves the shown reparto`() = runTest {
        cargarReparto()
        advanceUntilIdle()
        coEvery { repartoRepository.guardarPlantilla(7) } returns ok

        viewModel.guardarPlantilla()
        advanceUntilIdle()

        coVerify(exactly = 1) { repartoRepository.guardarPlantilla(7) }
        assertEquals(ProductoUiState.Success(R.string.plantilla_reparto_guardada), viewModel.uiState.value)
    }
}
