package com.example.sodappcomposse.Producto

import com.example.sodappcomposse.API.ApiServices
import com.example.sodappcomposse.API.PostResponse
import retrofit2.Response
import javax.inject.Inject

interface ProductoRepository {
    suspend fun getProductos(): Response<ProductoResponse>
    suspend fun getProductoByName(nombre: String): Response<ProductoResponseByName>
    suspend fun postProducto(productoRequest: ProductoRequest): Response<PostResponse>
    suspend fun updateProducto(nombre: String, precio: Double, cantidad: Int): Response<PostResponse>
    suspend fun deleteProducto(nombre: String): Response<PostResponse>
}

class ProductoRepositoryImpl @Inject constructor(
    private val apiServices: ApiServices
) : ProductoRepository {
    override suspend fun getProductos() = apiServices.getProductos()
    override suspend fun getProductoByName(nombre: String) = apiServices.getProductoByName(nombre)
    override suspend fun postProducto(productoRequest: ProductoRequest) = apiServices.postProducto(productoRequest)
    override suspend fun updateProducto(nombre: String, precio: Double, cantidad: Int) = 
        apiServices.updateProducto(nombre, precio, cantidad)
    override suspend fun deleteProducto(nombre: String) = apiServices.deleteProducto(nombre)
}
