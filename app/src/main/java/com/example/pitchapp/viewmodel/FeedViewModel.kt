package com.example.pitchapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pitchapp.data.model.FeedItem
import com.example.pitchapp.data.remote.LastFmService
import com.example.pitchapp.data.repository.FeedRepository
import com.example.pitchapp.data.repository.MusicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FeedViewModel(
    private val feedRepository: FeedRepository
) : ViewModel() {
    private val _feedState = MutableStateFlow<FeedState>(FeedState.Loading)
    val feedState: StateFlow<FeedState> = _feedState.asStateFlow()

    fun loadFeed() {
        viewModelScope.launch {
            _feedState.value = FeedState.Loading
            try {

                val recentResult = feedRepository.getRecentReviewItems()
                val popularResult = feedRepository.getPopularAlbumsFromReviews()


                val items = mutableListOf<FeedItem>().apply {
                    add(FeedItem.SectionHeader("Popular Albums"))
                    addAll(popularResult)
                    add(FeedItem.SectionHeader("Recent Reviews"))
                    addAll(
                        recentResult.map { review ->
                            Log.d("Reviews","review album details: ${review.albumDetails!!}")
                            FeedItem.ReviewItem(
                                review = review,
                                album = review.albumDetails // Should be populated in repository
                            )
                        }
                    )
                }

                _feedState.value = FeedState.Success(items)
            } catch (e: Exception) {
                _feedState.value = FeedState.Error(
                    "Failed to load feed: ${e.localizedMessage ?: "Unknown error"}"
                )
            }
        }
    }
//    fun loadRandomTopTrack() {
//        viewModelScope.launch {
//            try {
//                val topTracks = MusicRepository.getTopTracks()
//
//                if (topTracks.isNotEmpty()) {
//                    val randomTrack = topTracks.random()
//                    // Maybe you can trigger navigation or update a special "highlighted" track in UI?
//                    _feedState.value = FeedState.Success(
//                        listOf(FeedItem.SectionHeader("Random Top Track")) +
//                                listOf(FeedItem.TrackItem(randomTrack))
//                    )
//                }
//            } catch (e: Exception) {
//                _feedState.value = FeedState.Error("Failed to load top tracks: ${e.localizedMessage}")
//            }
//        }
//    }
    sealed class FeedState {
        object Loading : FeedState()
        data class Success(val items: List<FeedItem>) : FeedState()
        data class Error(val message: String) : FeedState()

    }
}