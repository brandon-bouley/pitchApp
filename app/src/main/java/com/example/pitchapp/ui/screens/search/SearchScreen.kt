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
import androidx.compose.material3.Button
import androidx.navigation.NavController
import com.example.pitchapp.data.model.Artist
import com.example.pitchapp.ui.components.ErrorMessage
import com.example.pitchapp.ui.navigation.Screen
import com.example.pitchapp.viewmodel.SearchViewModel
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.ui.platform.LocalContext
import com.example.pitchapp.data.model.Album
import com.example.pitchapp.data.model.FeedItem
import com.example.pitchapp.data.model.Review
import com.example.pitchapp.data.repository.MusicRepository
import com.example.pitchapp.ui.components.AlbumCard
import com.example.pitchapp.ui.components.ReviewCard
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.LaunchedEffect
import com.example.pitchapp.viewmodel.ReviewViewModel
import androidx.lifecycle.viewModelScope


@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: SearchViewModel,
    reviewViewModel: ReviewViewModel
) {
    val context = LocalContext.current
    val windowSizeClass = calculateWindowSizeClass(activity = context as Activity)
    val isCompactLayout = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact


    if (isCompactLayout) {
        CompactSearchLayout(navController, viewModel, reviewViewModel)
    } else {
        ExpandedSearchLayout(navController, viewModel, reviewViewModel)
    }
}

@Composable
private fun CompactSearchLayout(navController: NavController, viewModel: SearchViewModel, reviewViewModel: ReviewViewModel) {
    LaunchedEffect(Unit) {
        viewModel.softReset()
    }
    Column(Modifier.fillMaxSize()) {
        SearchContent(
            viewModel = viewModel,
            onAlbumSelected = { album ->
                viewModel.onAlbumClicked(navController, album)
                viewModel.setSearchType(SearchViewModel.SearchType.ALBUM)
            }
        )
    }
}

@Composable
private fun ExpandedSearchLayout(
    navController: NavController,
    viewModel: SearchViewModel,
    reviewViewModel: ReviewViewModel
) {
    Row(Modifier.fillMaxSize()) {
        Column(Modifier.weight(0.4f)) {
            SearchContent(
                viewModel = viewModel,
                onAlbumSelected = { album ->
                    viewModel.selectAlbum(album)
                }
            )
        }

        Column(Modifier.weight(0.6f)) {
            DetailPane(viewModel, navController)
        }
    }
}

@Composable
private fun SearchContent(
    viewModel: SearchViewModel,
    onAlbumSelected: (Album) -> Unit = { album ->
        viewModel.selectAlbum(album)
    }
) {
    val searchState by viewModel.searchState.collectAsState()
    val selectedArtist by viewModel.selectedArtist.collectAsState()

    LaunchedEffect(selectedArtist) {
        selectedArtist?.let { artist ->
            viewModel.getArtistTopAlbums(artist.name)
        }
    }

    Column(Modifier.fillMaxSize()) {
        SearchField(
            query = searchState.searchQuery,
            onQueryChange = viewModel::updateSearchQuery,
            searchType = searchState.searchType,
            viewModel = viewModel
        )

        Spacer(Modifier.height(8.dp))

        when {
            searchState.isLoading -> SpinningRecord()
            searchState.error != null -> ErrorMessage(
                message = searchState.error!!,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            else -> when (searchState.searchType) {
                SearchViewModel.SearchType.ARTIST -> ArtistResultsList(
                    results = searchState.results.filterIsInstance<Artist>(),
                    onSelect = { artist ->
                        viewModel.getArtistTopAlbums(artist.name)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                SearchViewModel.SearchType.ALBUM -> AlbumResultsList(
                    albums = searchState.results.filterIsInstance<Album>(),
                    onSelect = { album ->
                        onAlbumSelected(album)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun DetailPane(
    viewModel: SearchViewModel,
    navController: NavController
) {
    val searchState by viewModel.searchState.collectAsState()

    Box(Modifier.fillMaxSize()) {
        when {
            searchState.isLoading -> SpinningRecord()
            searchState.error != null -> ErrorMessage(
                message = searchState.error!!,
                modifier = Modifier.align(Alignment.Center)
            )
            searchState.selectedAlbumDetails != null -> {
                val (album, reviews) = searchState.selectedAlbumDetails!!
                Column(Modifier.fillMaxSize()) {
                    AlbumDetailContent(
                        album = album,
                        reviews = reviews,
                        modifier = Modifier.weight(1f),
                        navController = navController
                    )


                                Button(
                                onClick = { /* TODO */
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
    searchType: SearchViewModel.SearchType,
    viewModel: SearchViewModel
) {
    Column(Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = query,
            onValueChange = { newValue ->
                onQueryChange(newValue)
            },
            label = { Text(when (searchType) {
                SearchViewModel.SearchType.ARTIST -> "Search artists"
                SearchViewModel.SearchType.ALBUM -> "Search albums"
            })},
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Search
            ),
            singleLine = true
        )
    }
}

@Composable
fun AlbumDetailContent(
    album: Album,
    reviews: List<Review>,
    modifier: Modifier = Modifier,
    navController: NavController
) {
    Column(modifier.padding(16.dp)) {
        Text(album.title, style = MaterialTheme.typography.headlineSmall)
        Text(album.artist, style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(16.dp))

        NetworkImage(
            url = album.artworkUrl,
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(24.dp))

        Text("Reviews (${reviews.size})", style = MaterialTheme.typography.titleMedium)

        LazyColumn {
            items(reviews) { review ->
                ReviewCard(
                    reviewItem = FeedItem.ReviewItem(review=review, album=album),
                    onClick = {
                        navController.navigate(
                            Screen.Profile.createRoute(review.username))
                    }
                )
            }
        }
    }
}

@Composable
fun NetworkImage(
    url: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(url)
            .crossfade(true)
            .build(),
        contentDescription = null,
        modifier = modifier,
        contentScale = contentScale
    )
}




