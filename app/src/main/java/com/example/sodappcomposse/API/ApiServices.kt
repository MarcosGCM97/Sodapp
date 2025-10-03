package com.example.sodappcomposse.API

import com.example.sodappcomposse.Caja.DataCaja
import com.example.sodappcomposse.Cliente.Cliente
import com.example.sodappcomposse.Cliente.ClienteRequest
import com.example.sodappcomposse.Cliente.DiasEntrega
import com.example.sodappcomposse.Cliente.DiasEntregaByid
import com.example.sodappcomposse.Cliente.TodosLosDias
import com.example.sodappcomposse.Producto.Producto
import com.example.sodappcomposse.Producto.ProductoRequest
import com.example.sodappcomposse.IngresoUsuario.UsuarioRequest
import com.example.sodappcomposse.Ventas.DataVenta
import com.example.sodappcomposse.Ventas.VentaApiResponse
import com.example.sodappcomposse.Ventas.VentaCompleta
import com.example.sodappcomposse.Ventas.VentaRequest
import retrofit2.Response // Para manejar la respuesta completa, incluyendo el código de estado
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.PUT

//TOKEN PARA GITHUB EXPIRA EN &= DIAS DESDE EL 20/07/25
interface ApiServices {

    @POST("login.php")
    suspend fun login(
        @Body usuarioRequest: UsuarioRequest
    ): Response<UsuarioResponse>

    //@GET("getUsuario.php")
    //suspend fun getUsuario(): Response<Usuario>

    @GET("getClientes.php")
    suspend fun getClientes(): Response<List<Cliente>>

    @GET("getClienteById.php")
    suspend fun getClienteById(@Query("id") id: Int): Response<Cliente>

    @DELETE("deleteCliente.php")
    suspend fun deleteCliente(@Query("id") id: Int): Response<PostResponse>

    @POST("postCliente.php")
    suspend fun postCliente(
        @Body clienteRequest: ClienteRequest
    ): Response<PostResponse>

    @PUT("updateCliente.php")
    suspend fun updateCliente(
        @Query("id") id: Int,
        @Query("nombre") nombre: String,
        @Query("direccion") direccion: String,
        @Query("telefono") telefono: String
    ): Response<PostResponse>

    @GET("getProductos.php")
    suspend fun getProductos(): Response<List<Producto>>

    @GET("getProductoByName.php")
    suspend fun getProductoByName(@Query("nombre") nombre: String): Response<Producto>

    @DELETE("deleteProducto.php")
    suspend fun deleteProducto(@Query("nombre") nombre: String): Response<PostResponse>

    @POST("postProducto.php")
    suspend fun postProducto(
        @Body productoRequest: ProductoRequest
    ): Response<PostResponse>

    @PUT("updateProducto.php")
    suspend fun updateProducto(
        @Query("nombre") nombre: String,
        @Query("precio") precio: Double,
        @Query("cantidad") cantidad: Int
    ): Response<PostResponse>

    @GET("getVentas.php")
    suspend fun getVentas(): Response<List<DataVenta>>

    @GET("getVentasByClienteId.php")
    suspend fun getVentasByCienteId(@Query("id") id: Int): Response<VentaApiResponse>

    @POST("postVenta.php")
    suspend fun postVenta(
        @Body ventaRequest: VentaRequest
    ): Response<PostResponse>

    @DELETE("deleteVenta.php")
    suspend fun deleteVenta(
        @Query("id") id: Int,
        //@Query("cantidad") cantidad: Int
    ): Response<PostResponse>

    @PUT("updateDeudaCliente.php")
    suspend fun updateDeudaCliente(
        @Query("id") id: Int,
        @Query("deuda") deuda: Double
    ): Response<PostResponse>

    @GET("getCajaPorMes.php")
    suspend fun getCajaPorMes(@Query("mes") mes: Int): Response<DataCaja>

    @GET("getDiasEntrega.php")
    suspend fun getDiasEntrega(): Response<TodosLosDias>

    @GET("getDiasEntregaByID.php")
    suspend fun getDiasEntregaById(@Query("id") id: Int): Response<DiasEntregaByid>

    @PUT("updateDiasEntrega.php")
    suspend fun updateDiasEntrega(
        @Body diasEntrega: DiasEntrega
    ): Response<PostResponse>
}

