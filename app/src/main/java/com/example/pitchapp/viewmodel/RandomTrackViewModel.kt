package com.example.pitchapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pitchapp.data.model.RandomTrack
import com.example.pitchapp.data.model.Result
import com.example.pitchapp.data.model.Review
import com.example.pitchapp.data.repository.TrackReviewRepository
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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
        val track = _selectedTrack.value
        val text = _reviewText.value
        val stars = _rating.value

        if (track == null) {
            _submissionResult.value = Result.Error(
                IllegalArgumentException("You must select a track before submitting a review")
            )
            return
        }

        if (text.isBlank() || stars < 0.5f) {
            _submissionResult.value = Result.Error(
                IllegalArgumentException("Please write a review and select a rating of at least 0.5 stars")
            )
            return
        }
        viewModelScope.launch {
            try {

                val review = Review(
                    id = "",
                    albumId = track.mbid ?: "${track.artist.name}-${track.name}",
                    userId = userId,
                    username = username.ifBlank { "Anonymous" },
                    content = text,
                    rating = stars,
                    timestamp = Timestamp.now()
                )
                val result = repository.insertTrackReview(review)
                _submissionResult.value = result

                if (result is Result.Success) {
                    _reviewText.value = ""
                    _rating.value = 0f
                    onComplete()
                }


            }catch (e: Exception){
                _submissionResult.value = Result.Error(e)
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
