package com.example.pitchapp.data.repository

import com.example.pitchapp.data.model.Profile
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import com.example.pitchapp.data.model.Result
import com.example.pitchapp.data.model.Review
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.example.pitchapp.data.model.UserSummary
import kotlinx.coroutines.tasks.await


class ProfileRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun getProfile(userId: String): Profile {
        val snapshot = db.collection("users").document(userId).get().await()
        if (!snapshot.exists()) {
            throw Exception("User profile not found.")
        }
        return snapshot.toObject(Profile::class.java) ?: throw Exception("Failed to parse user profile.")
    }

    private suspend fun getReviews(userId: String): List<Review> {
        val querySnapshot = db.collection("reviews")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .await()

        return querySnapshot.toObjects(Review::class.java)
    }

    suspend fun updateProfileStats(userId: String) {
        val reviews = getReviews(userId)
        val average = reviews.map { it.rating }.average().toFloat()

        db.collection("profiles").document(userId).update(
            mapOf(
                "reviewCount" to reviews.size,
                "averageRating" to average,
                "lastUpdated" to Timestamp.now()
            )
        ).await()
    }

}