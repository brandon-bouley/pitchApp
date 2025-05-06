package com.example.pitchapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pitchapp.data.model.Profile
import com.example.pitchapp.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log
import com.example.pitchapp.data.model.Album
import com.example.pitchapp.data.model.Result
import com.example.pitchapp.data.repository.MusicRepository
import com.example.pitchapp.data.repository.ReviewRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

class ProfileViewModel(
    private val repository: ProfileRepository,
    private val musicRepository: MusicRepository
) : ViewModel() {
    sealed class ProfileState {
        data object Loading : ProfileState()
        data class Success(val profile: Profile) : ProfileState()
        data class Error(val message: String) : ProfileState()
    }

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    fun loadProfileByUsername(username: String) {
        viewModelScope.launch {
            try {
                Log.d("ProfileViewModel", "Loading profile for username: $username")
                _profileState.value = ProfileState.Loading
                val profile = repository.getProfileByUsername(username)
                _profileState.value = ProfileState.Success(profile)
                Log.d("ProfileViewModel", "Successfully loaded profile: ${profile.username}")
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error loading profile by username", e)
                _profileState.value = ProfileState.Error(e.message ?: "Error loading profile")
            }
        }
    }


    fun loadProfile(username: String) {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            try {
                val profile = repository.getProfileByUsername(username)

                // Process reviews with async/await
                val processedReviews = profile.recentReviews.map { review ->
                    async {
                        if (review.albumDetails?.id?.isNotEmpty() == true) {
                            review
                        } else {
                            try {
                                when (val result = musicRepository.getAlbumFromFirestore(review.albumId)) {
                                    is Result.Success -> review.copy(albumDetails = result.data)
                                    is Result.Error -> review.copy(albumDetails = null)
                                }
                            } catch (e: Exception) {
                                review.copy(
                                    albumDetails = Album(
                                        id = review.albumId,
                                        title = "Unknown Album",
                                        artist = "Unknown Artist",
                                        artworkUrl = ""
                                    )
                                )
                            }
                        }
                    }
                }.awaitAll()

                val updatedProfile = profile.copy(recentReviews = processedReviews)
                _profileState.value = ProfileState.Success(updatedProfile)

            } catch (e: Exception) {
                _profileState.value = ProfileState.Error("Failed to load profile: ${e.message}")
            }
        }
    }

    suspend fun searchUsers(query: String): List<Profile> {
        return try {
            repository.searchUsers(query)
        } catch (e: Exception) {
            Log.e("ProfileViewModel", "Error searching users", e)
            emptyList()
        }
    }

    fun toggleFollow(currentUserId: String, targetUserId: String) {
        viewModelScope.launch {
            try {
                val currentState = _profileState.value
                if (currentState is ProfileState.Success) {
                    val profile = currentState.profile
                    val isFollowing = profile.followers.contains(currentUserId)

                    if (isFollowing) {
                        repository.unfollowUser(currentUserId, targetUserId)
                    } else {
                        repository.followUser(currentUserId, targetUserId)
                    }

                    // Reload the profile to update UI
                    loadProfileByUsername(profile.username)
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error toggling follow", e)
                _profileState.value = ProfileState.Error("Follow operation failed: ${e.message}")
            }
        }
    }

    fun refreshProfile(userId: String) {
        viewModelScope.launch {
            try {
                repository.updateProfileStats(userId)
                loadProfile(userId)
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error refreshing profile", e)
                _profileState.value = ProfileState.Error("Failed to refresh: ${e.message}")
            }
        }
    }

    fun updateProfile(userId: String, updates: Map<String, Any>) {
        viewModelScope.launch {
            try {
                repository.updateProfile(userId, updates)
                loadProfile(userId)
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error updating profile", e)
                _profileState.value = ProfileState.Error("Failed to update: ${e.message}")
            }
        }
    }
}