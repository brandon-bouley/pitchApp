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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repository: ProfileRepository

) : ViewModel() {

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val profileState: StateFlow<ProfileState> = _profileState

    fun loadProfile(userId: String) {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            try {
                val profile = repository.getProfile(userId)
                _profileState.value = ProfileState.Success(profile)
            } catch (e: Exception) {
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

    sealed class ProfileState {
        data object Loading : ProfileState()
        data class Success(val profile: Profile) : ProfileState()
        data class Error(val message: String) : ProfileState()
    }
}