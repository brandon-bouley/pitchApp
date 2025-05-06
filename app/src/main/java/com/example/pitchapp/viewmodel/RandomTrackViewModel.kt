//package com.example.pitchapp.viewmodel
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.pitchapp.data.model.Album
//import com.example.pitchapp.data.model.RandomTrack
//import com.example.pitchapp.data.model.Result
//import com.example.pitchapp.data.model.Review
//import com.example.pitchapp.data.model.TrackReview
//import com.example.pitchapp.data.repository.TrackReviewRepository
//import com.example.pitchapp.viewmodel.ReviewViewModel.ReviewUiState
//import com.google.firebase.Firebase
//import com.google.firebase.Timestamp
//import com.google.firebase.auth.auth
//import com.google.firebase.firestore.FirebaseFirestore
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.tasks.await
//
//class TrackReviewViewModel(
//    private val repository: TrackReviewRepository,
//    private val authViewModel: AuthViewModel
//) : ViewModel() {
//    private val _uiState = MutableStateFlow(TrackReviewUiState())
//    val uiState: StateFlow<TrackReviewUiState> = _uiState.asStateFlow()
//
//    private val _reviews = MutableStateFlow<List<TrackReview>>(emptyList())
//    val reviews: StateFlow<List<TrackReview>> = _reviews.asStateFlow()
//
//    private val _selectedTrack = MutableStateFlow<RandomTrack?>(null)
//    val selectedTrack: StateFlow<RandomTrack?> get() = _selectedTrack
//
//    private val _reviewText = MutableStateFlow("")
//    val reviewText: StateFlow<String> get() = _reviewText
//
//    private val _rating = MutableStateFlow(0f)
//    val rating: StateFlow<Float> get() = _rating
//
//    private val _submissionResult = MutableStateFlow<Result<Unit>?>(null)
//    val submissionResult: StateFlow<Result<Unit>?> get() = _submissionResult
//    fun loadReviews(trackId: String) {
//        viewModelScope.launch {
//            when (val result = repository.getReviewsForTrack(trackId)) {
//                is Result.Success -> _reviews.value = result.data
//                is Result.Error -> _uiState.value = _uiState.value.copy(
//                    errorMessage = "Failed to load reviews: ${result.exception.message}"
//                )
//                else -> throw IllegalStateException("Unexpected result type")
//            }
//        }
//    }
//
//    fun setSelectedTrack(track: RandomTrack) {
//        _selectedTrack.value = track
//    }
//
//    fun updateReviewText(text: String) {
//        _reviewText.value = text
//    }
//
//    fun updateRating(value: Float) {
//        _rating.value = value
//    }
//
//    fun submitReview( onComplete: () -> Unit) {
//        val currentState = _uiState.value
//        val track = _selectedTrack.value
//        val text = _reviewText.value
//        val stars = _rating.value
//        val userId = authViewModel.userId.value
//        if (userId == null) {
//            _uiState.value = currentState.copy(
//                errorMessage = "You must be logged in to submit a review"
//            )
//            return
//        }
//
//
//        if (track == null) {
//            _uiState.value = currentState.copy(
//                errorMessage = "You must be logged in to submit a review"
//            )
//            return
//        }
//
//        // Validate form
//        if (!currentState.isFormValid) {
//            _uiState.value = currentState.copy(
//                errorMessage = "Please provide a valid rating (0.5-5 stars) for track"
//            )
//            return
//        }
//
//        viewModelScope.launch {
//            _uiState.value = currentState.copy(isSubmitting = true)
//
//
//            try {
//                val userDoc = FirebaseFirestore.getInstance()
//                    .collection("users")
//                    .document(userId)
//                    .get()
//                    .await()
//
//                val username = userDoc.getString("username") ?: "Anonymous"
//
//                val review = TrackReview(
//                    id = "",
//                    trackId = track.mbid ?: "${track.artist.name}-${track.name}",
//                    userId = userId,
//                    username = username.ifBlank { "Anonymous" },
//                    content = text,
//                    rating = stars,
//                    timestamp = Timestamp.now()
//                )
//                when (val result = repository.insertTrackReview(review)) {
//                    is Result.Success -> {
//                        _reviews.value += review
//                        _uiState.value = currentState.copy(
//                            isSubmitting = false,
//                            submissionSuccess = true,
//                            errorMessage = null
//                        )
//                        onComplete()
//                    }
//
//                    is Result.Error -> {
//                        _uiState.value = currentState.copy(
//                            isSubmitting = false,
//                            errorMessage = "Failed to submit: ${result.exception.message ?: "Unknown error"}"
//                        )
//                    }
//
//                    else -> {
//                        _uiState.value = currentState.copy(
//                            isSubmitting = false,
//                            errorMessage = "Unexpected result type"
//                        )
//                    }
//                }
//
//
//            }catch (e: Exception){
//                _submissionResult.value = Result.Error(e)
//            }
//        }
//
//
//
//
//    }
//
//
//
//    fun clearReviewState() {
//        _selectedTrack.value = null
//        _reviewText.value = ""
//        _rating.value = 0f
//        _submissionResult.value = null
//    }
//}
//data class TrackReviewUiState(
//    val selectedTrack: RandomTrack? = null,
//    val rating: Float = 0f,
//    val reviewText: String = "",
//    val isSubmitting: Boolean = false,
//    val submissionSuccess: Boolean = false,
//    val errorMessage: String? = null
//) {
//    val isFormValid: Boolean
//        get() = selectedTrack != null &&
//                rating >= 0.5f &&
//                reviewText.length in 3..500
//}
//
