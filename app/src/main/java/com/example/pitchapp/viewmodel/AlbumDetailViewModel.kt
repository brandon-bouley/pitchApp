package com.example.pitchapp.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pitchapp.data.model.Album
import com.example.pitchapp.data.model.Review
import com.example.pitchapp.data.repository.MusicRepository
import com.example.pitchapp.data.repository.ReviewRepository
import kotlinx.coroutines.launch

class AlbumDetailViewModel(
    private val musicRepository: MusicRepository,
    private val reviewRepository: ReviewRepository
) : ViewModel() {
    private val _albumDetails = mutableStateOf<Album?>(null)
    val albumDetails: State<Album?> = _albumDetails

    private val _reviews = mutableStateOf<List<Review>>(emptyList())
    val reviews: State<List<Review>> = _reviews


    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    fun loadAlbumDetails(albumId: String) {
        viewModelScope.launch {
            _isLoading.value = true

            val albumResult = musicRepository.getAlbumDetails(albumId)
            val reviewsResult = try {
                reviewRepository.getReviewsForAlbum(albumId)
            } catch (e: Exception) {
                emptyList()
            }

            when (albumResult) {
                is MusicRepository.Result.Success -> {
                    _albumDetails.value = albumResult.data
                    _error.value = null
                }
                is MusicRepository.Result.Error -> {
                    _error.value = albumResult.exception.localizedMessage
                }
            }

            _reviews.value = reviewsResult
            _isLoading.value = false
        }
    }
}