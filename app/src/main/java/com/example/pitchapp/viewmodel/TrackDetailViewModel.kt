package com.example.pitchapp.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pitchapp.data.model.RandomTrack
import com.example.pitchapp.data.model.Track
import com.example.pitchapp.data.model.Review
import com.example.pitchapp.data.repository.MusicRepository

import kotlinx.coroutines.launch
import com.example.pitchapp.data.model.Result
import com.example.pitchapp.data.model.TrackReview
import com.example.pitchapp.data.repository.TrackReviewRepository


class TrackDetailViewModel(
    private val musicRepository: MusicRepository,
    private val trackRepository: TrackReviewRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val trackId: String? = savedStateHandle.get<String>("trackId")

    private val _trackDetails = mutableStateOf<Track?>(null)
    val trackDetails: State<Track?> = _trackDetails

    private val _reviews = mutableStateOf<List<TrackReview>>(emptyList())
    val reviews: State<List<TrackReview>> = _reviews

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    init {
        if (trackId == null) {
            _error.value = "On init: Invalid track ID"
        } else {
            loadTrackDetails(trackId)
        }
    }

    fun refresh() {
        trackId?.let { loadTrackDetails(it) }
    }

    fun clearState() {
        _trackDetails.value = null
        _reviews.value = emptyList()
        _error.value = null
    }

    fun loadTrackDetails(trackId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = musicRepository.getTrackFromFirestore(trackId)) {
                is Result.Success -> {
                    _trackDetails.value = result.data
                    loadReviews(trackId)
                }
                is Result.Error -> {
                    _error.value = result.exception.message
                }
                else -> {
                    _error.value = "Unexpected result type: ${result.javaClass.simpleName}"
                }
            }
            _isLoading.value = false
        }
    }

    fun loadReviews(trackId: String) {
        viewModelScope.launch {
            try {
                when (val reviewsResult = trackRepository.getReviewsForTrack(trackId)) {
                    is Result.Success -> _reviews.value = reviewsResult.data
                    is Result.Error -> _error.value = reviewsResult.exception.message
                    else -> {
                        // Handle unexpected state
                        _error.value = "Unexpected result type: ${reviewsResult.javaClass.simpleName}"
                    }
                }
            } catch (_: Exception) {}
        }
    }
}