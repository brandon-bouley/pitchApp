package com.example.pitchapp.data.repository

import android.util.Log
import com.example.pitchapp.data.model.Album
import com.example.pitchapp.data.model.Review
import com.google.firebase.Firebase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.QuerySnapshot
import com.example.pitchapp.data.model.Result
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue

class ReviewRepository {
    private val db = Firebase.firestore
    private val reviewsRef = db.collection("reviews")
    private val albumsRef = db.collection("albums")
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

    // In ReviewRepository
    suspend fun insertReview(review: Review): Result<Unit> {
        return try {
            val doc = reviewsRef.document()
            val data = review.copy(id = doc.id).toFirestoreMap()

            // Write review to reviews collection
            doc.set(data).await()

            // Add to user's recentReviews (keep only last 5)
            db.collection("users").document(review.userId)
                .update("recentReviews", FieldValue.arrayUnion(data))
                .await()

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getAllReviews(): Result<List<Review>> {
        return try {
            val snapshot = Firebase.firestore.collection("reviews")
                .get()
                .await()
            Result.Success(snapshot.toReviews())
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getReviewsByUser(userId: String): Result<List<Review>> {
        return try {
            val querySnapshot = reviewsRef
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            Result.Success(querySnapshot.toReviews())
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getReviewsForAlbum(albumId: String): Result<List<Review>> {
        return try {
            val querySnapshot = reviewsRef
                .whereEqualTo("albumId", albumId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            Result.Success(querySnapshot.toReviews())
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

    suspend fun getRecentReviews(limit: Int = 50): Result<List<Review>> {
        return try {
            val querySnapshot = reviewsRef
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            Result.Success(querySnapshot.toReviews())
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getAverageRating(albumId: String,type: String): Result<Float> {
        return try {
            if(type==="albums"){
                val albumSnapshot = albumsRef.document(albumId).get().await()
                val averageRating = albumSnapshot.getDouble("averageRating")?.toFloat() ?: 0f
                Result.Success(averageRating)

            }else{
                val albumSnapshot = tracksRef.document(albumId).get().await()
                val averageRating = albumSnapshot.getDouble("averageRating")?.toFloat() ?: 0f
                Result.Success(averageRating)
            }

        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun toggleLike(reviewId: String, userId: String): Result<Unit> {
        return try {
            val reviewRef = reviewsRef.document(reviewId)
            db.runTransaction { transaction ->
                val snapshot = transaction.get(reviewRef)
                val currentLikes = snapshot.get("likes") as? List<String> ?: emptyList()

                val newLikes = if (userId in currentLikes) {
                    currentLikes - userId
                } else {
                    currentLikes + userId
                }

                transaction.update(reviewRef,
                    mapOf(
                        "likes" to newLikes,
                        "likeCount" to newLikes.size
                    )
                )
            }.await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // Helper extension to convert QuerySnapshot to Review list

    private fun DocumentSnapshot.toReview(): Review? {
        return try {
            val albumDetailsMap = get("albumDetails") as? Map<String, Any>

            Review(
                id = id,
                albumId = getString("albumId") ?: "",
                userId = getString("userId") ?: "",
                username = getString("username") ?: "",
                content = getString("content") ?: "",
                rating = getDouble("rating")?.toFloat() ?: 0f,
                timestamp = getTimestamp("timestamp") ?: Timestamp.now(),
                likes = get("likes") as? List<String> ?: emptyList(),
                albumDetails = albumDetailsMap?.let { map ->
                    Album(
                        id = map["id"] as? String ?: "",
                        title = map["title"] as? String ?: "",
                        artist = map["artist"] as? String ?: "",
                        artworkUrl = map["artworkUrl"] as? String ?: ""
                    )
                },
                favoriteTrack = getString("favoriteTrack")
            )
        } catch (e: Exception) {
            Log.e("ReviewParsing", "Error parsing review document $id", e)
            null
        }
    }

    private fun QuerySnapshot.toReviews(): List<Review> {
        return this.documents.mapNotNull { it.toReview() }
    }
}