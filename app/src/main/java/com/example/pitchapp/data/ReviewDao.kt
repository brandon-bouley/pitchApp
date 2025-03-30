package com.example.pitchapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewDao {
    @Insert
    suspend fun insert(review: Review)

    @Query("SELECT * FROM review WHERE songId = :songId")
    fun getReviewsForSong(songId: String): Flow<List<Review>>
}