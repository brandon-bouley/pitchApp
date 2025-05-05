//package com.example.pitchapp.data.model
//
//import com.google.firebase.Timestamp
//import com.google.firebase.firestore.DocumentId
//import com.google.firebase.firestore.Exclude
//
//data class TrackReview(
//    @DocumentId val id: String = "",
//    val trackId: String,
//    val userId: String,
//    val username: String,
//    val content: String,
//    val reviews: List<TrackReviews>
//    val rating: Float,
//    val timestamp: Timestamp = Timestamp.now(),
//    val likes: List<String> = emptyList(), //  user IDs who liked this review
//    val likeCount: Int = 0, // Derived field for querying/sorting
//
//    @Exclude
//    val trackDetails: RandomTrack? = null
//) {
//    init {
//        require(rating in 0.5f..5.0f) { "Rating must be between 0.5 and 5.0" }
//        require(rating % 0.5f == 0f) { "Rating must be in half-star increments" }
//    }
//
//    // Firestore serialization helper
//    fun toTrackFirestoreMap(): Map<String, Any> = mapOf(
//        "trackId" to trackId,
//        "userId" to userId,
//        "username" to username,
//        "content" to content,
//        "rating" to rating,
//        "timestamp" to timestamp,
//        "likes" to likes,
//        "likeCount" to likes.size
//    )
//}