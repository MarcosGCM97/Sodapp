package com.example.sodappcomposse.API

import com.example.sodappcomposse.Caja.DataCajaResponse
import com.example.sodappcomposse.Cliente.ClienteRequest
import com.example.sodappcomposse.Cliente.ClienteResponse
import com.example.sodappcomposse.Cliente.ClienteResponseById
import com.example.sodappcomposse.Cliente.DiasEntrega
import com.example.sodappcomposse.Cliente.DiasEntregaByid
import com.example.sodappcomposse.Cliente.TodosLosDias
import com.example.sodappcomposse.Producto.ProductoRequest
import com.example.sodappcomposse.IngresoUsuario.UsuarioRequest
import com.example.sodappcomposse.Producto.ProductoResponse
import com.example.sodappcomposse.Producto.ProductoResponseByName
import com.example.sodappcomposse.Ventas.VentaApiResponse
import com.example.sodappcomposse.Ventas.VentaApiResponseById
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

    @POST("api_2/login.php")
    suspend fun login(
        @Body usuarioRequest: UsuarioRequest
    ): Response<UsuarioResponse>

    //@GET("getUsuario.php")
    //suspend fun getUsuario(): Response<Usuario>

    //{"cl_ide":"14","cl_nom":"Agust\u00edn brocca","cl_dir":"s\/n","cl_tel":"3513476902","cl_deb":"4500","cl_emp":"Ariza"}
    @GET("api_2/clientes.php")
    suspend fun getClientes(): Response<ClienteResponse>

    @GET("api_2/clientes.php")
    suspend fun getClienteById(@Query("id") id: Int): Response<ClienteResponseById>

    @DELETE("api_2/clientes.php")
    suspend fun eliminarCliente(@Query("id") id: Int): Response<PostResponse>

    @POST("api_2/clientes.php")
    suspend fun postCliente(
        @Body clienteRequest: ClienteRequest
    ): Response<PostResponse>

    @PUT("api_2/clientes.php")
    suspend fun updateCliente(
        @Query("id") id: Int,
        @Query("nombre") nombre: String,
        @Query("direccion") direccion: String,
        @Query("telefono") telefono: String
    ): Response<PostResponse>

    /*"pr_ide":"1","pr_nom":"vidon de agua","pr_val":"4500.00","pr_stk":"1","pr_emp":"Ariza","created_at":null,"updated_at":null*/
    @GET("api_2/productos.php")
    suspend fun getProductos(): Response<ProductoResponse>

    @GET("api_2/productos.php")
    suspend fun getProductoByName(@Query("nombre") nombre: String): Response<ProductoResponseByName>

    @DELETE("api_2/productos.php")
    suspend fun deleteProducto(@Query("nombre") nombre: String): Response<PostResponse>

    @POST("api_2/productos.php")
    suspend fun postProducto(
        @Body productoRequest: ProductoRequest
    ): Response<PostResponse>

    @PUT("api_2/productos.php")
    suspend fun updateProducto(
        @Query("nombre") nombre: String,
        @Query("precio") precio: Double,
        @Query("cantidad") cantidad: Int
    ): Response<PostResponse>

/*cl.cl_nom, cl.cl_dir, cl.cl_tel, pr.pr_nom, pr.pr_val, vt.vt_can, vt.vt_fec, vt.vt_ide*/
    @GET("api_2/ventas.php")
    suspend fun getVentas(
        @Query("usuarioId") usuarioId: String
    ): Response<VentaApiResponse>

    @GET("api_2/ventas.php")
    suspend fun getVentasByClienteId(
        @Query("id") id: Int,
        @Query("usuarioId") usuarioId: String
    ): Response<VentaApiResponseById>

    @POST("api_2/ventas.php")
    suspend fun postVenta(
        @Body ventaRequest: VentaRequest
    ): Response<PostResponse>

    @DELETE("api_2/ventas.php")
    suspend fun deleteVenta(
        @Query("id") id: Int?,
        //@Query("cantidad") cantidad: Int
    ): Response<PostResponse>

    @PUT("api_2/deudaCliente.php")
    suspend fun updateDeudaCliente(
        @Query("id") id: Int,
        @Query("deuda") deuda: Double?
    ): Response<PostResponse>

    @GET("api_2/cajaMes.php")
    suspend fun getCajaPorMes(@Query("mes") mes: Int): Response<DataCajaResponse>

    @GET("api_2/dias.php")
    suspend fun getDiasEntrega(): Response<TodosLosDias>

    @GET("api_2/dias.php")
    suspend fun getDiasEntregaById(@Query("id") id: Int): Response<DiasEntregaByid>

    @PUT("api_2/dias.php")
    suspend fun updateDiasEntrega(
        @Body diasEntrega: DiasEntrega
    ): Response<PostResponse>
}



