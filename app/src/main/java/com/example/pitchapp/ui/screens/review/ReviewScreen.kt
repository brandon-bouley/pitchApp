package com.example.pitchapp.ui.screens.review

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.pitchapp.data.model.Album
import com.example.pitchapp.ui.components.AlbumSearchField
import com.example.pitchapp.viewmodel.ReviewViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pitchapp.ui.screens.search.SpinningRecord
import com.example.pitchapp.viewmodel.SearchViewModel

@Composable
fun AddReviewScreen(
    navController: NavController,
    reviewViewModel: ReviewViewModel,
    searchViewModel: SearchViewModel,
    albumId: String?
) {
    val uiState       by reviewViewModel.uiState.collectAsState()
    val loadedAlbum   by searchViewModel.selectedAlbum.collectAsState()


    LaunchedEffect(albumId) {
        albumId?.let { searchViewModel.selectAlbumById(it) }
    }

    LaunchedEffect(loadedAlbum) {
        loadedAlbum?.let { reviewViewModel.setSelectedAlbum(it) }
    }

    Scaffold { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            Text("Selected Album:", style = MaterialTheme.typography.titleMedium)
            uiState.selectedAlbum?.let { album ->
                Text("${album.name} by ${album.artists.joinToString { it.name }}",
                    style = MaterialTheme.typography.bodyMedium)
            } ?: Text("No album selected", style = MaterialTheme.typography.bodyMedium)

            Spacer(Modifier.height(8.dp))

            AlbumSearchField(
                viewModel       = searchViewModel,
                onAlbumSelected = { reviewViewModel.setSelectedAlbum(it) },
                modifier        = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))
            Text("Rating:", style = MaterialTheme.typography.titleMedium)
            StarRating(
                rating         = uiState.rating,
                onRatingChange = { reviewViewModel.updateRating(it) }
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
                    reviewViewModel.submitReview { navController.popBackStack() }
                },
                enabled = uiState.isFormValid && !uiState.isSubmitting,
                modifier = Modifier.align(Alignment.End)
            ) {
                if (uiState.isSubmitting) SpinningRecord()
                else Text("Submit Review")
            }

            uiState.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}



@Composable
fun StarRating(
    rating: Int,
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