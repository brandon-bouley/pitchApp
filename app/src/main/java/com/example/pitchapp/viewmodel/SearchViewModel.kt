package com.example.pitchapp.viewmodel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pitchapp.data.model.Album
import com.example.pitchapp.data.model.Artist
import com.example.pitchapp.data.model.Review
import com.example.pitchapp.data.repository.MusicRepository
import com.example.pitchapp.data.repository.ReviewRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel(
    private val musicRepository: MusicRepository,
    private val reviewRepository: ReviewRepository
) : ViewModel() {
    // Search state management
    private val _searchState = MutableStateFlow(SearchState())
    val searchState: StateFlow<SearchState> = _searchState.asStateFlow()

    // Selected items
    private val _selectedArtist = MutableStateFlow<Artist?>(null)
    val selectedArtist: StateFlow<Artist?> = _selectedArtist.asStateFlow()

    private val _artistAlbums = MutableStateFlow<List<Album>>(emptyList())
    val artistAlbums: StateFlow<List<Album>> = _artistAlbums.asStateFlow()

    private val _selectedAlbum = MutableStateFlow<Album?>(null)
    val selectedAlbum: StateFlow<Album?> = _selectedAlbum.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _albumDetails = mutableStateOf<Album?>(null)
    val albumDetails: State<Album?> = _albumDetails

    private val _reviews = mutableStateOf<List<Review>>(emptyList())
    val reviews: State<List<Review>> = _reviews

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    private val _searchType = MutableStateFlow(SearchType.ARTIST)
    val searchType: StateFlow<SearchType> = _searchType.asStateFlow()

    private var searchJob: Job? = null

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            if (query.length >= 3) {
                performSearch()
            } else {
                _searchState.value = SearchState().copy(
                    error = if (query.isNotEmpty()) "Minimum 3 characters required" else null
                )
            }
        }
    }

    private fun performSearch() {
        viewModelScope.launch {
            try {
                _searchState.value = SearchState(isLoading = true)

                val results = when (_searchType.value) {
                    SearchType.ARTIST -> musicRepository.searchArtists(_searchQuery.value)
                    SearchType.ALBUM -> musicRepository.searchAlbums(_searchQuery.value)
                }.map { it }

                _searchState.value = SearchState(
                    results = results,
                    isLoading = false
                )
            } catch (e: Exception) {
                Log.e("SearchViewModel", "Search failed", e)
                _searchState.value = SearchState(
                    error = "Search failed: ${e.localizedMessage}",
                    isLoading = false
                )
            }
        }
    }

    fun loadArtistAlbums(artistId: String) {
        viewModelScope.launch {
            _searchState.value = _searchState.value.copy(isLoading = true)
            try {
                val albums = musicRepository.getArtistAlbums(artistId)
                _artistAlbums.value = albums
                _searchState.value = _searchState.value.copy(
                    isLoading = false,
                    searchPhase = SearchPhase.ALBUM_SEARCH
                )
            } catch (e: Exception) {
                _searchState.value = _searchState.value.copy(
                    error = "Failed to load albums: ${e.localizedMessage}",
                    isLoading = false
                )
            }
        }
    }

    fun selectArtist(artist: Artist) {
        _selectedArtist.value = artist
        loadArtistAlbums(artist.id)
    }

    fun selectAlbum(album: Album) {
        _selectedAlbum.value = album
        Log.d("SearchViewModel", "Selected album: ${album.name}")
    }

    fun setSearchType(type: SearchType) {
        _searchType.value = type
    }

    fun selectAlbumById(albumId: String) {
        viewModelScope.launch {

            val album = artistAlbums.value.find { it.id == albumId }
                ?: when (val result = musicRepository.getAlbumDetails(albumId)) {
                    is MusicRepository.Result.Success -> result.data
                    is MusicRepository.Result.Error -> {
                        Log.e("SearchViewModel", "Failed to fetch album details", result.exception)
                        null
                    }
                }

            album?.let {
                _selectedAlbum.value = it
            }
        }
    }

    fun loadAlbumDetails(albumId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val albumResult = musicRepository.getAlbumDetails(albumId)
                val reviewsResult = reviewRepository.getReviewsForAlbum(albumId)

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
            } catch (e: Exception) {
                _error.value = "Failed to load details: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }




    data class SearchState(
        val results: List<Any> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val searchPhase: SearchPhase = SearchPhase.ARTIST_SEARCH
    )



    enum class SearchPhase {
        ARTIST_SEARCH, ALBUM_SEARCH
    }

    enum class SearchType {
        ARTIST, ALBUM
    }

    fun resetSearch() {
        _selectedArtist.value = null
        _artistAlbums.value = emptyList()
        _searchState.value = SearchState()
    }
}