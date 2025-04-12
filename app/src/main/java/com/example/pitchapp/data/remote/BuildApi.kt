package com.example.pitchapp.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object BuildApi {
    private const val BASE_URL = "https://api.reccobeats.com/"

    val api: ReccoBeatsApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ReccoBeatsApiService::class.java)
    }
}
