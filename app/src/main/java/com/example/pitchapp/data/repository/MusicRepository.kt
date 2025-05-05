package com.example.pitchapp.data.repository

import android.util.Log
import com.example.pitchapp.data.model.Album
import com.example.pitchapp.data.model.ApiAlbum
import com.example.pitchapp.data.model.ApiAlbumSimple
import com.example.pitchapp.data.model.ApiArtist
import com.example.pitchapp.data.model.Artist
import com.example.pitchapp.data.model.RandomTrack
import com.example.pitchapp.data.remote.LastFmService
import com.example.pitchapp.data.model.toDomainAlbum
import com.example.pitchapp.data.model.toDomainArtist
import com.example.pitchapp.data.model.Result
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import javax.inject.Inject

class MusicRepository @Inject constructor(
    private val lastFmService: LastFmService
) {

    companion object {
        fun generateAlbumId(artist: String, title: String): String {
            val normalized = "${artist.trim().lowercase()}|${title.trim().lowercase()}"
            return normalized.sha256()
        }

        private fun String.sha256(): String {
            val bytes = MessageDigest.getInstance("SHA-256")
                .digest(this.toByteArray(Charsets.UTF_8))
            return bytes.joinToString("") { "%02x".format(it) }
        }
    }

    private val db = Firebase.firestore("newPitchDB")
    private val albumsRef = db.collection("albums")

   suspend fun getTopTracks(limit: Int = 11): List<RandomTrack> {
        return try {
            val response = lastFmService.getTopTracks(
                method="chart.gettoptracks",
                limit=limit
            )
            response.tracks.track // assuming TopTracksResponse has a 'tracks' field containing a list of tracks
        } catch (e: Exception) {
            Log.e("MusicRepository", "Fetching top tracks failed", e)
            emptyList()
        }
    }


    suspend fun getAlbumFromFirestore(albumId: String): Result<Album> {
        return try {
            val snapshot = db.collection("albums")
                .document(albumId)
                .get()
                .await()

            if (snapshot.exists()) {
                Result.Success(snapshot.toObject(Album::class.java)!!)
            } else {
                Result.Error(Exception("Album not found"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // MusicRepository.kt
    private suspend fun saveAlbumToFirestore(album: Album): Result<Unit> {
        return try {
            db.collection("albums")
                .document(album.id)
                .set(album)
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // ----- ARTIST SEARCH -----
    suspend fun searchArtists(
        query: String,
        page: Int = 1,
        limit: Int = 30
    ): Result<List<Artist>> =
        try {
            val resp = lastFmService.searchArtists(
                method = "artist.search",
                query  = query,
                page   = page,
                limit  = limit
            )
            // resp.results.artistMatches.artists: List<ApiArtist>
            val list = resp.results.artistMatches.artists
                .map(ApiArtist::toDomainArtist)
            Result.Success(list)
        } catch (e: Exception) {
            Log.e("MusicRepository", "Artist search failed", e)
            Result.Error(e)
        }

    // ----- ARTIST TOP ALBUMS -----
    suspend fun getArtistTopAlbums(
        artist: String,
        mbid: String? = null,
        page: Int = 1,
        limit: Int = 50
    ): Result<List<Album>> =
        try {
            val resp = lastFmService.getArtistTopAlbums(
                method = "artist.gettopalbums",
                artist = artist,
                mbid   = mbid,
                page   = page,
                limit  = limit
            )
            // resp.topalbums.albums: List<ApiAlbumSimple>
            val list = resp.topalbums.albums
                .map(ApiAlbumSimple::toDomainAlbum)
            Result.Success(list)
        } catch (e: Exception) {
            Log.e("MusicRepository", "Top albums fetch failed", e)
            Result.Error(e)
        }

    // ----- ALBUM SEARCH -----
    suspend fun searchAlbums(
        query: String,
        page: Int = 1,
        limit: Int = 30
    ): Result<List<Album>> =
        try {
            val resp = lastFmService.searchAlbums(
                method = "album.search",
                query  = query,
                page   = page,
                limit  = limit
            )
            // resp.results.albumMatches.albums: List<ApiAlbum>
            val list = resp.results.albumMatches.albums
                .map(ApiAlbum::toDomainAlbum)
            Result.Success(list)
        } catch (e: Exception) {
            Log.e("MusicRepository", "Album search failed", e)
            Result.Error(e)
        }

    // ----- ALBUM DETAIL -----
    suspend fun getAlbumInfo(
        artist: String? = null,
        album:  String? = null,
        mbid:    String? = null
    ): Result<Album> =
        try {
            require(artist != null || mbid != null) {
                "Must provide either artist+album or MBID"
            }

            val resp = lastFmService.getAlbumInfo(
                method    = "album.getinfo",
                artist    = artist ?: "",
                album     = album  ?: "",
                mbid      = mbid
            )

            // resp.album: AlbumDetail
            val domain = resp.album.toDomainAlbum()
            Result.Success(domain)
        } catch (e: Exception) {
            Log.e("MusicRepository", "Album details fetch failed", e)
            Result.Error(e)
        }

    suspend fun getOrFetchAlbum(artist: String, title: String): Result<Album> {
        val albumId = generateAlbumId(artist, title)

        return when (val existing = getAlbumFromFirestore(albumId)) {
            is Result.Success -> existing
            else -> {
                when (val apiResult = getAlbumInfo(artist, title)) {
                    is Result.Success -> {
                        val rawAlbum = apiResult.data
                        if (rawAlbum.title.isBlank() || rawAlbum.artist.isBlank()) {
                            return Result.Error(Exception("Invalid API album data"))
                        }

                        val album = rawAlbum.copy(id = albumId)

                        // 3. Handle save result properly
                        when (val saveResult = saveAlbumToFirestore(album)) {
                            is Result.Success -> Result.Success(album)
                            is Result.Error -> Result.Error(
                                Exception("API data loaded but Firestore save failed: ${saveResult.exception}")
                            )
                        }
                    }
                    is Result.Error -> apiResult
                }
            }
        }
    }



}