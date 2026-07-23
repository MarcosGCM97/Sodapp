package com.example.sodappcomposse.API

import com.example.sodappcomposse.API.ApiServices
import com.example.sodappcomposse.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    private const val BASE_URL = "http://www.aplicaciones-servicios-1997.site/"

    // Interceptor para logs (solo en modo DEBUG)
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
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
