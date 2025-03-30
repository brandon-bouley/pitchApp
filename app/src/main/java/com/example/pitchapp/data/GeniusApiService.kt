package com.example.pitchapp.data

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface GeniusApiService {
    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Header("Authorization") token: String = "Bearer sJw_PfnYfU_wDE-UOhImtEG-U1bdYUEZG3tdkCSV-usHuU70KhwxlSQhsC3zbyKk"
    ): Response<GeniusSearchResponse>

    @GET("songs/{id}")
    suspend fun getSongDetails(
        @Path("id") songId: String,
        @Header("Authorization") token: String = "Bearer sJw_PfnYfU_wDE-UOhImtEG-U1bdYUEZG3tdkCSV-usHuU70KhwxlSQhsC3zbyKk"
    ): Response<SongDetailsResponse>

    data class SongDetailsResponse(val response: SongResponse)
    data class SongResponse(val song: Song)
    data class GeniusSearchResponse(val response: ResponseData)
    data class ResponseData(val hits: List<Hit>)
    data class Hit(val result: Song)
    data class Song(
        val id: String,
        val title: String,
        @SerializedName("artist_names")
        val artist: String,
        @SerializedName("song_art_image_thumbnail_url")
        val thumbnail: String
    )

}