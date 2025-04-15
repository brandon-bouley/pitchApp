package com.example.pitchapp.data.remote

import android.util.Log
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object BuildApi {
    private const val BASE_URL = "https://api.reccobeats.com/"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Accept", "application/json")
                .build()

            Log.d("API_REQUEST", "Request URL: ${request.url}")

            val response = chain.proceed(request)

            val rawJson = response.peekBody(Long.MAX_VALUE).string()
            Log.d("API_RESPONSE", "Response JSON: $rawJson")

            response
        }
        .build()

    val api: ReccoBeatsApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ReccoBeatsApiService::class.java)
    }
}