package com.example.pitchapp.data.local.dao

import androidx.room.*
import com.example.pitchapp.data.model.Track

@Dao
interface TrackDao {
    @Query("SELECT * FROM tracks WHERE title LIKE :query OR artist LIKE :query")
    suspend fun searchTracks(query: String): List<Track>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tracks: List<Track>)
}
