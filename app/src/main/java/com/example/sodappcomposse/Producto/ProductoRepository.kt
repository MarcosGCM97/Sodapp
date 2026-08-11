package com.example.sodappcomposse.Producto

import com.example.sodappcomposse.API.ApiServices
import com.example.sodappcomposse.API.PostResponse
import com.example.sodappcomposse.R
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

sealed class ProductoResult<out T> {
    data class Success<T>(val data: T) : ProductoResult<T>()
    data class Error(val messageRes: Int, val args: Array<Any> = emptyArray()) : ProductoResult<Nothing>() {
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

interface ProductoRepository {
    suspend fun getProductos(forceRefresh: Boolean = false): ProductoResult<ProductoResponse>
    suspend fun getProductoByName(nombre: String): ProductoResult<ProductoResponseByName>
    suspend fun postProducto(productoRequest: ProductoRequest): ProductoResult<PostResponse>
    suspend fun updateProducto(nombre: String, precio: Double, cantidad: Int): ProductoResult<PostResponse>
    suspend fun deleteProducto(nombre: String): ProductoResult<PostResponse>
    fun clearCache()
}

class ProductoRepositoryImpl @Inject constructor(
    private val apiServices: ApiServices
) : ProductoRepository {
    private var cachedProductos: ProductoResult<ProductoResponse>? = null

    override suspend fun getProductos(forceRefresh: Boolean): ProductoResult<ProductoResponse> {
        if (forceRefresh || cachedProductos == null || cachedProductos is ProductoResult.Error) {
            cachedProductos = handleApiCall { apiServices.getProductos() }
        }
        return cachedProductos!!
    }

    override suspend fun getProductoByName(nombre: String): ProductoResult<ProductoResponseByName> = handleApiCall {
        apiServices.getProductoByName(nombre)
    }

    override suspend fun postProducto(productoRequest: ProductoRequest): ProductoResult<PostResponse> {
        val result = handleApiCall { apiServices.postProducto(productoRequest) }
        if (result is ProductoResult.Success) clearCache()
        return result
    }

    override suspend fun updateProducto(nombre: String, precio: Double, cantidad: Int): ProductoResult<PostResponse> {
        val result = handleApiCall { apiServices.updateProducto(nombre, precio, cantidad) }
        if (result is ProductoResult.Success) clearCache()
        return result
    }

    override suspend fun deleteProducto(nombre: String): ProductoResult<PostResponse> {
        val result = handleApiCall { apiServices.deleteProducto(nombre) }
        if (result is ProductoResult.Success) clearCache()
        return result
    }

    override fun clearCache() {
        cachedProductos = null
    }

    private suspend fun <T> handleApiCall(call: suspend () -> retrofit2.Response<T>): ProductoResult<T> {
        return try {
            val response = call()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    ProductoResult.Success(body)
                } else {
                    ProductoResult.Error(R.string.cuerpo_nulo_error)
                }
            } else {
                ProductoResult.Error(R.string.error_servidor_format, arrayOf(response.code()))
            }
        } catch (e: IOException) {
            ProductoResult.Error(R.string.error_red_verificar)
        } catch (e: HttpException) {
            ProductoResult.Error(R.string.error_http_format, arrayOf(e.code()))
        } catch (e: Exception) {
            ProductoResult.Error(R.string.error_inesperado_format, arrayOf(e.message?.take(100) ?: ""))
        }
    }
}
