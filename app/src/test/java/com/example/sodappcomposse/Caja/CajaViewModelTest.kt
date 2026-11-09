package com.example.sodappcomposse.Caja

import com.example.sodappcomposse.R
import com.example.sodappcomposse.UserPreferencesRepository
import io.mockk.every
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class CajaViewModelTest {

    private lateinit var repository: CajaRepository
    private lateinit var viewModel: CajaViewModel
    private val preferencias: UserPreferencesRepository = mockk { every { userName } returns flowOf("Ariza") }
    private val testDispatcher = StandardTestDispatcher()
    private val hoy = LocalDate.of(2026, 3, 18) // Wednesday

    private fun reporte(periodo: Periodo) = Reporte(
        periodo, ResumenReporte(vendido = 100.0, ventas = 1), ResumenAnterior(),
        emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), null
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        coEvery { repository.getReporte(any()) } answers { ReporteResult.Success(reporte(firstArg())) }
        viewModel = CajaViewModel(repository, preferencias)
        viewModel.hoy = { hoy }
        viewModel.seleccionarPeriodo(Periodo.de(TipoPeriodo.MES, hoy))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `starts on the current month and loads it`() = runTest {
        val vm = CajaViewModel(repository, preferencias)
        assertEquals(ReporteUiState.Loading, vm.uiState.value)
        advanceUntilIdle()

        assertEquals(Periodo.de(TipoPeriodo.MES, LocalDate.now()), vm.periodo.value)
        assertEquals(ReporteUiState.Success(reporte(vm.periodo.value)), vm.uiState.value)
    }

    @Test
    fun `seleccionarTipo switches to the day, week or month of today when it is in view`() = runTest {
        viewModel.seleccionarTipo(TipoPeriodo.SEMANA)
        assertEquals(Periodo(TipoPeriodo.SEMANA, LocalDate.of(2026, 3, 16), LocalDate.of(2026, 3, 22)), viewModel.periodo.value)

        viewModel.seleccionarTipo(TipoPeriodo.DIA)
        assertEquals(Periodo(TipoPeriodo.DIA, hoy, hoy), viewModel.periodo.value)
        advanceUntilIdle()
        assertEquals(ReporteUiState.Success(reporte(Periodo(TipoPeriodo.DIA, hoy, hoy))), viewModel.uiState.value)
    }

    @Test
    fun `seleccionarTipo of a past period keeps its start`() = runTest {
        viewModel.anterior() // February
        viewModel.seleccionarTipo(TipoPeriodo.SEMANA)

        assertEquals(Periodo(TipoPeriodo.SEMANA, LocalDate.of(2026, 1, 26), LocalDate.of(2026, 2, 1)), viewModel.periodo.value)
    }

    @Test
    fun `anterior crosses the year and carries it`() = runTest {
        viewModel.seleccionarPeriodo(Periodo.de(TipoPeriodo.MES, LocalDate.of(2026, 1, 10)))
        viewModel.anterior()
        advanceUntilIdle()

        val diciembre = Periodo(TipoPeriodo.MES, LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 31))
        assertEquals(diciembre, viewModel.periodo.value)
        coVerify { repository.getReporte(diciembre) }
    }

    @Test
    fun `siguiente never goes past the current period`() = runTest {
        assertFalse(viewModel.puedeAvanzar())
        viewModel.siguiente()
        assertEquals(Periodo.de(TipoPeriodo.MES, hoy), viewModel.periodo.value)

        viewModel.anterior()
        assertTrue(viewModel.puedeAvanzar())
        viewModel.siguiente()
        assertEquals(Periodo.de(TipoPeriodo.MES, hoy), viewModel.periodo.value)
    }

    @Test
    fun `seleccionarFecha clamps a future date to today`() = runTest {
        viewModel.seleccionarTipo(TipoPeriodo.DIA)
        viewModel.seleccionarFecha(LocalDate.of(2026, 4, 2))

        assertEquals(Periodo(TipoPeriodo.DIA, hoy, hoy), viewModel.periodo.value)
    }

    @Test
    fun `shows the repository error and reintentar loads again`() = runTest {
        coEvery { repository.getReporte(any()) } returns ReporteResult.Error(R.string.error_api_format, listOf(500))
        viewModel.anterior()
        advanceUntilIdle()
        assertEquals(ReporteUiState.Error(R.string.error_api_format, listOf(500)), viewModel.uiState.value)

        coEvery { repository.getReporte(any()) } answers { ReporteResult.Success(reporte(firstArg())) }
        viewModel.reintentar()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is ReporteUiState.Success)
    }

    @Test
    fun `a slow answer of a previous period never replaces the current one`() = runTest {
        advanceUntilIdle()
        val lento = CompletableDeferred<ReporteResult>()
        val febrero = Periodo.de(TipoPeriodo.MES, LocalDate.of(2026, 2, 1))
        coEvery { repository.getReporte(febrero) } coAnswers { lento.await() }

        viewModel.anterior() // February, never answers in time
        advanceUntilIdle()
        viewModel.siguiente() // back to March
        advanceUntilIdle()
        lento.complete(ReporteResult.Success(reporte(febrero)))
        advanceUntilIdle()

        assertEquals(ReporteUiState.Success(reporte(Periodo.de(TipoPeriodo.MES, hoy))), viewModel.uiState.value)
    }
}
