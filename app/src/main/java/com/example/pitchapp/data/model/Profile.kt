package com.example.pitchapp.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude

data class Profile(
    @DocumentId val userId: String = "",
    val displayName: String = "",
    val email: String? = null,
    val photoUrl: String? = null,
    val reviewCount: Int = 0,
    val averageRating: Float = 0f,
    val createdAt: Timestamp = Timestamp.now(),
    val lastUpdated: Timestamp = Timestamp.now()
) {
    @Exclude var reviews: List<Review> = emptyList()  // Not stored in Firestore


    init {
        require(averageRating in 0f..5f) {
            "Average rating must be between 0 and 5"
        }
    }
}