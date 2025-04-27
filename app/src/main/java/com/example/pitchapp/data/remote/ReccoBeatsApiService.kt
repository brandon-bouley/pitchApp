package com.example.pitchapp.data.remote
import retrofit2.http.GET
import retrofit2.http.Query
import com.example.pitchapp.data.model.Artist
import com.example.pitchapp.data.model.Album

interface ReccoBeatsApiService {
    @GET("v1/album/search")
    suspend fun searchAlbums(@Query("searchText") query: String): AlbumSearchResponse

    @GET("v1/artist/search")
    suspend fun searchArtists(@Query("searchText") query: String): ArtistSearchResponse
}

data class AlbumSearchResponse(
    val content: List<Album>,
    val page: Int,
    val size: Int,
    val totalElements: Int,
    val totalPages: Int
)
data class ArtistSearchResponse(
    val content: List<Artist>,
    val page: Int,
    val size: Int,
    val totalElements: Int,
    val totalPages: Int
)