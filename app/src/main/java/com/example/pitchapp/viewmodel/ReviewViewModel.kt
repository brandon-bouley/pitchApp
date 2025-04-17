package com.example.pitchapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pitchapp.data.model.Album
import com.example.pitchapp.data.model.Review
import com.example.pitchapp.data.repository.ReviewRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReviewViewModel(
    private val repository: ReviewRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    fun setSelectedAlbum(album: Album) {
        _uiState.value = _uiState.value.copy(
            selectedAlbum = album,
            errorMessage = null
        )
    }

    fun updateRating(rating: Int) {
        _uiState.value = _uiState.value.copy(
            rating = rating,
            errorMessage = null
        )
    }

    fun updateReviewText(text: String) {
        _uiState.value = _uiState.value.copy(
            reviewText = text,
            errorMessage = null
        )
    }

    fun submitReview(onSuccess: () -> Unit) {
        val currentState = _uiState.value
        if (!currentState.isFormValid) {
            _uiState.value = currentState.copy(
                errorMessage = "Please select an album and provide a rating"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = currentState.copy(isSubmitting = true)
            try {
                repository.insertReview(
                    Review(
                        albumId = currentState.selectedAlbum!!.id,
                        albumTitle = currentState.selectedAlbum.name,
                        author = "current_user", // Replace with actual user
                        content = currentState.reviewText,
                        rating = currentState.rating
                    )
                )
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isSubmitting = false,
                    errorMessage = "Failed to submit review: ${e.localizedMessage}"
                )
            }
        }
    }

    data class ReviewUiState(
        val selectedAlbum: Album? = null,
        val rating: Int = 0,
        val reviewText: String = "",
        val isSubmitting: Boolean = false,
        val errorMessage: String? = null
    ) {
        val isFormValid: Boolean
            get() = selectedAlbum != null && rating > 0 && reviewText.isNotBlank()
    }
}