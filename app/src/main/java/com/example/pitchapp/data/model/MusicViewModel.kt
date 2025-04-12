package com.example.pitchapp.data.model
import androidx.lifecycle.*
import com.example.pitchapp.data.repository.MusicRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.pitchapp.data.repository.ReviewRepository

class MusicViewModel(private val repository: MusicRepository) : ViewModel() {
    private val _searchResults = MutableLiveData<List<Any>>()
    val searchResults: LiveData<List<Any>> = _searchResults

    fun search(query: String) {
        viewModelScope.launch {
            repository.searchMusic(query).observeForever {
                _searchResults.postValue(it)
            }
        }
    }
}
class ReviewViewModel(private val reviewRepo: ReviewRepository) : ViewModel() {

    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews

    fun loadReviews(itemId: String, itemType: String) {
        viewModelScope.launch {
            _reviews.value = reviewRepo.getReviewsForItem(itemId, itemType)
        }
    }

    fun postReview(itemId: String, itemType: String, userName: String, text: String) {
        viewModelScope.launch {
            reviewRepo.addReview(
                Review(
                    itemId = itemId,
                    itemType = itemType,
                    userName = userName,
                    reviewText = text
                )
            )
            loadReviews(itemId, itemType) // reload
        }
    }
}
