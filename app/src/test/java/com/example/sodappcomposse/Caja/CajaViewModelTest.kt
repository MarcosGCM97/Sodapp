package com.example.sodappcomposse.Caja

import com.example.sodappcomposse.R
import io.mockk.coEvery
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
        viewModel = CajaViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getCajaPorMes transitions to Success state when repository succeeds`() = runTest {
        // Arrange
        val responseData = DataCajaResponse(success = true, caja = emptyList())
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
    fun `getCajaPorMes does not update state if month changes during request`() = runTest {
        // Arrange
        val mesInicial = Meses.ENERO
        val mesNuevo = Meses.FEBRERO
        viewModel.seleccionarMes(mesInicial)
        
        coEvery { repository.getCajaPorMes(mesInicial.numero) } coAnswers {
            // Simulamos un retraso y cambiamos el mes en el ViewModel
            viewModel.seleccionarMes(mesNuevo)
            CajaResult.Success(DataCajaResponse(success = true, caja = emptyList()))
        }

        // Act
        viewModel.getCajaPorMes()
        advanceUntilIdle()

        // Assert
        // El estado no debe ser Success del mes inicial porque el mes cambió
        // En la implementación actual, se quedaría en Loading (o Idle si se reinicia) 
        // hasta que se llame de nuevo para el nuevo mes.
        // Como no llamamos a getCajaPorMes para FEBRERO, verificamos que NO es Success del inicial.
        assertTrue(viewModel.cajaUiState !is CajaUiState.Success)
    }
}
