package com.example.sodappcomposse.API

import com.example.sodappcomposse.Cliente.Cliente
import com.example.sodappcomposse.Cliente.ClienteRequest
import com.example.sodappcomposse.Producto.Producto
import com.example.sodappcomposse.Producto.ProductoRequest
import com.example.sodappcomposse.UsuarioRequest
import com.example.sodappcomposse.Ventas.DataVenta
import com.example.sodappcomposse.Ventas.VentaRequest
import retrofit2.Response // Para manejar la respuesta completa, incluyendo el código de estado
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiServices {

    @POST("login.php")
    suspend fun login(
        @Body usuarioRequest: UsuarioRequest
    ): Response<UsuarioResponse>

    //@GET("getUsuario.php")
    //suspend fun getUsuario(): Response<Usuario>

    @GET("getClientes.php")
    suspend fun getClientes(): Response<List<Cliente>>

    @GET("clientes/{id}")
    suspend fun getCliente(@Path("id") id: Int): Response<Cliente>

    @POST("postCliente.php")
    suspend fun postCliente(
        @Body clienteRequest: ClienteRequest
    ): Response<PostResponse>

    @GET("getProductos.php")
    suspend fun getProductos(): Response<List<Producto>>

    @GET("productos/{id}")
    suspend fun getProducto(@Path("id") id: Int): Response<Producto>

    @POST("postProducto.php")
    suspend fun postProducto(
        @Body productoRequest: ProductoRequest
    ): Response<PostResponse>

    @GET("getVentas.php")
    suspend fun getVentas(): Response<List<DataVenta>>

    @GET("ventas/{id}")
    suspend fun getVenta(@Path("id") id: Int): Response<DataVenta>

    @POST("postVenta.php")
    suspend fun postVenta(
        @Body ventaRequest: VentaRequest
    ): Response<PostResponse>
}

