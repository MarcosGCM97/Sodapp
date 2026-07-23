package com.example.sodappcomposse.Cliente

import com.example.sodappcomposse.API.ApiServices
import com.example.sodappcomposse.API.PostResponse
import retrofit2.Response
import javax.inject.Inject

interface ClienteRepository {
    suspend fun getClientes(): Response<ClienteResponse>
    suspend fun getClienteById(id: Int): Response<ClienteResponseById>
    suspend fun postCliente(clienteRequest: ClienteRequest): Response<PostResponse>
    suspend fun updateCliente(id: Int, nombre: String, direccion: String, telefono: String): Response<PostResponse>
    suspend fun eliminarCliente(id: Int): Response<PostResponse>
    suspend fun updateDeudaCliente(id: Int, deuda: Double?): Response<PostResponse>
}

class ClienteRepositoryImpl @Inject constructor(
    private val apiServices: ApiServices
) : ClienteRepository {
    override suspend fun getClientes() = apiServices.getClientes()
    override suspend fun getClienteById(id: Int) = apiServices.getClienteById(id)
    override suspend fun postCliente(clienteRequest: ClienteRequest) = apiServices.postCliente(clienteRequest)
    override suspend fun updateCliente(id: Int, nombre: String, direccion: String, telefono: String) = 
        apiServices.updateCliente(id, nombre, direccion, telefono)
    override suspend fun eliminarCliente(id: Int) = apiServices.eliminarCliente(id)
    override suspend fun updateDeudaCliente(id: Int, deuda: Double?) = apiServices.updateDeudaCliente(id, deuda)
}
