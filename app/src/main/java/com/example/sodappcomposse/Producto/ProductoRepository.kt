package com.example.sodappcomposse.Producto

import com.example.sodappcomposse.API.ApiServices
import com.example.sodappcomposse.API.PostResponse
import retrofit2.Response
import javax.inject.Inject

interface ProductoRepository {
    suspend fun getProductos(forceRefresh: Boolean = false): Response<ProductoResponse>
    suspend fun getProductoByName(nombre: String): Response<ProductoResponseByName>
    suspend fun postProducto(productoRequest: ProductoRequest): Response<PostResponse>
    suspend fun updateProducto(nombre: String, precio: Double, cantidad: Int): Response<PostResponse>
    suspend fun deleteProducto(nombre: String): Response<PostResponse>
    fun clearCache()
}

class ProductoRepositoryImpl @Inject constructor(
    private val apiServices: ApiServices
) : ProductoRepository {
    private var cachedProductos: Response<ProductoResponse>? = null

    override suspend fun getProductos(forceRefresh: Boolean): Response<ProductoResponse> {
        if (forceRefresh || cachedProductos == null || !cachedProductos!!.isSuccessful) {
            cachedProductos = apiServices.getProductos()
        }
        return cachedProductos!!
    }

    override suspend fun getProductoByName(nombre: String) = apiServices.getProductoByName(nombre)
    
    override suspend fun postProducto(productoRequest: ProductoRequest): Response<PostResponse> {
        val response = apiServices.postProducto(productoRequest)
        if (response.isSuccessful) clearCache()
        return response
    }

    override suspend fun updateProducto(nombre: String, precio: Double, cantidad: Int): Response<PostResponse> {
        val response = apiServices.updateProducto(nombre, precio, cantidad)
        if (response.isSuccessful) clearCache()
        return response
    }

    override suspend fun deleteProducto(nombre: String): Response<PostResponse> {
        val response = apiServices.deleteProducto(nombre)
        if (response.isSuccessful) clearCache()
        return response
    }

    override fun clearCache() {
        cachedProductos = null
    }
}
