package com.example.pitchapp.data.remote
import retrofit2.http.GET
import retrofit2.http.Query
import com.example.pitchapp.data.model.Track
import com.example.pitchapp.data.model.Album

interface ReccoBeatsApiService {
    @GET("v1/album/search")
    suspend fun searchAlbums(@Query("query") query: String): AlbumSearchResponse

    @GET("v1/track/search")
    suspend fun searchTracks(@Query("query") query: String): TrackSearchResponse
}

data class AlbumSearchResponse(
    val albums: List<Album>
)

data class TrackSearchResponse(
    val tracks: List<Track>
)