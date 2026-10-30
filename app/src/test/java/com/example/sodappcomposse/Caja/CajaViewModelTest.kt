package com.example.sodappcomposse.Caja

import com.example.sodappcomposse.R
import com.example.sodappcomposse.Ventas.Venta
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CajaViewModelTest {

    private lateinit var repository: CajaRepository
    private lateinit var viewModel: CajaViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        coEvery { repository.getCobrosPorMes(any()) } returns CobrosResult.Success(emptyList())
        viewModel = CajaViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getCajaPorMes transitions to Success state when repository succeeds`() = runTest {
        // Arrange
        val responseData = DataCajaResponse(
            success = true,
            caja = listOf(Venta(producto = "Bidon", precio = 100.0, cantidad = 1, monto = 100.0))
        )
        val mes = Meses.ENERO
        viewModel.seleccionarMes(mes)
        coEvery { repository.getCajaPorMes(mes.numero) } returns CajaResult.Success(responseData)

        // Act
        viewModel.getCajaPorMes()
        assertEquals(CajaUiState.Loading, viewModel.cajaUiState)
        
        advanceUntilIdle()

        // Assert
        assertTrue(viewModel.cajaUiState is CajaUiState.Success)
        assertEquals(R.string.datos_cargados_success, (viewModel.cajaUiState as CajaUiState.Success).messageRes)
    }

    @Test
    fun `getCajaPorMes transitions to Error state when repository fails`() = runTest {
        // Arrange
        val mes = Meses.ENERO
        viewModel.seleccionarMes(mes)
        coEvery { repository.getCajaPorMes(mes.numero) } returns CajaResult.Error(R.string.no_ventas_mes_error)

        // Act
        viewModel.getCajaPorMes()
        advanceUntilIdle()

        // Assert
        assertTrue(viewModel.cajaUiState is CajaUiState.Error)
        assertEquals(R.string.no_ventas_mes_error, (viewModel.cajaUiState as CajaUiState.Error).messageRes)
    }

    @Test
    fun `getCajaPorMes exposes Empty state when month has no sales`() = runTest {
        val mes = Meses.FEBRERO
        viewModel.seleccionarMes(mes)
        coEvery { repository.getCajaPorMes(mes.numero) } returns
            CajaResult.Success(DataCajaResponse(success = true, caja = emptyList()))

        viewModel.getCajaPorMes()
        advanceUntilIdle()

        assertEquals(CajaUiState.Empty, viewModel.cajaUiState)
        assertEquals(mes, viewModel.mesCargado.value)
    }

    @Test
    fun `totals use stored sale amount instead of current price times quantity`() = runTest {
        val mes = Meses.MARZO
        viewModel.seleccionarMes(mes)
        val ventas = listOf(
            // Price changed after the sale: stored monto is the source of truth
            Venta(producto = "Bidon", precio = 200.0, cantidad = 2, monto = 150.0),
            Venta(producto = "Bidon", precio = 200.0, cantidad = 1, monto = 80.0),
            Venta(producto = "Soda", precio = 50.0, cantidad = 3, monto = 120.0)
        )
        coEvery { repository.getCajaPorMes(mes.numero) } returns
            CajaResult.Success(DataCajaResponse(success = true, caja = ventas))
        backgroundScope.launch { viewModel.cajaTotales.collect {} }

        viewModel.getCajaPorMes()
        advanceUntilIdle()

        val totales = viewModel.cajaTotales.value
        assertEquals(350.0, totales.montoTotal, 0.001)
        assertEquals(6, totales.cantidadTotal)
        assertEquals(230.0, totales.cantidadPorProducto.getValue("Bidon").monto, 0.001)
        assertEquals(3, totales.cantidadPorProducto.getValue("Bidon").cantidad)
        assertEquals(120.0, totales.cantidadPorProducto.getValue("Soda").monto, 0.001)
    }

    @Test
    fun `changing month while request is in flight resets to Idle and ignores stale result`() = runTest {
        val mesInicial = Meses.ENERO
        val mesNuevo = Meses.FEBRERO
        val pending = CompletableDeferred<CajaResult>()
        viewModel.seleccionarMes(mesInicial)
        coEvery { repository.getCajaPorMes(mesInicial.numero) } coAnswers { pending.await() }
        backgroundScope.launch { viewModel.cajaTotales.collect {} }

        viewModel.getCajaPorMes()
        advanceUntilIdle()
        assertEquals(CajaUiState.Loading, viewModel.cajaUiState)

        viewModel.seleccionarMes(mesNuevo)
        pending.complete(
            CajaResult.Success(
                DataCajaResponse(success = true, caja = listOf(Venta(producto = "Bidon", cantidad = 1, monto = 100.0)))
            )
        )
        advanceUntilIdle()

        assertEquals(CajaUiState.Idle, viewModel.cajaUiState)
        assertNull(viewModel.mesCargado.value)
        assertEquals(0.0, viewModel.cajaTotales.value.montoTotal, 0.001)
    }

    @Test
    fun `month change during non-suspending request does not leave Loading state`() = runTest {
        val mesInicial = Meses.ENERO
        val mesNuevo = Meses.FEBRERO
        viewModel.seleccionarMes(mesInicial)
        coEvery { repository.getCajaPorMes(mesInicial.numero) } coAnswers {
            viewModel.seleccionarMes(mesNuevo)
            CajaResult.Success(DataCajaResponse(success = true, caja = emptyList()))
        }

        viewModel.getCajaPorMes()
        advanceUntilIdle()

        assertEquals(CajaUiState.Idle, viewModel.cajaUiState)
        assertNull(viewModel.mesCargado.value)
    }

    @Test
    fun `header month reflects loaded data and clears on month change`() = runTest {
        val mes = Meses.ABRIL
        viewModel.seleccionarMes(mes)
        coEvery { repository.getCajaPorMes(mes.numero) } returns CajaResult.Success(
            DataCajaResponse(success = true, caja = listOf(Venta(producto = "Bidon", cantidad = 1, monto = 100.0)))
        )

        viewModel.getCajaPorMes()
        advanceUntilIdle()
        assertEquals(mes, viewModel.mesCargado.value)

        viewModel.seleccionarMes(Meses.MAYO)
        assertNull(viewModel.mesCargado.value)
        assertEquals(CajaUiState.Idle, viewModel.cajaUiState)
    }
    @Test
    fun `getCajaPorMes also loads the month's collections per payment method`() = runTest {
        val totales = listOf(CobroForma("Efectivo", 1500.0), CobroForma("Transferencia", 500.0))
        viewModel.seleccionarMes(Meses.MARZO)
        coEvery { repository.getCajaPorMes(Meses.MARZO.numero) } returns CajaResult.Success(DataCajaResponse(true, emptyList()))
        coEvery { repository.getCobrosPorMes(Meses.MARZO.numero) } returns CobrosResult.Success(totales)

        viewModel.getCajaPorMes()
        assertEquals(CobrosUiState.Loading, viewModel.cobrosUiState)
        advanceUntilIdle()

        assertEquals(CobrosUiState.Success(totales), viewModel.cobrosUiState)
        assertEquals(2000.0, (viewModel.cobrosUiState as CobrosUiState.Success).total, 0.0)
    }

    @Test
    fun `a failed collections request does not hide the month's sales`() = runTest {
        viewModel.seleccionarMes(Meses.MARZO)
        coEvery { repository.getCajaPorMes(Meses.MARZO.numero) } returns CajaResult.Success(
            DataCajaResponse(true, listOf(Venta(producto = "Bidon", precio = 100.0, cantidad = 1, monto = 100.0)))
        )
        coEvery { repository.getCobrosPorMes(Meses.MARZO.numero) } returns CobrosResult.Error(R.string.cuerpo_nulo_error)

        viewModel.getCajaPorMes()
        advanceUntilIdle()

        assertTrue(viewModel.cajaUiState is CajaUiState.Success)
        assertEquals(CobrosUiState.Error(R.string.cuerpo_nulo_error), viewModel.cobrosUiState)
    }

    @Test
    fun `selecting another month clears the loaded collections`() = runTest {
        viewModel.seleccionarMes(Meses.MARZO)
        coEvery { repository.getCajaPorMes(Meses.MARZO.numero) } returns CajaResult.Success(DataCajaResponse(true, emptyList()))
        viewModel.getCajaPorMes()
        advanceUntilIdle()

        viewModel.seleccionarMes(Meses.ABRIL)

        assertEquals(CobrosUiState.Idle, viewModel.cobrosUiState)
    }
}
