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
    var albumDetails: Album? = null,
    val favoriteTrack: String? = null
) {
    init {
        require(rating in 0.5f..5.0f) { "Rating must be between 0.5 and 5.0" }
        require(rating % 0.5f == 0f) { "Rating must be in half-star increments" }
    }

    // Firestore serialization helper

    fun toFirestoreMap(): Map<String, Any> {
        return hashMapOf<String, Any>().apply {
            put("id", id)
            put("albumId", albumId)
            put("trackId",trackId)
            put("userId", userId)
            put("username", username)
            put("content", content)
            put("rating", rating)
            put("timestamp", timestamp)
            put("likes", likes)
            put("albumDetails", albumDetails?.toFirestoreMap() ?: hashMapOf<String, Any>())
            put("favoriteTrack", favoriteTrack ?: "")
        }
    }

    fun Album.toFirestoreMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "title" to title,
            "artist" to artist,
            "artworkUrl" to artworkUrl,
        )
    }
}