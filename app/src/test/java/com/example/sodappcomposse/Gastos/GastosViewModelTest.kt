package com.example.sodappcomposse.Gastos

import com.example.sodappcomposse.Producto.Producto
import com.example.sodappcomposse.Producto.ProductoRepository
import com.example.sodappcomposse.Producto.ProductoResponse
import com.example.sodappcomposse.Producto.ProductoResult
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class GastosViewModelTest {

    private lateinit var repository: GastoRepository
    private lateinit var productos: ProductoRepository
    private lateinit var viewModel: GastosViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val mes = YearMonth.now()
    private val desde = mes.atDay(1).toString()
    private val hasta = mes.atEndOfMonth().toString()
    private val gasto = Gasto(id = 1, categoria = "vehiculo", monto = 1500.0, fecha = desde)
    private val periodo = GastosPeriodo(desde, hasta, listOf(gasto), 1500.0)
    private val bidon = Producto(id = 3, nombrePr = "Bidón", retornable = 1)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        productos = mockk()
        coEvery { repository.listar(any(), any(), any()) } returns GastoResult.Success(periodo)
        coEvery { productos.getProductos(any()) } returns ProductoResult.Success(ProductoResponse(true, listOf(bidon)))
        viewModel = GastosViewModel(repository, productos)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `cargar lists the current month and loads the products`() = runTest {
        viewModel.cargar()
        advanceUntilIdle()

        coVerify { repository.listar(desde, hasta, null) }
        assertEquals(periodo, viewModel.periodo.value)
        assertEquals(listOf(bidon), viewModel.productos.value)
        assertEquals(GastosUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `cambiarMes and filtrar reload the matching period and category`() = runTest {
        viewModel.cambiarMes(-1)
        advanceUntilIdle()
        val anterior = mes.minusMonths(1)
        coVerify { repository.listar(anterior.atDay(1).toString(), anterior.atEndOfMonth().toString(), null) }
        assertEquals(anterior, viewModel.mes.value)

        viewModel.filtrar(CategoriaGasto.COMBUSTIBLE)
        advanceUntilIdle()
        coVerify { repository.listar(anterior.atDay(1).toString(), anterior.atEndOfMonth().toString(), CategoriaGasto.COMBUSTIBLE) }
        assertEquals(CategoriaGasto.COMBUSTIBLE, viewModel.categoria.value)
    }

    @Test
    fun `a failed list shows the error and keeps no stale period`() = runTest {
        coEvery { repository.listar(any(), any(), any()) } returns GastoResult.Error(R.string.error_sin_permiso)

        viewModel.cargar()
        advanceUntilIdle()

        assertEquals(GastosUiState.Error(R.string.error_sin_permiso), viewModel.uiState.value)
        assertEquals(null, viewModel.periodo.value)
    }

    @Test
    fun `guardar saves, reports it, marks it saved and reloads`() = runTest {
        val nuevo = NuevoGasto(categoria = CategoriaGasto.OTRO, monto = 10.0, fecha = desde)
        coEvery { repository.guardar(nuevo) } returns GastoResult.Success(gasto)

        viewModel.guardar(nuevo)
        advanceUntilIdle()

        assertEquals(GastosUiState.Success(R.string.gasto_guardado), viewModel.uiState.value)
        assertTrue(viewModel.guardado.value)
        coVerify { repository.listar(desde, hasta, null) }
    }

    @Test
    fun `a rejected guardar keeps the form open with the backend message`() = runTest {
        val nuevo = NuevoGasto(categoria = CategoriaGasto.RECARGA_ENVASES, monto = 10.0, fecha = desde, productoId = 3, cantidad = 9)
        val error = GastoResult.Error(R.string.error_format, arrayOf("No hay suficientes envases vacíos"))
        coEvery { repository.guardar(nuevo) } returns error

        viewModel.guardar(nuevo)
        advanceUntilIdle()

        assertEquals(GastosUiState.Error(R.string.error_format, arrayOf("No hay suficientes envases vacíos")), viewModel.uiState.value)
        assertFalse(viewModel.guardado.value)
    }

    @Test
    fun `eliminar deletes, reports it and reloads`() = runTest {
        coEvery { repository.eliminar(1) } returns GastoResult.Success(Unit)

        viewModel.eliminar(1)
        advanceUntilIdle()

        coVerify { repository.eliminar(1) }
        assertEquals(GastosUiState.Success(R.string.gasto_eliminado), viewModel.uiState.value)
        assertEquals(periodo, viewModel.periodo.value)
    }
}
