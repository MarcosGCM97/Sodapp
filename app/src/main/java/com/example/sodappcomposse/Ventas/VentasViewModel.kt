package com.example.sodappcomposse.Ventas

import androidx.annotation.StringRes
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sodappcomposse.Cliente.ClienteRepository
import com.example.sodappcomposse.Cliente.ClienteResult
import com.example.sodappcomposse.Producto.Producto
import com.example.sodappcomposse.Producto.ProductoRepository
import com.example.sodappcomposse.Producto.ProductoResult
import com.example.sodappcomposse.Producto.ProductoVenta
import com.example.sodappcomposse.R
import com.example.sodappcomposse.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow as KStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn

sealed class VentasUiState{
    object Idle: VentasUiState()
    object Loading: VentasUiState()
    data class Error(@StringRes val messageRes: Int, val args: Array<Any> = emptyArray()): VentasUiState() {
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
    data class Success(@StringRes val messageRes: Int, val args: Array<Any> = emptyArray()): VentasUiState() {
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

@HiltViewModel
class VentasViewModel @Inject constructor(
    private val ventaRepository: VentaRepository,
    private val clienteRepository: ClienteRepository,
    private val productoRepository: ProductoRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    private val TAG = "VentasViewModel"

    private val _ventas = MutableStateFlow<List<Venta>>(emptyList())
    val ventas: StateFlow<List<Venta>> = _ventas.asStateFlow()

    private val _productos = MutableStateFlow<List<Producto>>(emptyList())

    val ventasAgrupadas: StateFlow<List<VentaAgrupada>> = combine(_ventas, _productos) { sales, products ->
        if (sales.isEmpty()) {
            emptyList<VentaAgrupada>()
        } else {
            val groupedByClienteAndFecha = sales.groupBy {
                Pair(it.nombreClienteDisplay, it.fecha.substringBefore(" "))
            }

            groupedByClienteAndFecha.map { (clienteFechaPair, ventasDelGrupo) ->
                val cliente = ventasDelGrupo.first().cliente
                val fecha = clienteFechaPair.second

                val productosSumados = ventasDelGrupo
                    .groupBy { it.producto.ifBlank { "Producto Desconocido" } }
                    .map { (nombreProducto, itemsProducto) ->
                        val fallbackPrecio = itemsProducto.firstOrNull()?.precio ?: 0.0
                        ProductoVenta(
                            nombre = nombreProducto,
                            cantidad = itemsProducto.sumOf { it.cantidad },
                            precio = products.find { it.nombrePr == nombreProducto }?.precioUni ?: fallbackPrecio
                        )
                    }

                val cantidadTotalDeEstaVenta = productosSumados.sumOf { it.cantidad }
                val montoTotalDeEstaVenta = productosSumados.sumOf { it.cantidad * (it.precio ?: 0.0) }

                VentaAgrupada(
                    cliente = cliente,
                    fecha = fecha,
                    productos = productosSumados,
                    cantidadTotalVenta = cantidadTotalDeEstaVenta,
                    montoTotalVenta = montoTotalDeEstaVenta
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var ventasUiState: VentasUiState by mutableStateOf(VentasUiState.Idle)
        private set


    private val _ventasPorClienteId = MutableStateFlow<List<Venta>>(emptyList())
    val ventasPorClienteId: StateFlow<List<Venta>> = _ventasPorClienteId.asStateFlow()

    fun getVentasByClienteId(idCl: String) {
        viewModelScope.launch {
            ventasUiState = VentasUiState.Loading

            // 1. Obtener el ID del usuario actual
            val idUsuario = userPreferencesRepository.userId.first() ?: "0"

            // 2. Pasar idUsuario a la llamada de la API
            when (val result = ventaRepository.getVentasByClienteId(idCl.toInt(), idUsuario)) {
                is VentaResult.Success -> {
                    _ventasPorClienteId.value = result.data.ventas
                    ventasUiState = VentasUiState.Success(R.string.ventas_cargadas_usuario_format, arrayOf(idUsuario))
                }
                is VentaResult.Error -> {
                    _ventasPorClienteId.value = emptyList()
                    ventasUiState = VentasUiState.Error(result.messageRes, result.args)
                }
            }
        }
    }

    internal fun getVentas() {
        viewModelScope.launch {
            ventasUiState = VentasUiState.Loading

            // Cargar productos para el mapeo de precios
            val productsResult = productoRepository.getProductos()
            if (productsResult is ProductoResult.Success) {
                _productos.value = productsResult.data.productos
            }

            // 1. Obtener el ID del usuario actual
            val idUsuario = userPreferencesRepository.userId.first() ?: "0"

            // 2. Llamar a la API enviando el usuario
            when (val result = ventaRepository.getVentas(idUsuario)) {
                is VentaResult.Success -> {
                    _ventas.value = result.data.ventas
                    ventasUiState = VentasUiState.Success(R.string.ventas_usuario_cargadas_format, arrayOf(idUsuario))
                }
                is VentaResult.Error -> {
                    ventasUiState = VentasUiState.Error(result.messageRes, result.args)
                }
            }
        }
    }

    internal suspend fun postVenta(clienteId: Int, productos: List<ProductoVenta>){
        val idUsuario = userPreferencesRepository.userId.first() ?: "0"
        val ventaParaApi = VentaRequest(clienteId, productos, idUsuario)

        when (val result = ventaRepository.postVenta(ventaParaApi)) {
            is VentaResult.Success -> {
                ventasUiState = VentasUiState.Success(R.string.venta_procesada_success)
            }
            is VentaResult.Error -> {
                ventasUiState = VentasUiState.Error(result.messageRes, result.args)
            }
        }
    }

    internal fun eliminarVenta(idVenta: Int, clienteId: Int, valorVenta: Double){
        viewModelScope.launch {
            when (val result = ventaRepository.deleteVenta(idVenta)) {
                is VentaResult.Success -> {
                    ventasUiState = VentasUiState.Success(R.string.venta_eliminada_success)
                    // actualizar deuda del cliente
                    when (val updateResult = clienteRepository.updateDeudaCliente(clienteId, valorVenta)) {
                        is ClienteResult.Success -> { /* Ok */ }
                        is ClienteResult.Error -> {
                            ventasUiState = VentasUiState.Error(updateResult.messageRes, updateResult.args)
                        }
                    }
                    getVentasByClienteId(clienteId.toString())
                }
                is VentaResult.Error -> {
                    ventasUiState = VentasUiState.Error(result.messageRes, result.args)
                }
            }
        }
    }
}
