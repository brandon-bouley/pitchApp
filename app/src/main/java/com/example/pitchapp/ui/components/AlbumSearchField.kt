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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.pitchapp.R
import com.example.pitchapp.data.model.Album
import com.example.pitchapp.data.model.Artist
import com.example.pitchapp.ui.screens.search.SpinningRecord
import com.example.pitchapp.viewmodel.SearchViewModel

@Composable
fun AlbumSearchField(
    viewModel: SearchViewModel,
    onAlbumSelected: (Album) -> Unit,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchState by viewModel.searchState.collectAsState()
    val selectedArtist by viewModel.selectedArtist.collectAsState()
    val artistAlbums by viewModel.artistAlbums.collectAsState()

    Column(modifier = modifier) {
        if (selectedArtist == null) {
            OutlinedTextField(
                value = searchQuery,
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
                    onClick = { viewModel.resetSearch() },
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
                        onSelect = onAlbumSelected,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
