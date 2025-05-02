package com.example.pitchapp.ui.screens.review

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.pitchapp.data.repository.MusicRepository
import com.example.pitchapp.data.model.RandomTrack
import com.example.pitchapp.ui.components.AlbumSearchField
import com.example.pitchapp.ui.navigation.Screen
import com.example.pitchapp.ui.screens.search.SpinningRecord
import com.example.pitchapp.viewmodel.ReviewViewModel
import com.example.pitchapp.viewmodel.SearchViewModel
import com.example.pitchapp.data.model.Result
import com.example.pitchapp.viewmodel.AuthViewModel


//set selected track to null somewhere
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
    val loadedAlbum by searchViewModel.selectedAlbum.collectAsState()
    val userId by authViewModel.userId.collectAsState()

    LaunchedEffect(loadedAlbum) {
        loadedAlbum?.let { reviewViewModel.setSelectedAlbum(it) }
    }


    LaunchedEffect(albumId) {
        albumId?.let {
            // Handle the Result type properly
            when(val result = musicRepository.getAlbumFromFirestore(it)) {
                is Result.Success -> {
                    result.data.let { album ->
                        reviewViewModel.setSelectedAlbum(album)
                    }
                }
                is Result.Error -> {
                    reviewViewModel.updateErrorMessage("Failed to load album: ${result.exception.message}")
                    loadedAlbum?.let { it1 -> reviewViewModel.setSelectedAlbum(it1) }
                }
            }
        }
    }

    Scaffold { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
                Text("Selected Album:", style = MaterialTheme.typography.titleMedium)
                uiState.selectedAlbum?.let { album ->
                    Text(
                        text = "${album.title} by ${album.artist}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } ?: Text("No album selected", style = MaterialTheme.typography.bodyMedium)

                Spacer(Modifier.height(8.dp))


                AlbumSearchField(
                    viewModel = searchViewModel,
                    onAlbumSelected = { searchViewModel.selectAlbum(it) },
                    modifier = Modifier.fillMaxWidth()
                )


            Spacer(Modifier.height(16.dp))
            Text("Rating:", style = MaterialTheme.typography.titleMedium)
            StarRating(
                rating = uiState.rating,
                onRatingChange = { reviewViewModel.updateRating(it.toFloat()) }
            )

            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value            = uiState.reviewText,
                onValueChange    = { reviewViewModel.updateReviewText(it) },
                label            = { Text("Your Review") },
                modifier         = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                singleLine       = false,
                keyboardOptions  = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Text,
                    imeAction    = ImeAction.Default
                )
            )

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    reviewViewModel.submitReview {
                        val albumId = uiState.selectedAlbum?.id
                        if (albumId != null) {
                            navController.navigate(Screen.AlbumDetail.createRoute(albumId))
                        }
                    }
                },
                enabled = uiState.isFormValid && !uiState.isSubmitting && userId != null,
                modifier = Modifier.align(Alignment.End)
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