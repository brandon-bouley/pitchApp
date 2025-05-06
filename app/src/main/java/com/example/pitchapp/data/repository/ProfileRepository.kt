package com.example.pitchapp.data.repository

import android.util.Log
import com.example.pitchapp.data.model.Album
import com.example.pitchapp.data.model.Profile
import com.example.pitchapp.data.model.Review
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class ProfileRepository {
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
            val reviews = getReviews(userId)

            // Return complete profile
            return profile.copy(
                recentReviews = reviews,
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

                    var review = Review(
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
            Log.d(TAG, "Getting profile for username: $username")
            // Query users collection by username
            val query = db.collection("users")
                .whereEqualTo("username", username)
                .limit(1)
                .get()
                .await()

            if (query.isEmpty) {
                Log.w(TAG, "No user found with username: $username")
                throw Exception("User not found")
            }

            val userDoc = query.documents[0]
            val userId = userDoc.id

            // Use the existing getProfile method to get the full profile
            return getProfile(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting profile by username", e)
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
}