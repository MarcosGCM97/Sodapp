package com.example.sodappcomposse.API

import com.example.sodappcomposse.API.ApiServices
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory // O MoshiConverterFactory
import kotlin.getValue
import kotlin.jvm.java


object RetrofitInstance {

    private const val BASE_URL = "https://www.unont.com.ar/mumResponsive/fev2/temp/Trash/"

    // Interceptor para logs (opcional, pero muy útil para depurar)
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Muestra el cuerpo de la petición y respuesta
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor) // Añadir el interceptor
        .build()

    // Creación de la instancia de Retrofit (lazy para que se cree solo cuando se necesite)
    val api: ApiServices by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient) // Usar el cliente OkHttp configurado
            .addConverterFactory(GsonConverterFactory.create()) // Especificar el convertidor JSON
            .build()
            .create(com.example.sodappcomposse.API.ApiServices::class.java) // Crear la implementación de tu interfaz ApiService
    }
}
