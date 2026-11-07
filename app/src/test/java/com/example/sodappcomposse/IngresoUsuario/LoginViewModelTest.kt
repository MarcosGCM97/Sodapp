package com.example.sodappcomposse.IngresoUsuario

import com.example.sodappcomposse.API.UsuarioResponse
import com.example.sodappcomposse.R
import com.example.sodappcomposse.Cliente.ClienteRepository
import com.example.sodappcomposse.Producto.ProductoRepository
import com.example.sodappcomposse.UserPreferencesRepository
import com.example.sodappcomposse.auth.AuthSession
import com.example.sodappcomposse.auth.SessionPermissions
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
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
class LoginViewModelTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var authSession: AuthSession
    private lateinit var clienteRepository: ClienteRepository
    private lateinit var productoRepository: ProductoRepository
    private lateinit var viewModel: LoginViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mockk()
        authSession = mockk(relaxUnitFun = true)
        every { authSession.sessionExpired } returns MutableStateFlow(false)
        every { authSession.permissions } returns MutableStateFlow(null)
        clienteRepository = mockk(relaxUnitFun = true)
        productoRepository = mockk(relaxUnitFun = true)
        val preferences = mockk<UserPreferencesRepository>()
        every { preferences.isLoggedIn } returns flowOf(false)
        viewModel = LoginViewModel(authRepository, preferences, authSession, clienteRepository, productoRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `successful login starts a session with the jwt and drops cached data`() = runTest {
        coEvery { authRepository.login(any()) } returns AuthResult.Success(
            UsuarioResponse(true, "ok", Usuario(idUs = 5, nombreUs = "marcos"), "jwt-123")
        )

        viewModel.login("marcos", "secret")
        advanceUntilIdle()

        coVerify { authSession.startSession("5", "marcos", "jwt-123", SessionPermissions()) }
        verify { clienteRepository.clearCache() }
        verify { productoRepository.clearCache() }
        assertTrue(viewModel.loginUiState is LoginUiState.Success)
    }

    @Test
    fun `successful login shows a welcome message, not the exception template`() = runTest {
        coEvery { authRepository.login(any()) } returns AuthResult.Success(
            UsuarioResponse(true, "Usuario encontrado.", Usuario(idUs = 5, nombreUs = "marcos"), "jwt-123")
        )

        viewModel.login("marcos", "secret")
        advanceUntilIdle()

        val state = viewModel.loginUiState as LoginUiState.Success
        assertEquals(R.string.login_success_format, state.messageRes)
        assertTrue(state.args.contentEquals(arrayOf<Any>("marcos")))
    }

    @Test
    fun `login response without token is an error and starts no session`() = runTest {
        coEvery { authRepository.login(any()) } returns AuthResult.Success(
            UsuarioResponse(true, "ok", Usuario(idUs = 5, nombreUs = "marcos"), null)
        )

        viewModel.login("marcos", "secret")
        advanceUntilIdle()

        coVerify(exactly = 0) { authSession.startSession(any(), any(), any(), any()) }
        assertTrue(viewModel.loginUiState is LoginUiState.Error)
    }

    @Test
    fun `logout ends the session and drops cached data`() = runTest {
        viewModel.logout()
        advanceUntilIdle()

        coVerify { authSession.endSession() }
        verify { clienteRepository.clearCache() }
        verify { productoRepository.clearCache() }
    }

    @Test
    fun `successful login stores the permissions of the response`() = runTest {
        val permisos = SessionPermissions(esDueno = false, permisos = setOf("ventas.ver", "repartos.ver"))
        coEvery { authRepository.login(any()) } returns AuthResult.Success(
            UsuarioResponse(
                true, "ok", Usuario(idUs = 7, nombreUs = "juan"), "jwt-7",
                esDueno = false, permisos = listOf("ventas.ver", "repartos.ver")
            )
        )

        viewModel.login("juan", "secret")
        advanceUntilIdle()

        coVerify { authSession.startSession("7", "juan", "jwt-7", permisos) }
    }

    @Test
    fun `restoring a session loads stored permissions and refreshes them from the server`() = runTest {
        val actuales = SessionPermissions(esDueno = false, permisos = setOf("cobros.ver"))
        coEvery { authRepository.getSesion() } returns SesionResult.Success(actuales)

        viewModel.restoreSession()
        advanceUntilIdle()

        coVerify { authSession.loadStoredPermissions() }
        coVerify { authSession.updatePermissions(actuales) }
    }

    @Test
    fun `restoring a session rejected with 401 ends the session`() = runTest {
        coEvery { authRepository.getSesion() } returns SesionResult.Unauthorized

        viewModel.restoreSession()
        advanceUntilIdle()

        verify { authSession.markExpired() }
        coVerify(exactly = 0) { authSession.updatePermissions(any()) }
    }

    @Test
    fun `restoring a session that fails for another reason keeps the stored permissions`() = runTest {
        coEvery { authRepository.getSesion() } returns SesionResult.Error(R.string.error_red_verificar)

        viewModel.restoreSession()
        advanceUntilIdle()

        coVerify { authSession.loadStoredPermissions() }
        coVerify(exactly = 0) { authSession.updatePermissions(any()) }
        verify(exactly = 0) { authSession.markExpired() }
    }
}
