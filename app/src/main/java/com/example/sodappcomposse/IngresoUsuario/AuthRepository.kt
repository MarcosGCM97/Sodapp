package com.example.sodappcomposse.IngresoUsuario

import com.example.sodappcomposse.API.ApiServices
import com.example.sodappcomposse.API.UsuarioResponse
import retrofit2.Response
import javax.inject.Inject

interface AuthRepository {
    suspend fun login(usuarioRequest: UsuarioRequest): Response<UsuarioResponse>
}

class AuthRepositoryImpl @Inject constructor(
    private val apiServices: ApiServices
) : AuthRepository {
    override suspend fun login(usuarioRequest: UsuarioRequest): Response<UsuarioResponse> {
        return apiServices.login(usuarioRequest)
    }
}
