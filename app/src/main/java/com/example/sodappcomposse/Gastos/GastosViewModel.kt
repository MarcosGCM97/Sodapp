package com.example.sodappcomposse.Gastos

import androidx.annotation.StringRes
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sodappcomposse.Producto.Producto
import com.example.sodappcomposse.Producto.ProductoRepository
import com.example.sodappcomposse.Producto.ProductoResult
import com.example.sodappcomposse.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

sealed interface GastosUiState {
    object Idle : GastosUiState
    object Loading : GastosUiState
    data class Success(@StringRes val messageRes: Int) : GastosUiState
    data class Error(@StringRes val messageRes: Int, val args: Array<Any> = emptyArray()) : GastosUiState {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Error) return false
            return messageRes == other.messageRes && args.contentEquals(other.args)
        }
        override fun hashCode(): Int = 31 * messageRes + args.contentHashCode()
    }
}

/** Expenses of one month, optionally of one category, and the products a purchase can move. */
@HiltViewModel
class GastosViewModel @Inject constructor(
    private val repository: GastoRepository,
    private val productoRepository: ProductoRepository
) : ViewModel() {

    private val _mes = mutableStateOf(YearMonth.now())
    val mes: State<YearMonth> = _mes

    private val _categoria = mutableStateOf<CategoriaGasto?>(null)
    val categoria: State<CategoriaGasto?> = _categoria

    private val _periodo = mutableStateOf<GastosPeriodo?>(null)
    val periodo: State<GastosPeriodo?> = _periodo

    private val _productos = mutableStateOf<List<Producto>>(emptyList())
    val productos: State<List<Producto>> = _productos

    private val _uiState: MutableState<GastosUiState> = mutableStateOf(GastosUiState.Idle)
    val uiState: State<GastosUiState> = _uiState

    // True once a guardar succeeded; the quick form from a reparto closes on it
    private val _guardado = mutableStateOf(false)
    val guardado: State<Boolean> = _guardado

    /** Loads the month list and the products (for the purchase and refill fields). */
    fun cargar() {
        recargar()
        viewModelScope.launch {
            when (val result = productoRepository.getProductos()) {
                is ProductoResult.Success -> _productos.value = result.data.productos
                is ProductoResult.Error -> {}
            }
        }
    }

    fun cambiarMes(delta: Long) {
        _mes.value = _mes.value.plusMonths(delta)
        recargar()
    }

    fun filtrar(categoria: CategoriaGasto?) {
        _categoria.value = categoria
        recargar()
    }

    fun guardar(gasto: NuevoGasto) {
        viewModelScope.launch {
            _uiState.value = GastosUiState.Loading
            when (val result = repository.guardar(gasto)) {
                is GastoResult.Success -> {
                    _guardado.value = true
                    _uiState.value = GastosUiState.Success(R.string.gasto_guardado)
                    stockCambiado()
                    listar()
                }
                is GastoResult.Error -> _uiState.value = GastosUiState.Error(result.messageRes, result.args)
            }
        }
    }

    fun eliminar(id: Int) {
        viewModelScope.launch {
            _uiState.value = GastosUiState.Loading
            when (val result = repository.eliminar(id)) {
                is GastoResult.Success -> {
                    _uiState.value = GastosUiState.Success(R.string.gasto_eliminado)
                    stockCambiado()
                    listar()
                }
                is GastoResult.Error -> _uiState.value = GastosUiState.Error(result.messageRes, result.args)
            }
        }
    }

    fun resetState() {
        _uiState.value = GastosUiState.Idle
    }

    fun guardadoUsado() {
        _guardado.value = false
    }

    // A purchase or refill may have moved stock: the cached product list (Inventario, Ventas,
    // this form) must be fetched again, as after a sale
    private fun stockCambiado() {
        productoRepository.clearCache()
    }

    private fun recargar() {
        viewModelScope.launch {
            _uiState.value = GastosUiState.Loading
            _uiState.value = listar() ?: GastosUiState.Idle
        }
    }

    // Refreshes the period; returns the error state when the list failed
    private suspend fun listar(): GastosUiState.Error? {
        val mes = _mes.value
        return when (val result = repository.listar(mes.atDay(1).toString(), mes.atEndOfMonth().toString(), _categoria.value)) {
            is GastoResult.Success -> {
                _periodo.value = result.data
                null
            }
            is GastoResult.Error -> {
                _periodo.value = null
                GastosUiState.Error(result.messageRes, result.args)
            }
        }
    }
}
