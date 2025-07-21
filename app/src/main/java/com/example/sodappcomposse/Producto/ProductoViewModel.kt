package com.example.sodappcomposse.Producto

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sodappcomposse.API.ApiServices
import com.example.sodappcomposse.API.RetrofitInstance
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

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

class ProductoViewModel(
    private val apiServices: ApiServices = RetrofitInstance.api
) : ViewModel() {
    private val TAG = "ProductoViewModel"

    private val _productos = mutableStateListOf<Producto>()
    val productos: List<Producto> = _productos // Exponer como lista inmutable (pero observable)

    var productoUiState: ProductoUiState by mutableStateOf(ProductoUiState.Idle)
        private set // Solo modificable desde el ViewModel

    internal fun getProductos(){
        viewModelScope.launch {
            productoUiState = ProductoUiState.Loading // Es buena practica
            try {
                val response = apiServices.getProductos()

                if(response.isSuccessful){
                    val productosApi = response.body()!!
                    if(response.body() !== null){
                        _productos.clear()
                        _productos.addAll(productosApi)
                        productoUiState = ProductoUiState.Success("Productos cargados: ${_productos.size}")
                    }else{
                        Log.e(TAG, "Respuesta exitosa pero cuerpo nulo.")
                    }
                }else{
                    //Manejar error de la API
                    Log.e(TAG, "Error en la respuesta: ${response.code()} - ${response.message()}")
                }

            }catch (e: HttpException) {
                Log.e(TAG, "Error HTTP en la solicitud: ${e.code()} - ${e.message()}", e)
                productoUiState = ProductoUiState.Error("Error HTTP: ${e.message()}")
            }catch (e: IOException) {
                Log.e(TAG, "Error de Red/IO en la solicitud: ${e.message}", e)
                productoUiState = ProductoUiState.Error("Error de Red: Verifica tu conexión.")
            }catch (e: Exception) {
                Log.e(TAG, "Error general en la solicitud: ${e.message}", e)
                productoUiState = ProductoUiState.Error("Error inesperado: ${e.message?.take(100)}")

            }
        }
    }

    private val _addProductoUiState = mutableStateOf<AddProductoUiState>(AddProductoUiState.Idle)
    val addProductoUiState: State<AddProductoUiState> = _addProductoUiState

    internal fun agregarNuevoProducto(nombre: String, precio: String, cantidad: Int) {
        if (nombre.isBlank() || precio.isBlank() || cantidad.toString().isBlank()) {
            _addProductoUiState.value = AddProductoUiState.Error("Todos los campos son requeridos.")
            return
        }

        _addProductoUiState.value = AddProductoUiState.Loading
        viewModelScope.launch {
            try {
                var objProducto = ProductoRequest(
                    nombrePr = nombre,
                    precioPr = precio,
                    cantidadPr = cantidad
                )
                val response = apiServices.postProducto(objProducto)
                if (response.isSuccessful && response.body() != null){
                    val productos = response.body()!!
                    //Procesar la lista de productos
                    _addProductoUiState.value = AddProductoUiState.Success("Producto '$nombre' guardado exitosamente.")

                    getProductos()
                }
                else{
                    //Manejar error de la API
                    Log.e(TAG, "Error en la respuesta: ${response.code()} - ${response.message()}")
                    _addProductoUiState.value = AddProductoUiState.Error("Error en la respuesta: ${response.code()} - ${response.message()}")
                }

            } catch (e: Exception) {
                Log.e("ProductosViewModel", "Error al guardar producto: ${e.message}", e)
                _addProductoUiState.value = AddProductoUiState.Error("Error al guardar: ${e.message}")
            } catch (e: HttpException) {
                Log.e("ProductosViewModel", "Error HTTP al guardar producto: ${e.code()} - ${e.message()}", e)
            }
        }
    }

    fun resetAddProductoState() {
        _addProductoUiState.value = AddProductoUiState.Idle
    }

    private val _productoSeleccionadoParaEdicion = mutableStateOf<Producto?>(null) // Asume que tienes una data class Producto
    val productoSeleccionadoParaEdicion: State<Producto?> = _productoSeleccionadoParaEdicion

    // Función para actualizar el producto seleccionado
    fun seleccionarProductoParaEdicion(producto: Producto?) {
        _productoSeleccionadoParaEdicion.value = producto
        viewModelScope.launch {
            // Lógica para llamar a tu API o base de datos para actualizar el producto
            Log.d("ProductosViewModel", "Actualizando producto: ${producto?.nombrePr}")
        }
    }
}