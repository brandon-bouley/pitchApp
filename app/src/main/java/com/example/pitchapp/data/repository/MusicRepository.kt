package com.example.pitchapp.data.repository
import android.util.Log
import com.example.pitchapp.data.model.Album
import com.example.pitchapp.data.model.Artist
import com.example.pitchapp.data.remote.ReccoBeatsApiService

class MusicRepository(private val apiService: ReccoBeatsApiService) {

    suspend fun searchArtists(query: String): List<Artist> {
        return apiService.searchArtists(query).content
    }

    suspend fun searchAlbums(query: String): List<Album> {
        Log.d("DEBUG", "Calling API for albums")
        return apiService.searchAlbums(query).content
    }
}