package com.example.exerciseappapi

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "https://exercisedb.p.rapidapi.com/"
    private const val API_KEY = "9d7488b9a8mshe065b196c0d4f6bp184cb5jsn05a58f96fd91"
    private const val API_HOST = "exercisedb.p.rapidapi.com"

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain: Interceptor.Chain ->
            val request: Request = chain.request().newBuilder()
                .addHeader("x-rapidapi-key", API_KEY)
                .addHeader("x-rapidapi-host", API_HOST)
                .build()
            chain.proceed(request)
        }
        .build()

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: ExerciseApiService by lazy {
        retrofit.create(ExerciseApiService::class.java)
    }
}


