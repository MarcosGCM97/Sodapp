package com.example.sodappcomposse.API

import com.example.sodappcomposse.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Builds the single [ApiServices] client of the app. It is created through Hilt
 * (see di/NetworkModule) so the auth interceptor can be injected.
 */
object RetrofitInstance {

    // API_PATH comes from app/build.gradle.kts per build type ("api/" or the "api_2/" staging copy)
    private const val BASE_URL = "http://localhost:8765/"

    fun create(authInterceptor: Interceptor): ApiServices {
        // Interceptor para logs (solo en modo DEBUG)
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
            // Never log the session token
            redactHeader("Authorization")
        }

        val httpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiServices::class.java)
    }
}
