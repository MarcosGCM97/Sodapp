package com.example.sodappcomposse.Empresa

import com.example.sodappcomposse.R
import com.example.sodappcomposse.auth.AuthSession
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
class EmpresaViewModelTest {

    private lateinit var repository: EmpresaRepository
    private lateinit var authSession: AuthSession
    private lateinit var viewModel: EmpresaViewModel
    private val testDispatcher = StandardTestDispatcher()
    private val empresa = Empresa("Soderia La Calera", "351 555", "San Martín 123")

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        authSession = mockk(relaxUnitFun = true)
        every { authSession.currentToken() } returns "jwt"
        viewModel = EmpresaViewModel(repository, authSession)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `cargar exposes the saved company data`() = runTest {
        coEvery { repository.getEmpresa() } returns EmpresaResult.Success(empresa)

        viewModel.cargar()
        advanceUntilIdle()

        assertEquals(empresa, viewModel.empresa.value)
        assertEquals(EmpresaUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `cargar failure shows the error`() = runTest {
        coEvery { repository.getEmpresa() } returns EmpresaResult.Error(R.string.error_red_verificar)

        viewModel.cargar()
        advanceUntilIdle()

        assertEquals(EmpresaUiState.Error(R.string.error_red_verificar), viewModel.uiState.value)
    }

    @Test
    fun `guardar stores the data and updates the session company name`() = runTest {
        coEvery { repository.guardarEmpresa(empresa) } returns EmpresaResult.Success(empresa)

        viewModel.guardar(empresa.nombre, empresa.telefono!!, empresa.direccion!!)
        advanceUntilIdle()

        assertEquals(empresa, viewModel.empresa.value)
        assertEquals(EmpresaUiState.Success(R.string.empresa_guardada), viewModel.uiState.value)
        coVerify { authSession.updateCompanyName("Soderia La Calera", "jwt") }
    }

    @Test
    fun `guardar with a blank name is rejected without calling the server`() = runTest {
        viewModel.guardar("   ", "351", "")
        advanceUntilIdle()

        assertEquals(EmpresaUiState.Error(R.string.empresa_nombre_requerido), viewModel.uiState.value)
        coVerify(exactly = 0) { repository.guardarEmpresa(any()) }
    }

    @Test
    fun `guardar failure keeps the session company name`() = runTest {
        coEvery { repository.guardarEmpresa(any()) } returns EmpresaResult.Error(R.string.error_sin_permiso)

        viewModel.guardar("Otra", "", "")
        advanceUntilIdle()

        assertEquals(EmpresaUiState.Error(R.string.error_sin_permiso), viewModel.uiState.value)
        coVerify(exactly = 0) { authSession.updateCompanyName(any(), any()) }
    }
}
