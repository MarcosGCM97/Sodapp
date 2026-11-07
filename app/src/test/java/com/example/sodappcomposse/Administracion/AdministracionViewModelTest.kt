package com.example.sodappcomposse.Administracion

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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AdministracionViewModelTest {

    private lateinit var repository: AdministracionRepository
    private lateinit var viewModel: AdministracionViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val usuarios = listOf(
        UsuarioAdmin(5, "marcos", null, null, 1, true),
        UsuarioAdmin(7, "juan", 2, "Vendedor", 1, false)
    )
    private val roles = RolesAdmin(
        roles = listOf(RolAdmin(2, "Vendedor", listOf("ventas.ver"), 1)),
        permisosDisponibles = listOf(PermisoDisponible("ventas.ver", "Ver ventas"))
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        coEvery { repository.getUsuarios() } returns AdminResult.Success(usuarios)
        coEvery { repository.getRoles() } returns AdminResult.Success(roles)
        viewModel = AdministracionViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `cargar loads users, roles and the permission catalog`() = runTest {
        viewModel.cargar()
        advanceUntilIdle()

        assertEquals(usuarios, viewModel.usuarios.value)
        assertEquals(roles.roles, viewModel.roles.value)
        assertEquals(roles.permisosDisponibles, viewModel.permisosDisponibles.value)
        assertEquals(AdministracionUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `a failed load shows the error`() = runTest {
        coEvery { repository.getUsuarios() } returns AdminResult.Error(R.string.error_sin_permiso)

        viewModel.cargar()
        advanceUntilIdle()

        assertEquals(AdministracionUiState.Error(R.string.error_sin_permiso), viewModel.uiState.value)
    }

    @Test
    fun `creating a user confirms and reloads the lists`() = runTest {
        coEvery { repository.crearUsuario("ana", "secreto1", 2) } returns AdminResult.Success(Unit)

        viewModel.crearUsuario("ana", "secreto1", 2)
        advanceUntilIdle()

        assertEquals(AdministracionUiState.Success(R.string.usuario_creado), viewModel.uiState.value)
        coVerify { repository.getUsuarios() }
        coVerify { repository.getRoles() }
    }

    @Test
    fun `each user change calls its repository action`() = runTest {
        coEvery { repository.cambiarRol(7, 3) } returns AdminResult.Success(Unit)
        coEvery { repository.cambiarContrasena(7, "nueva123") } returns AdminResult.Success(Unit)
        coEvery { repository.cambiarActivo(7, false) } returns AdminResult.Success(Unit)

        viewModel.cambiarRol(7, 3)
        advanceUntilIdle()
        assertEquals(AdministracionUiState.Success(R.string.usuario_actualizado), viewModel.uiState.value)
        viewModel.cambiarContrasena(7, "nueva123")
        advanceUntilIdle()
        viewModel.cambiarActivo(7, false)
        advanceUntilIdle()

        coVerify { repository.cambiarRol(7, 3) }
        coVerify { repository.cambiarContrasena(7, "nueva123") }
        coVerify { repository.cambiarActivo(7, false) }
    }

    @Test
    fun `role changes call their repository action`() = runTest {
        coEvery { repository.crearRol("Cobrador", listOf("cobros.ver")) } returns AdminResult.Success(Unit)
        coEvery { repository.actualizarRol(4, "Cobrador", listOf("cobros.ver", "cobros.cargar")) } returns
            AdminResult.Success(Unit)

        viewModel.crearRol("Cobrador", listOf("cobros.ver"))
        advanceUntilIdle()
        assertEquals(AdministracionUiState.Success(R.string.rol_guardado), viewModel.uiState.value)
        viewModel.actualizarRol(4, "Cobrador", listOf("cobros.ver", "cobros.cargar"))
        advanceUntilIdle()

        coVerify { repository.crearRol("Cobrador", listOf("cobros.ver")) }
        coVerify { repository.actualizarRol(4, "Cobrador", listOf("cobros.ver", "cobros.cargar")) }
    }

    @Test
    fun `deleting a role with users shows the backend message`() = runTest {
        coEvery { repository.eliminarRol(2) } returns
            AdminResult.Error(R.string.error_format, arrayOf("El rol tiene usuarios asignados"))

        viewModel.eliminarRol(2)
        advanceUntilIdle()

        assertEquals(
            AdministracionUiState.Error(R.string.error_format, arrayOf("El rol tiene usuarios asignados")),
            viewModel.uiState.value
        )
    }

    @Test
    fun `deleting a role confirms it`() = runTest {
        coEvery { repository.eliminarRol(2) } returns AdminResult.Success(Unit)

        viewModel.eliminarRol(2)
        advanceUntilIdle()

        assertEquals(AdministracionUiState.Success(R.string.rol_eliminado), viewModel.uiState.value)
    }

    @Test
    fun `resetState goes back to idle`() = runTest {
        coEvery { repository.eliminarRol(2) } returns AdminResult.Success(Unit)
        viewModel.eliminarRol(2)
        advanceUntilIdle()

        viewModel.resetState()

        assertEquals(AdministracionUiState.Idle, viewModel.uiState.value)
    }
}
