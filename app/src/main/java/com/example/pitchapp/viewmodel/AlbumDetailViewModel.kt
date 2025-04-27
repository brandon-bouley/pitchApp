package com.example.pitchapp.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pitchapp.data.model.Album
import com.example.pitchapp.data.model.Review
import com.example.pitchapp.data.repository.MusicRepository
import com.example.pitchapp.data.repository.ReviewRepository
import kotlinx.coroutines.launch
import com.example.pitchapp.data.model.Result


class AlbumDetailViewModel(
    private val musicRepository: MusicRepository,
    private val reviewRepository: ReviewRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Get album ID from navigation arguments
    private val albumId: String? = savedStateHandle.get<String>("albumId")

    private val _albumDetails = mutableStateOf<Album?>(null)
    val albumDetails: State<Album?> = _albumDetails

    private val _reviews = mutableStateOf<List<Review>>(emptyList())
    val reviews: State<List<Review>> = _reviews

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    init {
        if (albumId == null) {
            _error.value = "Invalid album ID"
        } else {
            loadAlbumDetails(albumId)
        }
    }

    fun refresh() {
        albumId?.let { loadAlbumDetails(it) }
    }

    fun loadAlbumDetails(albumId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = musicRepository.getAlbumFromFirestore(albumId)) {
                is Result.Success -> {
                    _albumDetails.value = result.data
                    loadReviews(albumId)
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

    private fun loadReviews(albumId: String) {
        viewModelScope.launch {
            try {
                when (val reviewsResult = reviewRepository.getReviewsForAlbum(albumId)) {
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