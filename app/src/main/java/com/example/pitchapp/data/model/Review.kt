package com.example.pitchapp.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude

data class Review(
    @DocumentId val id: String = "",
    val albumId: String = "",
    val trackId: String ="",
    val userId: String = "",
    val username: String = "",
    val content: String = "",
    val rating: Float = 0f,
    val timestamp: Timestamp = Timestamp.now(),
    val likes: List<String> = emptyList(), //  user IDs who liked this review
    val likeCount: Int = 0, // Derived field for querying/sorting
    val albumDetails: Album? = null,
    val favoriteTrack: String? = null
) {
    init {
        require(rating in 0.5f..5.0f) { "Rating must be between 0.5 and 5.0" }
        require(rating % 0.5f == 0f) { "Rating must be in half-star increments" }
    }

    // Firestore serialization helper
    fun toFirestoreMap(): Map<String, Any> = mapOf(
        "albumId" to albumId,
        "trackId" to trackId,
        "userId" to userId,
        "username" to username,
        "content" to content,
        "rating" to rating,
        "timestamp" to timestamp,
        "likes" to likes,
        "likeCount" to likes.size,
        "favoriteTrack" to (favoriteTrack ?: ""),
        "albumDetails" to (albumDetails ?: Album())
    )
}