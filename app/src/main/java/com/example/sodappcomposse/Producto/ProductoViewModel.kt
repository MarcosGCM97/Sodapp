package com.example.sodappcomposse.Producto

import androidx.annotation.StringRes
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sodappcomposse.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

sealed class ProductoUiState{
    object Idle : ProductoUiState() //Estado inicial
    object Loading : ProductoUiState() //Cargando
    data class Error(@StringRes val messageRes: Int, val args: Array<Any> = emptyArray()) : ProductoUiState() {
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
    data class Success(@StringRes val messageRes: Int, val args: Array<Any> = emptyArray()) : ProductoUiState() {
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

sealed interface AddProductoUiState {
    object Idle : AddProductoUiState
    object Loading : AddProductoUiState
    data class Success(@StringRes val messageRes: Int, val args: Array<Any> = emptyArray()) : AddProductoUiState {
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
    data class Error(@StringRes val messageRes: Int, val args: Array<Any> = emptyArray()) : AddProductoUiState {
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
}

@HiltViewModel
class ProductoViewModel @Inject constructor(
    private val productoRepository: ProductoRepository
) : ViewModel() {
    private val TAG = "ProductoViewModel"

    private val _productos = mutableStateListOf<Producto>()
    val productos: List<Producto> = _productos // Exponer como lista inmutable (pero observable)

    var productoUiState: ProductoUiState by mutableStateOf(ProductoUiState.Idle)
        private set // Solo modificable desde el ViewModel

    val productosParaDropDown : MutableState<Producto?> = mutableStateOf(null)

    var productoByName: MutableState<Producto?> = mutableStateOf(null)
    val productoStateByName: State<Producto?> = productoByName

    fun getProductoByName(nombrePr: String) {

        productoByName = mutableStateOf(
            productos.find {
                it.nombrePr == nombrePr
            }
        )
    }

    internal fun getProductos(){
        viewModelScope.launch {
            productoUiState = ProductoUiState.Loading // Es buena practica
            when (val result = productoRepository.getProductos()) {
                is ProductoResult.Success -> {
                    _productos.clear()
                    _productos.addAll(result.data.productos)
                    
                    // Sincronizar el producto seleccionado para el DropDown/Card
                    val seleccionado = productosParaDropDown.value
                    if (seleccionado != null) {
                        productosParaDropDown.value = _productos.find { it.nombrePr == seleccionado.nombrePr }
                    }
                    
                    productoUiState = ProductoUiState.Success(R.string.productos_cargados_format, arrayOf(_productos.size))
                }
                is ProductoResult.Error -> {
                    productoUiState = ProductoUiState.Error(result.messageRes, result.args)
                }
            }
        }
    }

    private val _addProductoUiState = mutableStateOf<AddProductoUiState>(AddProductoUiState.Idle)
    val addProductoUiState: State<AddProductoUiState> = _addProductoUiState

    internal fun agregarNuevoProducto(nombre: String, precio: String, cantidad: Int) {
        if (nombre.isBlank() || precio.isBlank()) {
            _addProductoUiState.value = AddProductoUiState.Error(R.string.campos_requeridos_error)
            return
        }

        val precioDouble = precio.toDoubleOrNull()
        if (precioDouble == null || precioDouble <= 0) {
            _addProductoUiState.value = AddProductoUiState.Error(R.string.error_precio_invalido)
            return
        }
        if (cantidad < 0) {
            _addProductoUiState.value = AddProductoUiState.Error(R.string.error_cantidad_negativa)
            return
        }

        _addProductoUiState.value = AddProductoUiState.Loading
        viewModelScope.launch {
            val objProducto = ProductoRequest(
                nombrePr = nombre,
                precioUni = precioDouble,
                stock = cantidad
            )
            when (val result = productoRepository.postProducto(objProducto)) {
                is ProductoResult.Success -> {
                    _addProductoUiState.value = AddProductoUiState.Success(R.string.producto_guardado_success, arrayOf(nombre))
                    getProductos()
                }
                is ProductoResult.Error -> {
                    _addProductoUiState.value = AddProductoUiState.Error(result.messageRes, result.args)
                }
            }
        }
    }

    fun resetAddProductoState() {
        _addProductoUiState.value = AddProductoUiState.Idle
    }

    private val _productoSeleccionadoParaEdicion = mutableStateOf<Producto?>(null) // Asume que tienes una data class Producto
    val productoSeleccionadoParaEdicion: State<Producto?> = _productoSeleccionadoParaEdicion

    // Función para actualizar el producto seleccionado
    fun editarProducto(producto: Producto?) {
        if (producto == null) return
        _productoSeleccionadoParaEdicion.value = producto
        viewModelScope.launch {
            when (val result = productoRepository.updateProducto(producto.nombrePr, producto.precioUni, producto.stock)) {
                is ProductoResult.Success -> {
                    productoUiState = ProductoUiState.Success(R.string.producto_actualizado_success)
                    getProductos()
                }
                is ProductoResult.Error -> {
                    productoUiState = ProductoUiState.Error(result.messageRes, result.args)
                }
            }
        }
    }

    fun eliminarProducto(producto: Producto) {
        viewModelScope.launch {
            when (val result = productoRepository.deleteProducto(producto.nombrePr)) {
                is ProductoResult.Success -> {
                    productoUiState = ProductoUiState.Success(R.string.producto_eliminado_success)
                    getProductos()
                }
                is ProductoResult.Error -> {
                    productoUiState = ProductoUiState.Error(result.messageRes, result.args)
                }
            }
        }
    }

    //control de stock
    companion object {
        const val STOCK_BAJO_UMBRAL = 5
        const val STOCK_MEDIO_UMBRAL = 10
    }

    fun ajustarStock(producto: Producto, nuevaCantidad: Int) {
        val productoParaEditar = Producto(
            nombrePr = producto.nombrePr,
            precioUni = producto.precioUni,
            stock = nuevaCantidad
        )
        editarProducto(productoParaEditar)
    }
}
