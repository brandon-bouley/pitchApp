//package com.example.pitchapp.data.remote
//import com.example.pitchapp.data.mode.Artist
//import retrofit2.http.GET
//import retrofit2.http.Query
//import com.example.pitchapp.data.model.Album
//import retrofit2.http.Path
//
//interface ReccoBeatsApiService {
//    @GET("v1/album/search")
//    suspend fun searchAlbums(
//        @Query("searchText") query: String,
//        @Query("page") page: Int = 0,
//        @Query("size") size: Int = 25
//    ): AlbumSearchResponse
//
//    @GET("v1/artist/search")
//    suspend fun searchArtists(
//        @Query("searchText") query: String,
//        @Query("page") page: Int = 0,
//        @Query("size") size: Int = 25
//    ): ArtistSearchResponse
//
//    @GET("v1/album/{id}")
//    suspend fun getAlbumDetails(@Path("id") albumId: String): Album
//
//    @GET("v1/artist/{id}/album")
//    suspend fun getArtistAlbums(
//        @Path("id") artistId: String,
//        @Query("page") page: Int = 0,
//        @Query("size") size: Int = 25
//    ): AlbumSearchResponse
//}
//
//data class AlbumSearchResponse(
//    val content: List<Album>,
//    val page: Int,
//    val size: Int,
//    val totalElements: Int,
//    val totalPages: Int
//)
//data class ArtistSearchResponse(
//    val content: List<Artist>,
//    val page: Int,
//    val size: Int,
//    val totalElements: Int,
//    val totalPages: Int
//)