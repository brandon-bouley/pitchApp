package com.example.pitchapp.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude

data class Review(
    @DocumentId val id: String = "", // Firestore document ID
    val albumId: String,             // Reference to album's generated SHA-256 ID
    val userId: String,              // Firebase Auth UID
    val username: String,            // Display name (duplicated for queries)
    val content: String,
    val rating: Float,
    val timestamp: Timestamp = Timestamp.now(),

    @Exclude                         // Mark as excluded from Firestore serialization
    val albumDetails: Album? = null  // Not stored in Firestore
) {
    // Validation for rating values
    init {
        require(rating in 0.5f..5.0f) {
            "Rating must be between 0.5 and 5.0"
        }
        require(rating % 0.5f == 0f) {
            "Rating must be in half-star increments"
        }
    }

    // Firestore serialization helper
    fun toFirestoreMap(): Map<String, Any> = mapOf(
        "albumId" to albumId,
        "userId" to userId,
        "username" to username,
        "content" to content,
        "rating" to rating,
        "timestamp" to timestamp
    )
}