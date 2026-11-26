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
    fun `cambiarContrasena sends the passwords and reports success`() = runTest {
        coEvery { repository.cambiarContrasena("vieja12", "nueva12") } returns EmpresaResult.Success(Unit)

        viewModel.cambiarContrasena("vieja12", "nueva12", "nueva12")
        advanceUntilIdle()

        assertEquals(EmpresaUiState.Success(R.string.contrasena_cambiada), viewModel.contrasenaState.value)
        assertEquals(EmpresaUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `cambiarContrasena without the current password is rejected without calling the server`() = runTest {
        viewModel.cambiarContrasena("", "nueva12", "nueva12")
        advanceUntilIdle()

        assertEquals(EmpresaUiState.Error(R.string.contrasena_actual_requerida), viewModel.contrasenaState.value)
        coVerify(exactly = 0) { repository.cambiarContrasena(any(), any()) }
    }

    @Test
    fun `cambiarContrasena with a new password under 6 characters is rejected without calling the server`() = runTest {
        viewModel.cambiarContrasena("vieja12", "12345", "12345")
        advanceUntilIdle()

        assertEquals(EmpresaUiState.Error(R.string.contrasena_nueva_corta_format, arrayOf(6)), viewModel.contrasenaState.value)
        coVerify(exactly = 0) { repository.cambiarContrasena(any(), any()) }
    }

    @Test
    fun `cambiarContrasena when the repetition differs is rejected without calling the server`() = runTest {
        viewModel.cambiarContrasena("vieja12", "nueva12", "nueva13")
        advanceUntilIdle()

        assertEquals(EmpresaUiState.Error(R.string.contrasena_no_coinciden), viewModel.contrasenaState.value)
        coVerify(exactly = 0) { repository.cambiarContrasena(any(), any()) }
    }

    @Test
    fun `cambiarContrasena shows the server error`() = runTest {
        val error = EmpresaResult.Error(R.string.error_format, arrayOf("La contraseña actual es incorrecta"))
        coEvery { repository.cambiarContrasena(any(), any()) } returns error

        viewModel.cambiarContrasena("mala123", "nueva12", "nueva12")
        advanceUntilIdle()

        assertEquals(EmpresaUiState.Error(error.messageRes, error.args), viewModel.contrasenaState.value)
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
