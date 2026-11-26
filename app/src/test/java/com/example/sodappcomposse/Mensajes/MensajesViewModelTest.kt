package com.example.sodappcomposse.Mensajes

import com.example.sodappcomposse.R
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
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
class MensajesViewModelTest {

    private lateinit var repository: PlantillasRepository
    private val testDispatcher = StandardTestDispatcher()
    private val enCache = mapOf(TipoPlantilla.DEUDA to "Viejo {saldo}")
    private val guardadas = mapOf(TipoPlantilla.DEUDA to "Hola {cliente}", TipoPlantilla.VENTA to "Gracias")

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        every { repository.plantillasEnCache() } returns enCache
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `starts with the cached templates so a message can be sent before the request ends`() {
        val viewModel = MensajesViewModel(repository)

        assertEquals(enCache, viewModel.plantillas.value)
    }

    @Test
    fun `cargar replaces the templates with the server ones`() = runTest {
        coEvery { repository.getPlantillas() } returns PlantillasResult.Success(guardadas)
        val viewModel = MensajesViewModel(repository)

        viewModel.cargar()
        advanceUntilIdle()

        assertEquals(guardadas, viewModel.plantillas.value)
        assertEquals(MensajesUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `a failed cargar keeps the cached templates and reports the error`() = runTest {
        coEvery { repository.getPlantillas() } returns PlantillasResult.Error(R.string.error_red_verificar)
        val viewModel = MensajesViewModel(repository)

        viewModel.cargar()
        advanceUntilIdle()

        assertEquals(enCache, viewModel.plantillas.value)
        assertEquals(MensajesUiState.Error(R.string.error_red_verificar), viewModel.uiState.value)
    }

    @Test
    fun `guardar sends the text and shows the saved templates`() = runTest {
        coEvery { repository.guardarPlantilla(TipoPlantilla.VENTA, "Gracias") } returns PlantillasResult.Success(guardadas)
        val viewModel = MensajesViewModel(repository)

        viewModel.guardar(TipoPlantilla.VENTA, "Gracias")
        advanceUntilIdle()

        assertEquals(guardadas, viewModel.plantillas.value)
        assertEquals(MensajesUiState.Success(R.string.mensaje_plantilla_guardada), viewModel.uiState.value)
    }

    @Test
    fun `guardar with a blank text never calls the server`() = runTest {
        val viewModel = MensajesViewModel(repository)

        viewModel.guardar(TipoPlantilla.DEUDA, "  \n")
        advanceUntilIdle()

        assertEquals(MensajesUiState.Error(R.string.mensaje_plantilla_vacia), viewModel.uiState.value)
        coVerify(exactly = 0) { repository.guardarPlantilla(any(), any()) }
    }

    @Test
    fun `guardar surfaces the backend error and keeps the templates`() = runTest {
        val error = PlantillasResult.Error(R.string.error_format, arrayOf("Variable no permitida"))
        coEvery { repository.guardarPlantilla(any(), any()) } returns error
        val viewModel = MensajesViewModel(repository)

        viewModel.guardar(TipoPlantilla.DEUDA, "{total}")
        advanceUntilIdle()

        assertEquals(enCache, viewModel.plantillas.value)
        assertEquals(MensajesUiState.Error(R.string.error_format, arrayOf("Variable no permitida")), viewModel.uiState.value)
    }

    @Test
    fun `restaurar deletes the template and confirms the default message is back`() = runTest {
        coEvery { repository.restaurarPlantilla(TipoPlantilla.DEUDA) } returns PlantillasResult.Success(emptyMap())
        val viewModel = MensajesViewModel(repository)

        viewModel.restaurar(TipoPlantilla.DEUDA)
        advanceUntilIdle()

        assertEquals(emptyMap<TipoPlantilla, String>(), viewModel.plantillas.value)
        assertEquals(MensajesUiState.Success(R.string.mensaje_plantilla_restaurada), viewModel.uiState.value)
    }
}
