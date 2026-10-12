package com.example.sodappcomposse.IngresoUsuario

import com.example.sodappcomposse.API.UsuarioResponse
import com.example.sodappcomposse.Cliente.ClienteRepository
import com.example.sodappcomposse.Producto.ProductoRepository
import com.example.sodappcomposse.UserPreferencesRepository
import com.example.sodappcomposse.auth.AuthSession
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

        coVerify { authSession.startSession("5", "marcos", "jwt-123") }
        verify { clienteRepository.clearCache() }
        verify { productoRepository.clearCache() }
        assertTrue(viewModel.loginUiState is LoginUiState.Success)
    }

    @Test
    fun `login response without token is an error and starts no session`() = runTest {
        coEvery { authRepository.login(any()) } returns AuthResult.Success(
            UsuarioResponse(true, "ok", Usuario(idUs = 5, nombreUs = "marcos"), null)
        )

        viewModel.login("marcos", "secret")
        advanceUntilIdle()

        coVerify(exactly = 0) { authSession.startSession(any(), any(), any()) }
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
}
