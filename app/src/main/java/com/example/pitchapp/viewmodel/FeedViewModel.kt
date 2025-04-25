package com.example.pitchapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pitchapp.data.model.FeedItem
import com.example.pitchapp.data.repository.FeedRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FeedViewModel(
    private val feedRepository: FeedRepository
) : ViewModel() {
    private val _feedState = MutableStateFlow<FeedState>(FeedState.Loading)
    val feedState: StateFlow<FeedState> = _feedState.asStateFlow()

    init {
        loadFeed()
    }

    fun loadFeed() {
        viewModelScope.launch {
            _feedState.value = FeedState.Loading
            try {
                val popularAlbums = feedRepository.getPopularAlbumsFromReviews()
                val recentReviews = feedRepository.getRecentReviewItems()


                _feedState.value = FeedState.Success(
                    listOf(
                        FeedItem.SectionHeader("Popular Albums"),
                        *popularAlbums.toTypedArray(),
                        FeedItem.SectionHeader("Recent Reviews"),
                        *recentReviews.toTypedArray()
                    )
                )
            } catch (e: Exception) {
                _feedState.value = FeedState.Error(e.message ?: "Unknown error")
            }
        }
    }

    sealed class FeedState {
        object Loading : FeedState()
        data class Success(val items: List<FeedItem>) : FeedState()
        data class Error(val message: String) : FeedState()
    }
}