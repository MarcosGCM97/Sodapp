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
            try {
                val response = productoRepository.getProductos()

                if(response.isSuccessful){
                    response.body()?.let { productosApi ->
                        _productos.clear()
                        _productos.addAll(productosApi.productos)
                        productoUiState = ProductoUiState.Success(R.string.productos_cargados_format, arrayOf(_productos.size))
                    } ?: run {
                        productoUiState = ProductoUiState.Error(R.string.cuerpo_nulo_error)
                    }
                } else {
                    productoUiState = ProductoUiState.Error(R.string.error_servidor_format, arrayOf(response.code()))
                }
            } catch (e: IOException) {
                productoUiState = ProductoUiState.Error(R.string.error_red_verificar)
            } catch (e: HttpException) {
                productoUiState = ProductoUiState.Error(R.string.error_http_format, arrayOf(e.code()))
            } catch (e: Exception) {
                productoUiState = ProductoUiState.Error(R.string.error_inesperado_format, arrayOf(e.message?.take(100) ?: ""))
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
            try {
                val objProducto = ProductoRequest(
                    nombrePr = nombre,
                    precioUni = precioDouble,
                    stock = cantidad
                )
                val response = productoRepository.postProducto(objProducto)
                if (response.isSuccessful && response.body() != null) {
                    _addProductoUiState.value = AddProductoUiState.Success(R.string.producto_guardado_success, arrayOf(nombre))
                    getProductos()
                } else {
                    _addProductoUiState.value = AddProductoUiState.Error(R.string.error_respuesta_format, arrayOf(response.code(), response.message()))
                }
            } catch (e: IOException) {
                _addProductoUiState.value = AddProductoUiState.Error(R.string.error_red_format, arrayOf(e.message ?: ""))
            } catch (e: HttpException) {
                _addProductoUiState.value = AddProductoUiState.Error(R.string.error_http_format, arrayOf(e.code()))
            } catch (e: Exception) {
                _addProductoUiState.value = AddProductoUiState.Error(R.string.error_guardar_producto, arrayOf(e.message ?: ""))
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
            try {
                val response = productoRepository.updateProducto(producto.nombrePr, producto.precioUni, producto.stock)
                if (response.isSuccessful) {
                    productoUiState = ProductoUiState.Success(R.string.producto_actualizado_success)
                    getProductos()
                } else {
                    productoUiState = ProductoUiState.Error(R.string.error_actualizar_producto_format, arrayOf(response.code(), response.message()))
                }
            } catch (e: IOException) {
                productoUiState = ProductoUiState.Error(R.string.error_red_actualizar_producto, arrayOf(e.message ?: ""))
            } catch (e: HttpException) {
                productoUiState = ProductoUiState.Error(R.string.error_http_actualizar_producto, arrayOf(e.code()))
            } catch (e: Exception) {
                productoUiState = ProductoUiState.Error(R.string.error_inesperado_format, arrayOf(e.message ?: ""))
            }
        }
    }

    fun eliminarProducto(producto: Producto) {
        viewModelScope.launch {
            try {
                val response = productoRepository.deleteProducto(producto.nombrePr)
                if (response.isSuccessful) {
                    productoUiState = ProductoUiState.Success(R.string.producto_eliminado_success)
                    getProductos()
                } else {
                    productoUiState = ProductoUiState.Error(R.string.error_eliminar_producto_format, arrayOf(response.code(), response.message()))
                }
            } catch (e: IOException) {
                productoUiState = ProductoUiState.Error(R.string.error_red_eliminar_producto, arrayOf(e.message ?: ""))
            } catch (e: HttpException) {
                productoUiState = ProductoUiState.Error(R.string.error_http_eliminar_producto, arrayOf(e.code()))
            } catch (e: Exception) {
                productoUiState = ProductoUiState.Error(R.string.error_inesperado_format, arrayOf(e.message ?: ""))
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
