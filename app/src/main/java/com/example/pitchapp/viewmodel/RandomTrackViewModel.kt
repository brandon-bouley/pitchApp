package com.example.pitchapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pitchapp.data.model.RandomTrack
import com.example.pitchapp.data.model.Result
import com.example.pitchapp.data.model.Review
import com.example.pitchapp.data.repository.TrackReviewRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TrackReviewViewModel(
    private val repository: TrackReviewRepository
) : ViewModel() {

    private val _selectedTrack = MutableStateFlow<RandomTrack?>(null)
    val selectedTrack: StateFlow<RandomTrack?> get() = _selectedTrack

    private val _reviewText = MutableStateFlow("")
    val reviewText: StateFlow<String> get() = _reviewText

    private val _rating = MutableStateFlow(0f)
    val rating: StateFlow<Float> get() = _rating

    private val _submissionResult = MutableStateFlow<Result<Unit>?>(null)
    val submissionResult: StateFlow<Result<Unit>?> get() = _submissionResult

    fun setSelectedTrack(track: RandomTrack) {
        _selectedTrack.value = track
    }

    fun updateReviewText(text: String) {
        _reviewText.value = text
    }

    fun updateRating(value: Float) {
        _rating.value = value
    }

    fun submitReview(userId: String, username: String, onComplete: () -> Unit) {
        val track = _selectedTrack.value ?: return

        val review = Review(
            id = "",
            albumId = track.mbid ?: "${track.artist.name}-${track.name}",
            userId = userId,
            username = username,
            content = _reviewText.value,
            rating = _rating.value,
            timestamp = Timestamp.now()
        )

        viewModelScope.launch {
            val result = repository.insertTrackReview(review)
            _submissionResult.value = result
            if (result is Result.Success) {
                _reviewText.value = ""
                _rating.value = 0f
                onComplete()
            }
        }
    }

    fun clearReviewState() {
        _selectedTrack.value = null
        _reviewText.value = ""
        _rating.value = 0f
        _submissionResult.value = null
    }
}
