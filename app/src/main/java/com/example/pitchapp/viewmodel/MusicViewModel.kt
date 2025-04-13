package com.example.pitchapp.viewmodel
import android.util.Log
import androidx.lifecycle.*
import com.example.pitchapp.data.repository.MusicRepository
import kotlinx.coroutines.launch

class MusicViewModel(private val repository: MusicRepository) : ViewModel() {
    private val _searchResults = MutableLiveData<List<Any>>()
    val searchResults: LiveData<List<Any>> = _searchResults
    fun searchArtists(query: String) {
        viewModelScope.launch {
            try {
                Log.d("DEBUG", "searchAlbums called with query: $query")
                val artistResult = repository.searchArtists(query)
                Log.d("DEBUG", "Got artist result: $artistResult")
                _searchResults.postValue(artistResult)
            } catch (e: Exception) {
                Log.e("DEBUG", "searchAlbums failed", e)
            }
        }
    }

    fun searchAlbums(query: String) {
        viewModelScope.launch {
            try {
                Log.d("DEBUG", "searchAlbums called with query: $query")
                val albumResult = repository.searchAlbums(query)
                Log.d("DEBUG", "Got album result: $albumResult")
                _searchResults.postValue(albumResult)
            } catch (e: Exception) {
                Log.e("DEBUG", "searchAlbums failed", e)
            }
        }
    }
}