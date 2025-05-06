package com.example.pitchapp.ui.screens.review

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.pitchapp.data.model.Result
import com.example.pitchapp.data.repository.MusicRepository
import com.example.pitchapp.ui.components.AlbumSearchField
import com.example.pitchapp.ui.screens.search.SpinningRecord
import com.example.pitchapp.viewmodel.AuthViewModel
import com.example.pitchapp.viewmodel.ReviewViewModel
import com.example.pitchapp.viewmodel.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReviewScreen(
    navController: NavController,
    reviewViewModel: ReviewViewModel,
    searchViewModel: SearchViewModel,
    authViewModel: AuthViewModel,
    musicRepository: MusicRepository,
    albumId: String?
) {
    val uiState by reviewViewModel.uiState.collectAsState()
    val userId by authViewModel.userId.collectAsState()

    LaunchedEffect(albumId) {
        albumId?.let { id ->
            when (val result = musicRepository.getAlbumFromFirestore(id)) {
                is Result.Success -> {
                    reviewViewModel.setSelectedAlbum(result.data)
                    searchViewModel.selectAlbum(result.data)
                }

                is Result.Error -> {
                    reviewViewModel.updateErrorMessage("Album load failed: ${result.exception.message}")
                }
            }
        }
    }

    Scaffold { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // ===== Header: selected album info + search field =====
            item {
                uiState.selectedAlbum?.let { album ->
                    Column {
                        Text("Reviewing:", style = MaterialTheme.typography.titleMedium)
                        Text(album.title, style = MaterialTheme.typography.bodyLarge)
                        Text(album.artist, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(16.dp))
                    }
                }

                AlbumSearchField(
                    viewModel = searchViewModel,
                    onAlbumSelected = { selectedAlbum ->
                        searchViewModel.selectAlbum(selectedAlbum)
                        reviewViewModel.setSelectedAlbum(selectedAlbum)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ===== Favorite-track dropdown =====
            uiState.selectedAlbum?.tracks
                ?.takeIf { it.isNotEmpty() }
                ?.let { tracks ->
                    item {
                        Spacer(Modifier.height(16.dp))
                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                readOnly = true,
                                value = uiState.favoriteTrack ?: "Select favorite track",
                                onValueChange = {},
                                label = { Text("Favorite Track") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                tracks.forEach { track ->
                                    DropdownMenuItem(
                                        text = { Text("${track.position}. ${track.title}") },
                                        onClick = {
                                            reviewViewModel.updateFavoriteTrack(
                                                if (uiState.favoriteTrack == track.title) null
                                                else track.title
                                            )
                                            expanded = false
                                        },
                                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                                        trailingIcon = {
                                            if (uiState.favoriteTrack == track.title) {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = "Selected",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

            // ===== Rating =====
            item {
                Spacer(Modifier.height(16.dp))
                Text("Rating:", style = MaterialTheme.typography.titleMedium)
                StarRating(
                    rating = uiState.rating,
                    onRatingChange = { reviewViewModel.updateRating(it.toFloat()) }
                )
            }

            // ===== Review text field =====
            item {
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = uiState.reviewText,
                    onValueChange = { reviewViewModel.updateReviewText(it) },
                    label = { Text("Your Review") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    singleLine = false,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Default
                    )
                )
            }

            // ===== Submit button & error message =====
            item {
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        reviewViewModel.submitReview {
                            navController.popBackStack()
                        }
                    },
                    enabled = uiState.isFormValid && !uiState.isSubmitting && userId != null
                ) {
                    if (uiState.isSubmitting) {
                        SpinningRecord()
                    } else {
                        Text("Submit Review")
                    }
                }
                uiState.errorMessage?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}


@Composable
fun StarRating(
    rating: Float,
    onRatingChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier) {
        for (i in 1..5) {
            IconButton(
                onClick = { onRatingChange(i) },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "Star $i",
                    tint = if (i <= rating) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}