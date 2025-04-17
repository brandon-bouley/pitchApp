package com.example.pitchapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.pitchapp.data.model.Review

@Dao
interface ReviewDao {
    @Insert
    suspend fun insert(review: Review)

    @Query("SELECT * FROM reviews")
    suspend fun getAllReviews(): List<Review>

    @Query("SELECT * FROM reviews WHERE author = :username")
    suspend fun getReviewsByUser(username: String): List<Review>

    @Query("SELECT * FROM reviews WHERE albumId = :albumId ORDER BY timestamp DESC")
    suspend fun getReviewsForAlbum(albumId: String): List<Review>

    @Query("SELECT * FROM reviews ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentReviews(limit: Int = 50): List<Review>

    @Query("SELECT AVG(rating) FROM reviews WHERE albumId = :albumId")
    suspend fun getAverageRating(albumId: String): Float?
}