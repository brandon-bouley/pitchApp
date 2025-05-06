package com.example.pitchapp.data.repository

import com.example.pitchapp.data.model.Result
import com.example.pitchapp.data.model.Review
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class TrackReviewRepository {
    private val db = Firebase.firestore
    private val reviewsRef = db.collection("track_reviews")
    private val tracksRef = db.collection("tracks")

    suspend fun insertTrackReview(review: Review): Result<Unit> {
        return try {
            val doc = reviewsRef.document()
            val data = review.copy(id = doc.id).toFirestoreMap()
            doc.set(data).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getReviewsForTrack(trackId: String): Result<List<Review>> {
        return try {
            val querySnapshot = reviewsRef
                .whereEqualTo("trackId", trackId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            Result.Success(querySnapshot.toReviews())
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getTrackAverageRating(trackId: String): Result<Float> {
        return try {
            val trackSnapshot = tracksRef.document(trackId).get().await()
            val averageRating = trackSnapshot.getDouble("averageRating")?.toFloat() ?: 0f
            Result.Success(averageRating)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    private fun QuerySnapshot.toReviews(): List<Review> {
        return this.documents.mapNotNull { doc ->
            try {
                val trackId = doc.getString("trackId") ?: ""
                val userId = doc.getString("userId") ?: ""
                val username = doc.getString("username") ?: ""
                val content = doc.getString("content") ?: ""
                val rating = doc.getDouble("rating")?.toFloat() ?: 0f
                val timestamp = doc.getTimestamp("timestamp") ?: Timestamp.now()

                Review(
                    id = doc.id,
                    albumId = trackId,
                    userId = userId,
                    username = username,
                    content = content,
                    rating = rating,
                    timestamp = timestamp
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun Review.toFirestoreMap(): Map<String, Any?> {
        return mapOf(
            "trackId" to albumId,
            "userId" to userId,
            "username" to username,
            "content" to content,
            "rating" to rating,
            "timestamp" to timestamp
        )
    }
}
