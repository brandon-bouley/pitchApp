package com.example.pitchapp.data.repository

import android.util.Log
import com.example.pitchapp.data.model.Album
import com.example.pitchapp.data.model.Profile
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import com.example.pitchapp.data.model.Result
import com.example.pitchapp.data.model.Review
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class ProfileRepository(private val reviewRepository: ReviewRepository) {
    private val db = FirebaseFirestore.getInstance()
    private val TAG = "ProfileRepository"

    suspend fun getProfile(userId: String): Profile {
        try {
            Log.d(TAG, "Getting profile for user ID: $userId")
            // Get basic user information
            val userDoc = db.collection("users").document(userId).get().await()

            if (!userDoc.exists()) {
                Log.w(TAG, "User document not found for ID: $userId")
                throw Exception("User not found")
            }

            // Manually convert document to Profile to handle potential missing fields
            val username = userDoc.getString("username") ?: ""
            val email = userDoc.getString("email")
            val bio = userDoc.getString("bio")
            val themePreference = userDoc.getString("themePreference") ?: "light"
            val isPublic = userDoc.getBoolean("isPublic") ?: true
            val profilePictureUrl = userDoc.getString("profilePictureUrl")

            // Handle followers and following lists
            val followers = userDoc.get("followers") as? List<String> ?: emptyList()
            val following = userDoc.get("following") as? List<String> ?: emptyList()

            // Create base profile
            val profile = Profile(
                userId = userId,
                username = username,
                email = email,
                bio = bio,
                followers = followers,
                following = following,
                themePreference = themePreference,
                isPublic = isPublic,
                profilePictureUrl = profilePictureUrl,
                createdAt = userDoc.getTimestamp("createdAt"),
                updatedAt = userDoc.getTimestamp("updatedAt"),
                lastUpdated = userDoc.getTimestamp("lastUpdated")
            )

            // Get reviews
            val reviews = when (val reviewsResult = reviewRepository.getReviewsByUser(userId)) {
                is Result.Success -> reviewsResult.data
                is Result.Error -> {
                    Log.e(TAG, "Error fetching reviews: ${reviewsResult.exception.message}")
                    emptyList()
                }
            }

            return profile.copy(
                recentReviews = reviews.takeIf { it.isNotEmpty() } ?: getReviewsFromUserDocument(userId),
                reviewCount = reviews.size
            )

        } catch (e: Exception) {
            Log.e(TAG, "Error getting profile", e)
            throw e
        }
    }

    private suspend fun getReviews(userId: String): List<Review> {
        try {
            Log.d(TAG, "Getting reviews for user ID: $userId")
            val reviewsSnapshot = db.collection("reviews")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .await()

            val reviews = mutableListOf<Review>()

            for (doc in reviewsSnapshot.documents) {
                try {
                    val reviewId = doc.id
                    val albumId = doc.getString("albumId")
                    val content = doc.getString("content")
                    val rating = doc.getDouble("rating")?.toFloat()
                    val timestamp = doc.getTimestamp("timestamp")

                    val review = Review(
                                        id = reviewId,
                                        userId = userId,
                                        albumId = albumId?: "",
                                        content = content?: "",
                                        rating = rating?: 0.0f,
                                        timestamp = timestamp?: Timestamp.now()
                                    )


                    // If review has albumId, fetch album details
                    if (albumId != null) {
                        try {
                            val albumDoc = db.collection("albums").document(albumId).get().await()
                            if (albumDoc.exists()) {
                                val albumName = albumDoc.getString("name") ?: "Unknown Album"
                                val artist = albumDoc.getString("artist") ?: "Unknown Artist"
                                val imageUrl = albumDoc.getString("imageUrl")

                                val album = Album(
                                    id = albumId,
                                    title = albumName,
                                    artist = artist,
                                    artworkUrl = imageUrl?: ""
                                )

                                review.albumDetails = album
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error fetching album details for review", e)
                            // Continue without album details
                        }
                    }

                    reviews.add(review)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing review document", e)
                    // Skip this review and continue
                }
            }

            return reviews
        } catch (e: Exception) {
            Log.e(TAG, "Error getting reviews", e)
            return emptyList()
        }
    }

    suspend fun getProfileByUsername(username: String): Profile {
        try {
            // Query for the user document by username
            val userQuery = db.collection("users")
                .whereEqualTo("username", username)
                .limit(1)
                .get()
                .await()

            if (userQuery.isEmpty) {
                throw Exception("User not found")
            }

            val userDoc = userQuery.documents[0]
            val userId = userDoc.id

            // Extract profile data
            val profileData = userDoc.data ?: throw Exception("User data is empty")

            // Parse recent reviews
            val recentReviews = mutableListOf<Review>()
            val reviewsData = profileData["recentReviews"] as? List<Map<String, Any>> ?: emptyList()

            Log.d(TAG, "Found ${reviewsData.size} reviews for user $username")

            for (reviewData in reviewsData) {
                try {
                    // Extract album details from the review data
                    val albumDetailsMap = reviewData["albumDetails"] as? Map<String, Any>

                    // Create the review object
                    val review = Review(
                        id = reviewData["id"] as? String ?: "",
                        userId = reviewData["userId"] as? String ?: "",
                        username = reviewData["username"] as? String ?: "",
                        albumId = reviewData["albumId"] as? String ?: "",
                        content = reviewData["content"] as? String ?: "",
                        rating = (reviewData["rating"] as? Number)?.toFloat() ?: 0.0f,
                        timestamp = reviewData["timestamp"] as? Timestamp ?: Timestamp.now(),
                        likes = (reviewData["likes"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                        favoriteTrack = reviewData["favoriteTrack"] as? String,
                        // If albumDetails is present in the review, parse it
                        albumDetails = albumDetailsMap?.let {
                            com.example.pitchapp.data.model.Album(
                                id = it["id"] as? String ?: "",
                                title = it["title"] as? String ?: "Unknown Album",
                                artist = it["artist"] as? String ?: "Unknown Artist",
                                artworkUrl = it["artworkUrl"] as? String ?: ""
                            )
                        }
                    )

                    recentReviews.add(review)
                    Log.d(TAG, "Added review: ${review.id} for album: ${review.albumId}")
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing review: ${e.message}")
                }
            }

            // Build full profile
            return Profile(
                userId = userId,
                username = profileData["username"] as? String ?: "",
                bio = profileData["bio"] as? String,
                profilePictureUrl = profileData["profilePictureUrl"] as? String ?: "",
                followers = (profileData["followers"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                following = (profileData["following"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                reviewCount = recentReviews.size,
                recentReviews = recentReviews
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting profile: ${e.message}", e)
            throw e
        }
    }


    suspend fun searchUsers(query: String): List<Profile> {
        if (query.length < 2) return emptyList()

        try {
            Log.d(TAG, "Searching users with query: $query")
            val snapshot = db.collection("users")
                .orderBy("username")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .limit(10)
                .get()
                .await()

            val profiles = mutableListOf<Profile>()
            for (doc in snapshot.documents) {
                try {
                    val userId = doc.id
                    val username = doc.getString("username") ?: continue
                    val bio = doc.getString("bio")
                    val followers = doc.get("followers") as? List<String> ?: emptyList()
                    val following = doc.get("following") as? List<String> ?: emptyList()

                    profiles.add(
                        Profile(
                            userId = userId,
                            username = username,
                            bio = bio,
                            followers = followers,
                            following = following
                        )
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing user document during search", e)
                }
            }

            return profiles
        } catch (e: Exception) {
            Log.e(TAG, "Error searching users", e)
            return emptyList()
        }
    }

    suspend fun followUser(currentUserId: String, targetUserId: String) {
        try {
            Log.d(TAG, "User $currentUserId following user $targetUserId")
            db.runBatch { batch ->
                batch.update(
                    db.collection("users").document(currentUserId),
                    "following", FieldValue.arrayUnion(targetUserId)
                )
                batch.update(
                    db.collection("users").document(targetUserId),
                    "followers", FieldValue.arrayUnion(currentUserId)
                )
            }.await()
        } catch (e: Exception) {
            Log.e(TAG, "Error following user", e)
            throw e
        }
    }

    suspend fun unfollowUser(currentUserId: String, targetUserId: String) {
        try {
            Log.d(TAG, "User $currentUserId unfollowing user $targetUserId")
            db.runBatch { batch ->
                batch.update(
                    db.collection("users").document(currentUserId),
                    "following", FieldValue.arrayRemove(targetUserId)
                )
                batch.update(
                    db.collection("users").document(targetUserId),
                    "followers", FieldValue.arrayRemove(currentUserId)
                )
            }.await()
        } catch (e: Exception) {
            Log.e(TAG, "Error unfollowing user", e)
            throw e
        }
    }

    suspend fun updateProfileStats(userId: String) {
        try {
            val reviews = getReviews(userId)
            val averageRating = if (reviews.isNotEmpty()) {
                reviews.mapNotNull { it.rating }.average().toFloat()
            } else {
                0f
            }

            db.collection("users").document(userId).update(
                mapOf(
                    "reviewCount" to reviews.size,
                    "averageRating" to averageRating,
                    "updatedAt" to Timestamp.now()
                )
            ).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating profile stats", e)
            throw e
        }
    }

    suspend fun updateProfile(userId: String, updates: Map<String, Any>) {
        try {
            db.collection("users")
                .document(userId)
                .update(updates + mapOf("updatedAt" to Timestamp.now()))
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating profile", e)
            throw e
        }
    }

    private suspend fun getReviewsFromUserDocument(userId: String): List<Review> {
        val userDoc = db.collection("users").document(userId).get().await()
        val reviewsData = userDoc.get("recentReviews") as? List<Map<String, Any>> ?: emptyList()

        return reviewsData.mapNotNull { data ->
            try {
                Review(
                    id = data["id"] as? String ?: "",
                    albumId = data["albumId"] as? String ?: "",
                    userId = data["userId"] as? String ?: "",
                    username = data["username"] as? String ?: "Anonymous",
                    content = data["content"] as? String ?: "",
                    rating = (data["rating"] as? Double)?.toFloat() ?: 0f,
                    timestamp = data["timestamp"] as? Timestamp ?: Timestamp.now(),
                    likes = (data["likes"] as? List<String>) ?: emptyList(),
                    albumDetails = (data["albumDetails"] as? Map<String, Any>)?.let { albumMap ->
                        Album(
                            id = albumMap["id"] as? String ?: "",
                            title = albumMap["title"] as? String ?: "",
                            artist = albumMap["artist"] as? String ?: "",
                            artworkUrl = albumMap["artworkUrl"] as? String ?: ""
                        )
                    },
                    favoriteTrack = data["favoriteTrack"] as? String
                )
            } catch (e: Exception) {
                Log.e("ReviewConversion", "Error converting map to Review", e)
                null
            }
        }
    }

}