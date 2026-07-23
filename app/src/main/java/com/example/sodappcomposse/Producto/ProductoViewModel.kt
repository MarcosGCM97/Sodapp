package com.example.sodappcomposse.Producto

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sodappcomposse.API.RetrofitInstance
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

sealed class ProductoUiState{
    object Idle : ProductoUiState() //Estado inicial
    object Loading : ProductoUiState() //Cargando
    data class Error(val message: String) : ProductoUiState() //Error
    data class Success(val message: String) : ProductoUiState() //Éxito
}

sealed interface AddProductoUiState {
    object Idle : AddProductoUiState
    object Loading : AddProductoUiState
    data class Success(val message: String) : AddProductoUiState
    data class Error(val message: String) : AddProductoUiState
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
                        productoUiState = ProductoUiState.Success("Productos cargados: ${_productos.size}")
                    } ?: run {
                        productoUiState = ProductoUiState.Error("Respuesta exitosa pero cuerpo nulo.")
                    }
                } else {
                    productoUiState = ProductoUiState.Error("Error servidor: ${response.code()}")
                }
            } catch (e: IOException) {
                productoUiState = ProductoUiState.Error("Error de Red: Verifica tu conexión.")
            } catch (e: HttpException) {
                productoUiState = ProductoUiState.Error("Error HTTP: ${e.code()}")
            } catch (e: Exception) {
                productoUiState = ProductoUiState.Error("Error inesperado: ${e.message?.take(100)}")
            }
        }
    }

    private val _addProductoUiState = mutableStateOf<AddProductoUiState>(AddProductoUiState.Idle)
    val addProductoUiState: State<AddProductoUiState> = _addProductoUiState

    internal fun agregarNuevoProducto(nombre: String, precio: String, cantidad: Int) {
        if (nombre.isBlank() || precio.isBlank()) {
            _addProductoUiState.value = AddProductoUiState.Error("Todos los campos son requeridos.")
            return
        }

        val precioDouble = precio.toDoubleOrNull()
        if (precioDouble == null) {
            _addProductoUiState.value = AddProductoUiState.Error("Precio inválido.")
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
                    _addProductoUiState.value = AddProductoUiState.Success("Producto '$nombre' guardado exitosamente.")
                    getProductos()
                } else {
                    _addProductoUiState.value = AddProductoUiState.Error("Error en la respuesta: ${response.code()} - ${response.message()}")
                }
            } catch (e: IOException) {
                _addProductoUiState.value = AddProductoUiState.Error("Error de red: ${e.message}")
            } catch (e: HttpException) {
                _addProductoUiState.value = AddProductoUiState.Error("Error HTTP: ${e.code()}")
            } catch (e: Exception) {
                _addProductoUiState.value = AddProductoUiState.Error("Error al guardar: ${e.message}")
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
                    productoUiState = ProductoUiState.Success("Producto actualizado exitosamente.")
                    getProductos()
                } else {
                    productoUiState = ProductoUiState.Error("Error al actualizar producto: ${response.code()} - ${response.message()}")
                }
            } catch (e: IOException) {
                productoUiState = ProductoUiState.Error("Error de red al actualizar producto: ${e.message}")
            } catch (e: HttpException) {
                productoUiState = ProductoUiState.Error("Error HTTP al actualizar producto: ${e.code()}")
            } catch (e: Exception) {
                productoUiState = ProductoUiState.Error("Error al actualizar producto: ${e.message}")
            }
        }
    }

    fun eliminarProducto(producto: Producto) {
        viewModelScope.launch {
            try {
                val response = productoRepository.deleteProducto(producto.nombrePr)
                if (response.isSuccessful) {
                    _productos.remove(producto)
                    productoUiState = ProductoUiState.Success("Producto eliminado exitosamente.")
                    getProductos()
                } else {
                    productoUiState = ProductoUiState.Error("Error al eliminar producto: ${response.code()} - ${response.message()}")
                }
            } catch (e: IOException) {
                productoUiState = ProductoUiState.Error("Error de red al eliminar producto: ${e.message}")
            } catch (e: HttpException) {
                productoUiState = ProductoUiState.Error("Error HTTP al eliminar producto: ${e.code()}")
            } catch (e: Exception) {
                productoUiState = ProductoUiState.Error("Error al eliminar producto: ${e.message}")
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