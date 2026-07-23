package com.example.sodappcomposse.Caja

import com.example.sodappcomposse.API.ApiServices
import retrofit2.Response
import javax.inject.Inject

interface CajaRepository {
    suspend fun getCajaPorMes(mes: Int): Response<DataCajaResponse>
}

class CajaRepositoryImpl @Inject constructor(
    private val apiServices: ApiServices
) : CajaRepository {
    override suspend fun getCajaPorMes(mes: Int) = apiServices.getCajaPorMes(mes)
}
