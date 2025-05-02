package com.example.pitchapp.data.remote

import com.example.pitchapp.data.model.ArtistInfoResponse
import com.example.pitchapp.data.model.ArtistTopAlbumsResponse
import com.example.pitchapp.data.model.AlbumInfoResponse
import com.example.pitchapp.data.model.AlbumSearchResponse
import com.example.pitchapp.data.model.ArtistSearchResponse
import com.example.pitchapp.data.model.TopTracksResponse

import retrofit2.http.GET
import retrofit2.http.Query

interface LastFmService {
    @GET(".")
    suspend fun getTopTracks(
        @Query("method") method: String = "chart.gettoptracks",
        @Query("limit") limit: Int = 11
    ): TopTracksResponse
    //got rid of Call

    @GET(".")
    suspend fun searchArtists(
        @Query("method") method: String = "artist.search",
        @Query("artist") query: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 30
    ): ArtistSearchResponse

    @GET(".")
    suspend fun getArtistTopAlbums(
        @Query("method") method: String = "artist.getTopAlbums",
        @Query("artist") artist: String,
        @Query("mbid") mbid: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): ArtistTopAlbumsResponse

//    @GET(".")
//    suspend fun getArtistInfo(
//        @Query("method") method: String = "artist.getInfo",
//        @Query("artist") artist: String,
//        @Query("mbid") mbid: String? = null,
//        @Query("autocorrect") autocorrect: Int = 1
//    ): ArtistInfoResponse

    @GET(".")
    suspend fun searchAlbums(
        @Query("method") method: String = "album.search",
        @Query("album") query: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 30
    ): AlbumSearchResponse

    @GET(".")
    suspend fun getAlbumInfo(
        @Query("method") method: String = "album.getInfo",
        @Query("artist") artist: String,
        @Query("album") album: String,
        @Query("mbid") mbid: String? = null,
        @Query("autocorrect") autocorrect: Int = 1
    ): AlbumInfoResponse
}