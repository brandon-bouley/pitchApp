//package com.example.pitchapp.data.repository
//
//import com.example.pitchapp.data.model.Result
//import com.example.pitchapp.data.model.Track
//
//import com.example.pitchapp.data.model.TrackReview
//import com.google.firebase.Firebase
//import com.google.firebase.Timestamp
//import com.google.firebase.firestore.DocumentSnapshot
//import com.google.firebase.firestore.Query
//import com.google.firebase.firestore.QuerySnapshot
//import com.google.firebase.firestore.firestore
//import kotlinx.coroutines.tasks.await
//
//class TrackReviewRepository {
//    private val db = Firebase.firestore("newpitchdb")
//    private val reviewsRef = db.collection("track_reviews")
//    private val tracksRef = db.collection("tracks")
//
//    suspend fun insertTrackReview(review: TrackReview): Result<Unit> {
//        return try {
//            val doc = reviewsRef.document()
//            val data = review.copy(id = doc.id).toTrackFirestoreMap()
//            doc.set(data).await()
//            Result.Success(Unit)
//        } catch (e: Exception) {
//            Result.Error(e)
//        }
//    }
//    suspend fun insertTrack(review: Track): Result<Unit> {
//        return try {
//            val doc = reviewsRef.document()
//            val data = review.copy(id = doc.id).toTrackFirestoreMap()
//            doc.set(data).await()
//            Result.Success(Unit)
//        } catch (e: Exception) {
//            Result.Error(e)
//        }
//    }
//
//
//    suspend fun getReviewsForTrack(trackId: String): Result<List<TrackReview>> {
//        return try {
//            val querySnapshot = reviewsRef
//                .whereEqualTo("trackId", trackId)
//                .orderBy("timestamp", Query.Direction.DESCENDING)
//                .get()
//                .await()
//
//            Result.Success(querySnapshot.toTrackReviews())
//        } catch (e: Exception) {
//            Result.Error(e)
//        }
//    }
//
//    suspend fun getTrackAverageRating(trackId: String): Result<Float> {
//        return try {
//            val trackSnapshot = tracksRef.document(trackId).get().await()
//            val averageRating = trackSnapshot.getDouble("averageRating")?.toFloat() ?: 0f
//            Result.Success(averageRating)
//        } catch (e: Exception) {
//            Result.Error(e)
//        }
//    }
//    private fun DocumentSnapshot.toTrackReview(): TrackReview? {
//        return try {
//            TrackReview(
//                id = id,
//                trackId = getString("albumId") ?: "",
//                userId = getString("userId") ?: "",
//                username = getString("username") ?: "",
//                content = getString("content") ?: "",
//                rating = getDouble("rating")?.toFloat() ?: 0f,
//                timestamp = getTimestamp("timestamp") ?: Timestamp.now(),
//                likes = get("likes") as? List<String> ?: emptyList()
//            )
//        } catch (e: Exception) {
//            null
//        }
//    }
//
//    private fun QuerySnapshot.toTrackReviews(): List<TrackReview> {
//        return this.documents.mapNotNull { it.toTrackReview() }
//    }
//
//
//    }
//
//
//
