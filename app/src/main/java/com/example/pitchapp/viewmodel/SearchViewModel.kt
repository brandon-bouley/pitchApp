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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.pitchapp.data.model.Result

class SearchViewModel(
    private val musicRepository: MusicRepository,
    private val reviewRepository: ReviewRepository
) : ViewModel() {

    // State management
    private val _searchState = MutableStateFlow(SearchState())
    val searchState: StateFlow<SearchState> = _searchState.asStateFlow()

    private val _selectedAlbum = MutableStateFlow<Album?>(null)
    val selectedAlbum: StateFlow<Album?> = _selectedAlbum.asStateFlow()

    private val _selectedArtist = MutableStateFlow<Artist?>(null)
    val selectedArtist: StateFlow<Artist?> = _selectedArtist.asStateFlow()

    private val _artistAlbums = MutableStateFlow<List<Album>>(emptyList())
    val artistAlbums: StateFlow<List<Album>> = _artistAlbums.asStateFlow()

    private val _isAlbumSelected = MutableStateFlow(false)
    val isAlbumSelected: StateFlow<Boolean> = _isAlbumSelected.asStateFlow()

    private var searchJob: Job? = null

    fun updateSearchQuery(query: String) {
        _searchState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            if (query.length >= 3) {
                performSearch()
            } else {
                _searchState.update {
                    it.copy(
                        error = if (query.isNotEmpty()) "Minimum 3 characters required" else null,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun performSearch() {
        viewModelScope.launch {
            try {
                _searchState.update { it.copy(isLoading = true, error = null) }

                val result = when (_searchState.value.searchType) {
                    SearchType.ARTIST -> musicRepository.searchArtists(_searchState.value.searchQuery)
                    SearchType.ALBUM -> musicRepository.searchAlbums(_searchState.value.searchQuery)
                }

                when (result) {
                    is Result.Success -> {
                        _searchState.update {
                            it.copy(
                                results = result.data,
                                isLoading = false,
                                error = null
                            )
                        }
                    }
                    is Result.Error -> {
                        _searchState.update {
                            it.copy(
                                isLoading = false,
                                error = "Search failed: ${result.exception.message}"
                            )
                        }
                    }
                    else -> Result.Error(IllegalStateException("Unexpected result type"))
                }
            } catch (e: Exception) {
                _searchState.update {
                    it.copy(
                        isLoading = false,
                        error = "Search failed: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun selectArtist(artist: Artist) {
        _selectedArtist.value = artist
        _searchState.update {
            it.copy(
                searchQuery = "",  // Clear search query
                results = emptyList()  // Clear previous results
            )
        }
        getArtistTopAlbums(artist.name)
    }

    fun selectAlbum(album: Album) {
        viewModelScope.launch {
            _selectedAlbum.value = null
            _isAlbumSelected.value = false // Reset while loading

            when (val result = musicRepository.getOrFetchAlbum(album.artist, album.title)) {
                is Result.Success -> {
                    _selectedAlbum.value = result.data
                    _isAlbumSelected.value = true
                    loadAlbumDetails(result.data.id)
                }
                is Result.Error -> {
                    _searchState.update { it.copy(error = result.exception.message) }
                    _isAlbumSelected.value = false
                }
                else -> Result.Error(IllegalStateException("Unexpected result type"))
            }
        }
    }


    fun loadAlbumDetails(albumId: String) {
        viewModelScope.launch {
            _searchState.update { it.copy(isLoading = true, error = null) }
            println("albumId: $albumId")

            try {
                // Get album details from Firestore
                val albumResult = musicRepository.getAlbumFromFirestore(albumId)
                // Get reviews from Firestore
                val reviewsResult = reviewRepository.getReviewsForAlbum(albumId)

                when {
                    albumResult is Result.Success && reviewsResult is Result.Success -> {
                        _selectedAlbum.value = albumResult.data
                        _searchState.update {
                            it.copy(
                                selectedAlbumDetails = albumResult.data to reviewsResult.data,
                                isLoading = false
                            )
                        }
                    }
                    else -> {
                        val error = listOfNotNull(
                            (albumResult as? Result.Error)?.exception?.message,
                            (reviewsResult as? Result.Error)?.exception?.message
                        ).joinToString()

                        _searchState.update {
                            it.copy(
                                isLoading = false,
                                error = "Failed to load details: ${error.ifEmpty { "Unknown error" }}"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _searchState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load details: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun getArtistTopAlbums(artistName: String) {
        viewModelScope.launch {
            _searchState.update { it.copy(isLoading = true, error = null) }

            when (val result = musicRepository.getArtistTopAlbums(artistName)) {
                is Result.Success -> {
                    _artistAlbums.value = result.data
                    _searchState.update {
                        it.copy(
                            results = result.data,
                            isLoading = false,
                            searchType = SearchType.ALBUM
                        )
                    }
                }
                is Result.Error -> {
                    _searchState.update {
                        it.copy(
                            isLoading = false,
                            error = "Failed to load albums: ${result.exception.message}"
                        )
                    }
                }
                else -> Result.Error(IllegalStateException("Unexpected result type"))
            }
        }
    }


    data class SearchState(
        val searchQuery: String = "",
        val results: List<Any> = emptyList(),
        val selectedAlbumDetails: Pair<Album, List<Review>>? = null,
        val isLoading: Boolean = false,
        val error: String? = null,
        val searchType: SearchType = SearchType.ARTIST
    )

    enum class SearchType { ARTIST, ALBUM }

    fun setSearchType(type: SearchType) {
        _searchState.update { it.copy(searchType = type) }
    }

    fun resetSearch() {
        _selectedArtist.value = null
        _artistAlbums.value = emptyList()
        _searchState.value = SearchState()
        _isAlbumSelected.value = false
    }
}