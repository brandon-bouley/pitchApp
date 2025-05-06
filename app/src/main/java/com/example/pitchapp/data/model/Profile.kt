package com.example.pitchapp.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Profile(
    @DocumentId val userId: String = "",
    val username: String = "",
    val email: String? = null,
    val bio: String? = null,
    val followers: List<String> = emptyList(),
    val following: List<String> = emptyList(),
    val themePreference: String = "light",
    val reviewCount: Int = 0,
    val averageRating: Float = 0f,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val lastUpdated: Timestamp? = null,
    val isPublic: Boolean = true,
    @get:Exclude val recentReviews: List<Review> = emptyList(),
    val profilePictureUrl: String? = null
) {
    // Empty constructor required for Firestore deserialization
    constructor() : this(
        userId = "",
        username = "",
        email = null,
        bio = null,
        followers = emptyList(),
        following = emptyList()
    )
}