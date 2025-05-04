package com.example.pitchapp.data.remote

import android.util.Log
import com.example.pitchapp.data.model.DetailTrack
import com.example.pitchapp.data.model.TrackWrapper
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import okhttp3.logging.HttpLoggingInterceptor
import java.lang.reflect.Type

object LastFmApi {
    private const val BASE_URL = "https://ws.audioscrobbler.com/2.0/"
    private const val API_KEY = "78491fad8a8b76979cfe33f0ed510645"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)

        .addInterceptor { chain ->
            val original = chain.request()
            val url = original.url.newBuilder()
                .addQueryParameter("api_key", API_KEY)
                .addQueryParameter("format", "json")
                .addQueryParameter("raw", "true")
                .build()
            chain.proceed(original.newBuilder().url(url).build())
        }
        .addInterceptor(logging)
        .build()

    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(TrackWrapper::class.java, TrackWrapperDeserializer())
        .create()

    val service: LastFmService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build()
            .create(LastFmService::class.java)
    }
}

class TrackWrapperDeserializer : JsonDeserializer<TrackWrapper> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): TrackWrapper {
        return try {
            val trackObj = json.asJsonObject.get("track")
            when {
                trackObj.isJsonArray -> {
                    val listType = object : TypeToken<List<DetailTrack>>() {}.type
                    TrackWrapper(context.deserialize(trackObj, listType))
                }
                trackObj.isJsonObject -> {
                    val singleTrack = context.deserialize<DetailTrack>(trackObj, DetailTrack::class.java)
                    TrackWrapper(listOf(singleTrack))
                }
                else -> TrackWrapper(emptyList<DetailTrack>())
            }
        } catch (e: Exception) {
            TrackWrapper(emptyList<DetailTrack>())
        }
    }
}
