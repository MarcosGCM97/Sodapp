package com.example.sodappcomposse.Caja

import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sodappcomposse.R
import com.example.sodappcomposse.Ventas.Venta
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

sealed class CajaUiState {
    object Idle : CajaUiState()
    object Loading : CajaUiState()
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

    private val _caja = MutableStateFlow<DataCajaResponse>(DataCajaResponse(success = false, caja = emptyList()))
    // Eliminada la exposición de 'caja' a la UI para desacoplar modelos de red

    val cajaTotales: StateFlow<CajaTotales> = _caja.map { response ->
        val items = response.caja ?: emptyList()
        val grouped = items.groupBy { it.producto.ifBlank { "Producto Desconocido" } }.mapValues { (_, ventas) ->
            val firstVenta = ventas.first()
            CantidadDeVentasPorProducto(
                producto = firstVenta.producto.ifBlank { "Producto Desconocido" },
                cantidad = ventas.sumOf { it.cantidad },
                precio = ventas.sumOf { it.precio * it.cantidad.toDouble() }
            )
        }
        val totalQty = items.sumOf { it.cantidad }
        val totalAmount = items.sumOf { it.precio * it.cantidad }

        CajaTotales(grouped, totalQty, totalAmount)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CajaTotales())

    private val _mesSeleccionadoUi = MutableStateFlow<Meses?>(null)
    val mesSeleccionadoUi: StateFlow<Meses?> = _mesSeleccionadoUi.asStateFlow()

    var cajaUiState: CajaUiState by mutableStateOf(CajaUiState.Idle)
        private set

    fun seleccionarMes(mes: Meses) {
        if (_mesSeleccionadoUi.value == mes) {
            return
        }
        _mesSeleccionadoUi.value = mes
    }

    fun getCajaPorMes() {
        val mesNum = _mesSeleccionadoUi.value?.numero ?: return

        cajaUiState = CajaUiState.Loading
        _caja.value = DataCajaResponse(success = false, caja = emptyList())

        viewModelScope.launch {
            try {
                val response = cajaRepository.getCajaPorMes(mesNum)

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        if (_mesSeleccionadoUi.value?.numero == mesNum) {
                            if (responseBody.success == true && !responseBody.caja.isNullOrEmpty()) {
                                _caja.value = responseBody
                                cajaUiState = CajaUiState.Success(R.string.datos_cargados_success)
                            } else {
                                cajaUiState = CajaUiState.Error(R.string.no_ventas_mes_error)
                            }
                        }
                    } else {
                        if (_mesSeleccionadoUi.value?.numero == mesNum) {
                            cajaUiState = CajaUiState.Error(R.string.cuerpo_nulo_error)
                        }
                    }
                } else {
                    if (_mesSeleccionadoUi.value?.numero == mesNum) {
                        cajaUiState = CajaUiState.Error(R.string.error_api_format, arrayOf(response.code()))
                    }
                }
            } catch (e: IOException) {
                if (_mesSeleccionadoUi.value?.numero == mesNum) {
                    cajaUiState = CajaUiState.Error(R.string.error_red_format, arrayOf(e.message?.take(100) ?: ""))
                }
            } catch (e: HttpException) {
                if (_mesSeleccionadoUi.value?.numero == mesNum) {
                    cajaUiState = CajaUiState.Error(R.string.error_http_format, arrayOf(e.code()))
                }
            } catch (e: Exception) {
                if (_mesSeleccionadoUi.value?.numero == mesNum) {
                    cajaUiState = CajaUiState.Error(R.string.error_inesperado_format, arrayOf(e.message?.take(100) ?: ""))
                }
            }
        }
    }
}
