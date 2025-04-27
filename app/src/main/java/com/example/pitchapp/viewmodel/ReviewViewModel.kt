package com.example.pitchapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pitchapp.data.model.Album
import com.example.pitchapp.data.model.Review
import com.example.pitchapp.data.repository.ReviewRepository
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import com.example.pitchapp.data.model.Result

class ReviewViewModel(
    private val reviewRepository: ReviewRepository
) : ViewModel() {

    private val dummyAdminId = "admin"
    private val dummyAdminName = "PitchesLoveMySwag"

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews.asStateFlow()

    fun loadReviews(albumId: String) {
        viewModelScope.launch {
            when (val result = reviewRepository.getReviewsForAlbum(albumId)) {
                is Result.Success -> _reviews.value = result.data
                is Result.Error -> _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to load reviews: ${result.exception.message}"
                )
                else -> throw IllegalStateException("Unexpected result type")
            }
        }
    }

    fun setSelectedAlbum(album: Album) {
        _uiState.value = _uiState.value.copy(
            selectedAlbum = album,
            errorMessage = null
        )
    }

    fun updateRating(rating: Float) {
        _uiState.value = _uiState.value.copy(
            rating = rating.coerceIn(0.5f, 5.0f).roundToNearestHalf(),
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
//        val currentUser = Firebase.auth.currentUser

        val currentUser = object { // Dummy user for testing
            val uid = dummyAdminId
            val displayName = dummyAdminName
        }


        // Validate user first
        if (currentUser == null) {
            _uiState.value = currentState.copy(
                errorMessage = "You must be logged in to submit a review"
            )
            return
        }

        // Then validate form
        if (!currentState.isFormValid) {
            _uiState.value = currentState.copy(
                errorMessage = "Please select an album and provide a valid rating (0.5-5 stars)"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = currentState.copy(isSubmitting = true)

            try {
                val review = Review(
                    albumId = currentState.selectedAlbum!!.id,
                    userId = currentUser.uid,
                    username = currentUser.displayName,
                    content = currentState.reviewText,
                    rating = currentState.rating,
                    timestamp = Timestamp.now()
                )

                when (val result = reviewRepository.insertReview(review)) {
                    is Result.Success -> {
                        _uiState.value = currentState.copy(
                            isSubmitting = false,
                            submissionSuccess = true,
                            errorMessage = null
                        )
                        onSuccess()
                    }
                    is Result.Error -> {
                        _uiState.value = currentState.copy(
                            isSubmitting = false,
                            errorMessage = "Failed to submit: ${result.exception.message ?: "Unknown error"}"
                        )
                    }
                    else -> {
                        _uiState.value = currentState.copy(
                            isSubmitting = false,
                            errorMessage = "Unexpected result type"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isSubmitting = false,
                    errorMessage = "Error: ${e.localizedMessage ?: "Failed to submit review"}"
                )
            }
        }
    }

    // Add like functionality
    fun toggleLike(review: Review) {
        viewModelScope.launch {
            // For now using dummy admin ID, replace with real user ID later
            val userId = dummyAdminId
            when (val result = reviewRepository.toggleLike(review.id, userId)) {
                is Result.Success -> {
                    // Update local state if needed
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Could not update like: ${result.exception.message}"
                    )
                }
                else -> {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Unexpected result type"
                    )
                }
            }
        }
    }

    data class ReviewUiState(
        val selectedAlbum: Album? = null,
        val rating: Float = 0f,
        val reviewText: String = "",
        val isSubmitting: Boolean = false,
        val submissionSuccess: Boolean = false,
        val errorMessage: String? = null
    ) {
        val isFormValid: Boolean
            get() = selectedAlbum != null &&
                    rating >= 0.5f &&
                    reviewText.length in 10..500
    }

    private fun Float.roundToNearestHalf(): Float = (this * 2).roundToInt() / 2f
}