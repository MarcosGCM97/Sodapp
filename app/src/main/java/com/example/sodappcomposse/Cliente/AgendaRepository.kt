package com.example.sodappcomposse.Cliente

import com.example.sodappcomposse.API.ApiServices
import com.example.sodappcomposse.API.PostResponse
import retrofit2.Response
import javax.inject.Inject

interface AgendaRepository {
    suspend fun getDiasEntrega(): Response<TodosLosDias>
    suspend fun getDiasEntregaById(id: Int): Response<DiasEntregaByid>
    suspend fun updateDiasEntrega(diasEntrega: DiasEntrega): Response<PostResponse>
}

class AgendaRepositoryImpl @Inject constructor(
    private val apiServices: ApiServices
) : AgendaRepository {
    override suspend fun getDiasEntrega() = apiServices.getDiasEntrega()
    override suspend fun getDiasEntregaById(id: Int) = apiServices.getDiasEntregaById(id)
    override suspend fun updateDiasEntrega(diasEntrega: DiasEntrega) = apiServices.updateDiasEntrega(diasEntrega)
}
