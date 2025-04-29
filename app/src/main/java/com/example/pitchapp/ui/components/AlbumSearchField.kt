package com.example.pitchapp.ui.components

import AlbumResultsList
import ArtistResultsList
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.pitchapp.R
import com.example.pitchapp.data.model.Album
import com.example.pitchapp.data.model.Artist
import com.example.pitchapp.ui.screens.search.SpinningRecord
import com.example.pitchapp.viewmodel.SearchViewModel
import androidx.compose.foundation.layout.size
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun AlbumSearchField(
    viewModel: SearchViewModel,
    onAlbumSelected: (Album) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedAlbum by viewModel.selectedAlbum.collectAsState()
    val searchState by viewModel.searchState.collectAsState()
    val selectedArtist by viewModel.selectedArtist.collectAsState()
    val artistAlbums by viewModel.artistAlbums.collectAsState()
    val isAlbumSelected by viewModel.isAlbumSelected.collectAsState()

    if (!isAlbumSelected) {
        Column(modifier = modifier) {
            if (selectedArtist == null) {
                OutlinedTextField(
                    value = searchState.searchQuery,
                    onValueChange = { newQuery ->
                        viewModel.setSearchType(SearchViewModel.SearchType.ARTIST)
                        viewModel.updateSearchQuery(newQuery)
                    },
                    label = { Text("Search artists") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Search
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

            when {
                searchState.isLoading ->
                    SpinningRecord()

                    searchState.error != null ->
                        ErrorMessage(
                            message = searchState.error!!,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )

                    else -> ArtistResultsList(
                        results = searchState.results.filterIsInstance<Artist>(),
                        onSelect = { viewModel.selectArtist(it) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            viewModel.resetSearch()
                            viewModel.setSearchType(SearchViewModel.SearchType.ARTIST)
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to artists"
                        )
                    }

                Text(
                    text = "Albums by ${selectedArtist!!.name}",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)

                )
            }

                Spacer(modifier = Modifier.height(8.dp))

            when {
                searchState.isLoading -> {
                    SpinningRecord()
                }

                    searchState.error != null -> {
                        ErrorMessage(
                            message = searchState.error!!,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }

                    artistAlbums.isEmpty() -> {
                        Text(
                            "No albums found",
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }

                    else -> {
                        AlbumResultsList(
                            albums = artistAlbums,
                            onSelect = { album ->
                                viewModel.selectAlbum(album)
                                onAlbumSelected(album)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    } else {
        selectedAlbum?.let { album ->
            SelectedAlbumSummary(
                album = album,
                onEdit = { viewModel.resetSearch() }
            )
        }
    }
}


@Composable
fun SelectedAlbumSummary(
    album: Album,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Album artwork
        if (album.artworkUrl.isNotEmpty()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(album.artworkUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Album artwork",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(48.dp)
                    .padding(end = 8.dp)
            )
        }

        // Album info and change button
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = album.title,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "by ${album.artist}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            TextButton(onClick = onEdit) {
                Text("Change")
            }
        }
    }
}
