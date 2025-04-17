package com.example.pitchapp.viewmodel
import android.util.Log
import androidx.lifecycle.*
import com.example.pitchapp.data.repository.MusicRepository
import kotlinx.coroutines.launch
import com.example.pitchapp.data.model.Artist
import com.example.pitchapp.data.model.Album

class MusicViewModel(private val repository: MusicRepository) : ViewModel() {
    private val _searchResults = MutableLiveData<List<Any>>()
    val searchResults: LiveData<List<Any>> = _searchResults
    private val _selectedArtist = MutableLiveData<Artist?>()
    val selectedArtist: LiveData<Artist?> = _selectedArtist

    fun selectArtist(artist: Artist) {
        _selectedArtist.postValue(artist)
        println("selected artist $artist")
        println("selected artist ${_selectedArtist.value}")
    }
    private val _selectedAlbum = MutableLiveData<Album?>()
    val selectedAlbum: LiveData<Album?> = _selectedAlbum

    fun selectAlbum(album: Album) {
        _selectedAlbum.postValue(album)
    }
    fun searchArtists(query: String) {
        viewModelScope.launch {
            try {
                val result = repository.searchArtists(query)
                if (result.isEmpty()) {
                    Log.w("VIEWMODEL_WARN", "No artists returned from API")
                }
                _searchResults.postValue(result)
            } catch (e: Exception) {
                Log.e("VIEWMODEL_ERROR", "Error in searchArtists", e)
                _searchResults.postValue(emptyList())
            }

        }

    }

    fun searchAlbums(query: String) {
        viewModelScope.launch {
            try {
                val result = repository.searchAlbums(query)
                if (result.isEmpty()) {
                    Log.w("VIEWMODEL_WARN", "No albums returned from API")
                }
                _searchResults.postValue(result)
            } catch (e: Exception) {
                Log.e("VIEWMODEL_ERROR", "Error in searchAlbums", e)
                _searchResults.postValue(emptyList())
            }
        }
    }

}
