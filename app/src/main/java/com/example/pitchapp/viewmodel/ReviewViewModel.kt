package com.example.pitchapp.viewmodel

import androidx.compose.runtime.collectAsState
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
import com.example.pitchapp.data.repository.ProfileRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ReviewViewModel(
    private val reviewRepository: ReviewRepository,
    private val authViewModel: AuthViewModel
) : ViewModel() {

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

    fun updateFavoriteTrack(track: String?) {
        _uiState.value = _uiState.value.copy(favoriteTrack = track)
    }


    fun updateReviewText(text: String) {
        _uiState.value = _uiState.value.copy(
            reviewText = text,
            errorMessage = null
        )
    }

    fun submitReview(onSuccess: () -> Unit) {
        val currentState = _uiState.value
        val userId = authViewModel.userId.value

//      Validate user
        if (userId == null) {
            _uiState.value = currentState.copy(
                errorMessage = "You must be logged in to submit a review"
            )
            return
        }

        // Validate form
        if (!currentState.isFormValid) {
            _uiState.value = currentState.copy(
                errorMessage = "Please select an album and provide a valid rating (0.5-5 stars)"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = currentState.copy(isSubmitting = true)

            try {
                // Get username from Firestore
                val userDoc = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .get()
                    .await()

                val username = userDoc.getString("username") ?: "Anonymous"

                val review = Review(
                    albumId = currentState.selectedAlbum!!.id,
                    userId = userId,
                    username = username,
                    content = currentState.reviewText,
                    rating = currentState.rating,
                    timestamp = Timestamp.now(),
                    likes = emptyList(),
                    albumDetails = currentState.selectedAlbum,
                    favoriteTrack = currentState.favoriteTrack
                )

                when (val result = reviewRepository.insertReview(review)) {
                    is Result.Success -> {
                        _reviews.value += review
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
        val userId = authViewModel.userId.value ?: run {
            _uiState.value = _uiState.value.copy(
                errorMessage = "You must be logged in to like reviews"
            )
            return
        }

        viewModelScope.launch {
            when (val result = reviewRepository.toggleLike(review.id, userId)) {
                is Result.Success -> {
                    _reviews.value = _reviews.value.map {
                        if (it.id == review.id) {
                            it.copy(
                                likes = if (userId in it.likes) {
                                    it.likes - userId
                                } else {
                                    it.likes + userId
                                }
                            )
                        } else it
                    }
                }

                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Failed to like review: ${result.exception.message}"
                    )
                }
            }
        }
    }

    fun updateErrorMessage(message: String) {
        _uiState.value = _uiState.value.copy(errorMessage = message)
    }

    data class ReviewUiState(
        val selectedAlbum: Album? = null,
        val rating: Float = 0f,
        val reviewText: String = "",
        val isSubmitting: Boolean = false,
        val submissionSuccess: Boolean = false,
        val errorMessage: String? = null,
        val favoriteTrack: String? = null
    ) {
        val isFormValid: Boolean
            get() = selectedAlbum != null &&
                    rating >= 0.5f &&
                    reviewText.length in 3..500
    }

    private fun Float.roundToNearestHalf(): Float = (this * 2).roundToInt() / 2f
}