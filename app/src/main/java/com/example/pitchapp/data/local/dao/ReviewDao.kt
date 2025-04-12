package com.example.pitchapp.data.local.dao
import androidx.room.*
import com.example.pitchapp.data.model.Review

@Dao
interface ReviewDao {
    @Query("SELECT * FROM reviews WHERE itemId = :itemId AND itemType = :itemType ORDER BY timestamp DESC")
    suspend fun getReviews(itemId: String, itemType: String): List<Review>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: Review)

    @Delete
    suspend fun deleteReview(review: Review)
}