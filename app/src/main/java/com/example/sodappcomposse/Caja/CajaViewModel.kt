package com.example.sodappcomposse.Caja

import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sodappcomposse.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class CajaUiState {
    object Idle : CajaUiState()
    object Loading : CajaUiState()
    object Empty : CajaUiState()
    data class Error(@StringRes val messageRes: Int, val args: Array<Any> = emptyArray()) : CajaUiState() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Error) return false
            if (messageRes != other.messageRes) return false
            if (!args.contentEquals(other.args)) return false
            return true
        }
        override fun hashCode(): Int {
            var result = messageRes
            result = 31 * result + args.contentHashCode()
            return result
        }
    }
    data class Success(@StringRes val messageRes: Int, val args: Array<Any> = emptyArray()) : CajaUiState() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Success) return false
            if (messageRes != other.messageRes) return false
            if (!args.contentEquals(other.args)) return false
            return true
        }
        override fun hashCode(): Int {
            var result = messageRes
            result = 31 * result + args.contentHashCode()
            return result
        }
    }
}

data class CajaTotales(
    val cantidadPorProducto: Map<String, CantidadDeVentasPorProducto> = emptyMap(),
    val cantidadTotal: Int = 0,
    val montoTotal: Double = 0.0
)

@HiltViewModel
class CajaViewModel @Inject constructor(
    private val cajaRepository: CajaRepository
) : ViewModel() {

    private val _caja = MutableStateFlow(emptyCaja())

    // Totals use the amount stored at sale time (vt_mon), so later price
    // changes never alter past cash reports.
    val cajaTotales: StateFlow<CajaTotales> = _caja.map { response ->
        val items = response.caja ?: emptyList()
        val grouped = items.groupBy { it.producto.ifBlank { PRODUCTO_DESCONOCIDO } }.mapValues { (producto, ventas) ->
            CantidadDeVentasPorProducto(
                producto = producto,
                cantidad = ventas.sumOf { it.cantidad },
                monto = ventas.sumOf { it.monto }
            )
        }
        CajaTotales(
            cantidadPorProducto = grouped,
            cantidadTotal = items.sumOf { it.cantidad },
            montoTotal = items.sumOf { it.monto }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CajaTotales())

    private val _mesSeleccionadoUi = MutableStateFlow<Meses?>(null)
    val mesSeleccionadoUi: StateFlow<Meses?> = _mesSeleccionadoUi.asStateFlow()

    // Month the currently displayed data belongs to (null when nothing is loaded)
    private val _mesCargado = MutableStateFlow<Meses?>(null)
    val mesCargado: StateFlow<Meses?> = _mesCargado.asStateFlow()

    var cajaUiState: CajaUiState by mutableStateOf(CajaUiState.Idle)
        private set

    private var cargaJob: Job? = null

    fun seleccionarMes(mes: Meses) {
        if (_mesSeleccionadoUi.value == mes) {
            return
        }
        _mesSeleccionadoUi.value = mes
        // Drop any in-flight request and stale data from the previous month
        cargaJob?.cancel()
        cargaJob = null
        _caja.value = emptyCaja()
        _mesCargado.value = null
        cajaUiState = CajaUiState.Idle
    }

    fun getCajaPorMes() {
        val mes = _mesSeleccionadoUi.value ?: return

        cargaJob?.cancel()
        cajaUiState = CajaUiState.Loading
        _caja.value = emptyCaja()
        _mesCargado.value = null

        cargaJob = viewModelScope.launch {
            val result = cajaRepository.getCajaPorMes(mes.numero)
            ensureActive()
            if (_mesSeleccionadoUi.value != mes) return@launch

            when (result) {
                is CajaResult.Success -> {
                    _caja.value = result.data
                    _mesCargado.value = mes
                    cajaUiState = if (result.data.caja.isNullOrEmpty()) {
                        CajaUiState.Empty
                    } else {
                        CajaUiState.Success(R.string.datos_cargados_success)
                    }
                }
                is CajaResult.Error -> {
                    cajaUiState = CajaUiState.Error(result.messageRes, result.args)
                }
            }
        }
    }

    private companion object {
        const val PRODUCTO_DESCONOCIDO = "Producto Desconocido"
        fun emptyCaja() = DataCajaResponse(success = false, caja = emptyList())
    }
}
