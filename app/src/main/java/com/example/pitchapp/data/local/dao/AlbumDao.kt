package com.example.pitchapp.data.local.dao

import androidx.room.*
import com.example.pitchapp.data.model.Album

@Dao
interface AlbumDao {
    @Query("SELECT * FROM albums WHERE name LIKE :query OR artist LIKE :query")
    suspend fun searchAlbums(query: String): List<Album>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(albums: List<Album>)
}