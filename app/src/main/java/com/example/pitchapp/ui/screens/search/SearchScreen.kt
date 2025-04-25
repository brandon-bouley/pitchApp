package com.example.pitchapp.ui.screens.search

import AlbumResultsList
import ArtistResultsList
import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.pitchapp.data.model.Artist
import com.example.pitchapp.ui.components.ErrorMessage
import com.example.pitchapp.ui.navigation.Screen
import com.example.pitchapp.viewmodel.SearchViewModel
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.ui.platform.LocalContext
import com.example.pitchapp.data.model.Album

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: SearchViewModel,
) {
    val context = LocalContext.current
    val windowSizeClass = if (context is Activity) {
        calculateWindowSizeClass(context)
    } else {
        throw IllegalStateException("Context is not an Activity")
    }

    val isCompactLayout = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact

    if (isCompactLayout) {
        CompactSearchLayout(navController, viewModel)
    } else {
        ExpandedSearchLayout(navController, viewModel)
    }
}

@Composable
private fun CompactSearchLayout(navController: NavController, viewModel: SearchViewModel) {
    Column(Modifier.fillMaxSize()) {
        // Existing phone layout
        SearchContent(navController, viewModel)
    }
}

@Composable
private fun ExpandedSearchLayout(
    navController: NavController,
    viewModel: SearchViewModel
) {
    Row(Modifier.fillMaxSize()) {
        // Left panel (40% width)
        Column(
            Modifier
                .weight(0.4f)
                .padding(end = 8.dp)
        ) {
            SearchContent(
                navController = null,
                viewModel = viewModel,
                onAlbumSelected = { album ->
                    viewModel.selectAlbum(album)
                    viewModel.loadAlbumDetails(album.id)
                }
            )
        }

        // Right panel (60% width)
        Column(
            Modifier
                .weight(0.6f)
                .padding(start = 8.dp)
        ) {
            DetailPane(navController, viewModel) // Pass navController here
        }
    }
}

@Composable
private fun SearchContent(
    navController: NavController?,
    viewModel: SearchViewModel,
    onAlbumSelected: (Album) -> Unit = { album ->
        navController?.navigate(Screen.AlbumDetail.createRoute(album.id))
    }
) {
    val searchState by viewModel.searchState.collectAsState()
    val selectedArtist by viewModel.selectedArtist.collectAsState()
    val artistAlbums by viewModel.artistAlbums.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Column(Modifier.fillMaxSize()) {
        if (searchState.searchPhase == SearchViewModel.SearchPhase.ALBUM_SEARCH) {
            IconButton(
                onClick = { viewModel.resetSearch() },
                modifier = Modifier.align(Alignment.Start)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back to artist search")
            }
        }

        SearchField(
            query = searchQuery,
            onQueryChange = viewModel::updateSearchQuery,
            searchPhase = searchState.searchPhase,
            selectedArtist = selectedArtist
        )

        Spacer(Modifier.height(8.dp))

        when {
            searchState.isLoading -> SpinningRecord()
            searchState.error != null -> ErrorMessage(
                message = searchState.error!!,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            searchState.searchPhase == SearchViewModel.SearchPhase.ARTIST_SEARCH -> {
                ArtistResultsList(
                    results = searchState.results.filterIsInstance<Artist>(),
                    onSelect = { viewModel.selectArtist(it) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            searchState.searchPhase == SearchViewModel.SearchPhase.ALBUM_SEARCH -> {
                AlbumResultsList(
                    albums = artistAlbums,
                    onSelect = onAlbumSelected,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun DetailPane(
    navController: NavController,
    viewModel: SearchViewModel
) {
    val albumDetails by viewModel.albumDetails
    val reviews by viewModel.reviews
    val isLoading by viewModel.isLoading
    val error by viewModel.error

    Box(Modifier.fillMaxSize()) {
        when {
            isLoading -> SpinningRecord()
            error != null -> ErrorMessage(
                message = error ?: "Unknown error",
                modifier = Modifier.align(Alignment.Center)
            )
            albumDetails != null -> {
                Column(Modifier.fillMaxSize()) {
                    AlbumDetailContent(
                        album = albumDetails!!,
                        reviews = reviews
                    )

                    // Add the review button here
                    Button(
                        onClick = {
                            navController.navigate(
                                Screen.AddReview.createRoute(albumDetails!!.id)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text("Leave a Review")
                    }
                }
            }
            else -> Text(
                "Select an album to view details",
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}


@Composable
fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    searchPhase: SearchViewModel.SearchPhase,
    selectedArtist: Artist?
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        when (searchPhase) {
            SearchViewModel.SearchPhase.ARTIST_SEARCH -> {
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    label = { Text("Search artists") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Search
                    ),
                    singleLine = true
                )
            }
            SearchViewModel.SearchPhase.ALBUM_SEARCH -> {
                Text(
                    text = "Albums by: ${selectedArtist?.name ?: ""}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}
