package com.example.sodappcomposse.Ventas

import com.example.sodappcomposse.API.ApiServices
import com.example.sodappcomposse.API.PostResponse
import retrofit2.Response
import javax.inject.Inject

interface VentaRepository {
    suspend fun getVentas(usuarioId: String): Response<VentaApiResponse>
    suspend fun getVentasByClienteId(id: Int, usuarioId: String): Response<VentaApiResponseById>
    suspend fun postVenta(ventaRequest: VentaRequest): Response<PostResponse>
    suspend fun deleteVenta(id: Int?): Response<PostResponse>
}

class VentaRepositoryImpl @Inject constructor(
    private val apiServices: ApiServices
) : VentaRepository {
    override suspend fun getVentas(usuarioId: String) = apiServices.getVentas(usuarioId)
    override suspend fun getVentasByClienteId(id: Int, usuarioId: String) = apiServices.getVentasByClienteId(id, usuarioId)
    override suspend fun postVenta(ventaRequest: VentaRequest) = apiServices.postVenta(ventaRequest)
    override suspend fun deleteVenta(id: Int?) = apiServices.deleteVenta(id)
}
