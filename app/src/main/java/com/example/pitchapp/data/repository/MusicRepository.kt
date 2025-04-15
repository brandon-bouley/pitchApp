package com.example.pitchapp.data.repository

import android.util.Log
import com.example.pitchapp.data.model.Album
import com.example.pitchapp.data.model.Artist
import com.example.pitchapp.data.remote.ReccoBeatsApiService

class MusicRepository(private val apiService: ReccoBeatsApiService) {

    suspend fun searchArtists(query: String): List<Artist> {
        return try {
            val response = apiService.searchArtists(query)
            Log.d("API_SUCCESS", "Artists fetched: ${response.content.size}")
            response.content
        } catch (e: Exception) {
            Log.e("API_ERROR", "Artist fetch failed ${e.message}", e)
            emptyList()
        }
    }

    suspend fun searchAlbums(query: String): List<Album> {
        return try {
            val response = apiService.searchAlbums(query)
            Log.d("API_SUCCESS", "Albums fetched: ${response.content.size}")
            response.content
        } catch (e: Exception) {
            Log.e("API_ERROR", "Album fetch failed ${e.message}", e)
            emptyList()
        }
    }

    suspend fun getAlbumDetails(albumId: String): Result<Album> {
        return try {
            val response = apiService.getAlbumDetails(albumId)
            Log.d("ALBUM_DETAILS", "Fetched details for $albumId")
            Result.Success(response)
        } catch (e: Exception) {
            Log.e("ALBUM_DETAILS", "Failed to fetch album details", e)
            Result.Error(e)
        }
    }

    suspend fun getArtistAlbums(artistId: String): List<Album> {
        return try {
            val response = apiService.getArtistAlbums(artistId)
            Log.d("API_SUCCESS", "Artist albums fetched: ${response.content.size}")
            response.content
        } catch (e: Exception) {
            Log.e("API_ERROR", "Artist albums fetch failed", e)
            emptyList()
        }
    }


    sealed class Result<T> {
        data class Success<T>(val data: T) : Result<T>()
        data class Error<T>(val exception: Exception) : Result<T>()
    }
}