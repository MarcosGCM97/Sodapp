package com.example.sodappcomposse.Cliente

import com.example.sodappcomposse.API.ApiServices
import com.example.sodappcomposse.API.PostResponse
import retrofit2.Response
import javax.inject.Inject

interface ClienteRepository {
    suspend fun getClientes(forceRefresh: Boolean = false): Response<ClienteResponse>
    suspend fun getClienteById(id: Int): Response<ClienteResponseById>
    suspend fun postCliente(clienteRequest: ClienteRequest): Response<PostResponse>
    suspend fun updateCliente(id: Int, nombre: String, direccion: String, telefono: String): Response<PostResponse>
    suspend fun eliminarCliente(id: Int): Response<PostResponse>
    suspend fun updateDeudaCliente(id: Int, deuda: Double?): Response<PostResponse>
    fun clearCache()
}

class ClienteRepositoryImpl @Inject constructor(
    private val apiServices: ApiServices
) : ClienteRepository {
    private var cachedClientes: Response<ClienteResponse>? = null

    override suspend fun getClientes(forceRefresh: Boolean): Response<ClienteResponse> {
        if (forceRefresh || cachedClientes == null || !cachedClientes!!.isSuccessful) {
            cachedClientes = apiServices.getClientes()
        }
        return cachedClientes!!
    }

    override suspend fun getClienteById(id: Int) = apiServices.getClienteById(id)
    
    override suspend fun postCliente(clienteRequest: ClienteRequest): Response<PostResponse> {
        val response = apiServices.postCliente(clienteRequest)
        if (response.isSuccessful) clearCache()
        return response
    }

    override suspend fun updateCliente(id: Int, nombre: String, direccion: String, telefono: String): Response<PostResponse> {
        val response = apiServices.updateCliente(id, nombre, direccion, telefono)
        if (response.isSuccessful) clearCache()
        return response
    }

    override suspend fun eliminarCliente(id: Int): Response<PostResponse> {
        val response = apiServices.eliminarCliente(id)
        if (response.isSuccessful) clearCache()
        return response
    }

    override suspend fun updateDeudaCliente(id: Int, deuda: Double?): Response<PostResponse> {
        val response = apiServices.updateDeudaCliente(id, deuda)
        if (response.isSuccessful) clearCache()
        return response
    }

    override fun clearCache() {
        cachedClientes = null
    }
}
