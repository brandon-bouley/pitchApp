package com.example.pitchapp.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pitchapp.data.model.Profile
import com.example.pitchapp.data.model.Result
import com.example.pitchapp.data.model.Review
import com.example.pitchapp.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await



class ProfileViewModel(
    private val repository: ProfileRepository


) : ViewModel() {
    sealed class ProfileState {
        data object Loading : ProfileState()
        data class Success(val profile: Profile) : ProfileState()
        data class Error(val message: String) : ProfileState()
    }

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val profileState: StateFlow<ProfileState> = _profileState

    fun loadProfile(userId: String) {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            try {
                println("Attempting to load profile for user ID: $userId")
                val profile = repository.getProfile(userId)
                println("Successfully loaded profile: $profile")
                _profileState.value = ProfileState.Success(profile)
            } catch (e: Exception) {
                println("Error loading profile: ${e.stackTraceToString()}")
                _profileState.value = ProfileState.Error(
                    message = e.message ?: "Failed to load profile"
                )
            }
        }
    }

    fun refreshProfile(userId: String) {
        viewModelScope.launch {
            try {
                repository.updateProfileStats(userId)
                loadProfile(userId)
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error("Failed to refresh: ${e.message}")
            }
        }
    }



    suspend fun updateProfile(userId: String, newBio: String, newTheme: String) {
        try {
            val db = FirebaseFirestore.getInstance()
            db.collection("users")
                .document(userId)
                .update(
                    mapOf(
                        "bio" to newBio,
                        "themePreference" to newTheme,
                        "updatedAt" to com.google.firebase.Timestamp.now()
                    )
                )
                .await()
        } catch (e: Exception) {
            // Handle error if needed
        }
    }

        fun followUser(
            currentUserId: String,
            targetUserId: String,
            onSuccess: () -> Unit,
            onFailure: (String) -> Unit
        ) {
            val db = FirebaseFirestore.getInstance()
            val currentUserRef = db.collection("users").document(currentUserId)
            val targetUserRef = db.collection("users").document(targetUserId)

            db.runBatch { batch ->
                batch.update(currentUserRef, "following", FieldValue.arrayUnion(targetUserId))
                batch.update(targetUserRef, "followers", FieldValue.arrayUnion(currentUserId))
            }.addOnSuccessListener {
                onSuccess()
            }.addOnFailureListener { e ->
                onFailure(e.message ?: "Follow failed")
            }
        }

    fun updateThemePreference(userId: String, theme: String) {
        viewModelScope.launch {
            FirebaseFirestore.getInstance().collection("users")
                .document(userId)
                .update("themePreference", theme)
                .await()
            refreshProfile(userId)
        }
    }

    fun updatePrivacyPreference(userId: String, isPublic: Boolean) {
        viewModelScope.launch {
            FirebaseFirestore.getInstance().collection("users")
                .document(userId)
                .update("isPublic", isPublic)
                .await()
            refreshProfile(userId)
        }
    }

    fun updateBio(userId: String, newBio: String) {
        viewModelScope.launch {
            FirebaseFirestore.getInstance().collection("users")
                .document(userId)
                .update("bio", newBio)
                .await()
            refreshProfile(userId)
        }
    }

        fun unfollowUser(
            currentUserId: String,
            targetUserId: String,
            onSuccess: () -> Unit,
            onFailure: (String) -> Unit
        ) {
            val db = FirebaseFirestore.getInstance()
            val currentUserRef = db.collection("users").document(currentUserId)
            val targetUserRef = db.collection("users").document(targetUserId)

            db.runBatch { batch ->
                batch.update(currentUserRef, "following", FieldValue.arrayRemove(targetUserId))
                batch.update(targetUserRef, "followers", FieldValue.arrayRemove(currentUserId))
            }.addOnSuccessListener {
                onSuccess()
            }.addOnFailureListener { e ->
                onFailure(e.message ?: "Unfollow failed")
            }
        }
}



