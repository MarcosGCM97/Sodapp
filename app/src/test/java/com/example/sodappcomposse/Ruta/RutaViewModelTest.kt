package com.example.sodappcomposse.Ruta

import com.example.sodappcomposse.Producto.ProductoResult
import com.example.sodappcomposse.Producto.ProductoUiState
import com.example.sodappcomposse.R
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
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
class RutaViewModelTest {

    private lateinit var repository: RutaRepository
    private lateinit var viewModel: RutaViewModel

    private val planta = Planta("Ruta 20 km 6", "La Calera")
    private fun parada(orden: Int, estado: String) =
        RutaParada(paradaId = 40 + orden, orden = orden, estado = estado, direccion = "Calle $orden", localidad = "La Calera")

    private val ok = ProductoResult.Success(RutaResponse(success = true))

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        repository = mockk()
        viewModel = RutaViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `cargar shows the route and its legs`() = runTest {
        val ruta = Ruta(repartoId = 7, planta = planta, paradas = listOf(parada(1, "pendiente"), parada(2, "entregada")))
        coEvery { repository.getRuta(7) } returns ProductoResult.Success(ruta)

        viewModel.cargar(7)
        advanceUntilIdle()

        assertEquals(ruta, viewModel.ruta.value)
        assertEquals(armarTramos(ruta), viewModel.tramos.value)
    }

    @Test
    fun `iniciar starts the trip and reloads the route`() = runTest {
        coEvery { repository.getRuta(7) } returns ProductoResult.Success(Ruta(repartoId = 7, inicio = "2026-11-03 08:00:00"))
        coEvery { repository.iniciar(7) } returns ok

        viewModel.iniciar(7)
        advanceUntilIdle()

        coVerify(exactly = 1) { repository.iniciar(7) }
        assertEquals("2026-11-03 08:00:00", viewModel.ruta.value?.inicio)
    }

    @Test
    fun `puedeFinalizar only when started, not finished and no stop pendiente`() {
        val resueltas = listOf(parada(1, "entregada"), parada(2, "no_entregada"))
        assertTrue(puedeFinalizar(Ruta(inicio = "2026-11-03 08:00:00", paradas = resueltas)))
        assertFalse(puedeFinalizar(Ruta(inicio = null, paradas = resueltas)))
        assertFalse(puedeFinalizar(Ruta(inicio = "2026-11-03 08:00:00", fin = "2026-11-03 11:00:00", paradas = resueltas)))
        assertFalse(puedeFinalizar(Ruta(inicio = "2026-11-03 08:00:00", paradas = resueltas + parada(3, "pendiente"))))
        assertFalse(puedeFinalizar(Ruta(inicio = "2026-11-03 08:00:00", paradas = emptyList())))
        assertFalse(puedeFinalizar(null))
    }

    @Test
    fun `finalizar reports success and reloads`() = runTest {
        coEvery { repository.finalizar(7) } returns ok
        coEvery { repository.getRuta(7) } returns ProductoResult.Success(Ruta(repartoId = 7, fin = "2026-11-03 11:25:00"))

        viewModel.finalizar(7)
        advanceUntilIdle()

        assertEquals(ProductoUiState.Success(R.string.recorrido_finalizado), viewModel.uiState.value)
        assertEquals("2026-11-03 11:25:00", viewModel.ruta.value?.fin)
    }

    @Test
    fun `finalizar error is shown`() = runTest {
        val error = ProductoResult.Error(R.string.error_format, arrayOf("Quedan paradas pendientes"))
        coEvery { repository.finalizar(7) } returns error

        viewModel.finalizar(7)
        advanceUntilIdle()

        assertEquals(ProductoUiState.Error(R.string.error_format, arrayOf("Quedan paradas pendientes")), viewModel.uiState.value)
    }

    @Test
    fun `guardarPlanta requires address and locality`() = runTest {
        viewModel.guardarPlanta(" ", "La Calera", null, null)
        advanceUntilIdle()

        assertEquals(ProductoUiState.Error(R.string.campos_requeridos_error), viewModel.uiState.value)
        coVerify(exactly = 0) { repository.guardarPlanta(any()) }
    }

    @Test
    fun `guardarPlanta saves the trimmed plant`() = runTest {
        coEvery { repository.guardarPlanta(any()) } returns ok
        coEvery { repository.getPlanta() } returns ProductoResult.Success(planta)

        viewModel.guardarPlanta(" Ruta 20 km 6 ", " La Calera ", null, null)
        advanceUntilIdle()

        coVerify { repository.guardarPlanta(planta) }
        assertEquals(ProductoUiState.Success(R.string.planta_guardada), viewModel.uiState.value)
        assertEquals(planta, viewModel.planta.value)
    }

    @Test
    fun `a newer load wins over a slower older one`() = runTest {
        val vieja = Ruta(repartoId = 7, paradas = listOf(parada(1, "pendiente")))
        val nueva = Ruta(repartoId = 7, paradas = listOf(parada(1, "entregada")))
        var llamadas = 0
        coEvery { repository.getRuta(7) } coAnswers {
            llamadas++
            if (llamadas == 1) { delay(5_000); ProductoResult.Success(vieja) } else ProductoResult.Success(nueva)
        }

        viewModel.cargar(7)
        runCurrent()
        viewModel.cargar(7)
        advanceUntilIdle()

        assertEquals(nueva, viewModel.ruta.value)
    }

    @Test
    fun `a failed load is not shown as an error toast`() = runTest {
        coEvery { repository.getRuta(7) } returns ProductoResult.Error(R.string.error_red_verificar)

        viewModel.cargar(7)
        advanceUntilIdle()

        assertEquals(ProductoUiState.Idle, viewModel.uiState.value)
    }
}
