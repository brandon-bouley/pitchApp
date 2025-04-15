package com.example.pitchapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pitchapp.data.local.UserPreferenceDao
import com.example.pitchapp.data.model.Review
import com.example.pitchapp.data.model.UserPreference
import com.example.pitchapp.data.repository.ReviewRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userPrefDao: UserPreferenceDao,
    private val reviewRepo: ReviewRepository
) : ViewModel() {

    private val _user = MutableStateFlow<UserPreference?>(null)
    val user: StateFlow<UserPreference?> = _user.asStateFlow()

    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews.asStateFlow()

    fun loadProfile(username: String) {
        viewModelScope.launch {
            _user.value    = userPrefDao.getPreference(username)
            _reviews.value = reviewRepo.getReviewsByUser(username)
        }
    }
}
