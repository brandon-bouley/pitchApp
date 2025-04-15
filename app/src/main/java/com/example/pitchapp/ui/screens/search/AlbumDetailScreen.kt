package com.example.pitchapp.ui.screens.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.pitchapp.data.model.Album
import com.example.pitchapp.data.remote.BuildApi
import com.example.pitchapp.data.repository.MusicRepository
import com.example.pitchapp.ui.components.ErrorMessage
import com.example.pitchapp.viewmodel.AlbumDetailViewModel
import com.example.pitchapp.AlbumDetailViewModelFactory
import com.example.pitchapp.data.model.FeedItem
import com.example.pitchapp.data.model.Review
import com.example.pitchapp.ui.components.ReviewCard
import com.example.pitchapp.ui.navigation.Screen


@Composable
fun AlbumDetailScreen(
    albumId: String,
    navController: NavController,
    viewModel: AlbumDetailViewModel
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
        }
    }

    LaunchedEffect(albumId) {
        viewModel.loadAlbumDetails(albumId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {

        when {
            viewModel.isLoading.value -> CircularProgressIndicator()
            viewModel.error.value != null -> ErrorMessage(
                message = viewModel.error.value!!,
                modifier = Modifier.fillMaxSize()
            )

            viewModel.albumDetails.value != null -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    AlbumDetailContent(
                        album = viewModel.albumDetails.value!!,
                        reviews = viewModel.reviews.value
                    )


                    Button(
                        onClick = {
                            navController.navigate(Screen.AddReview.createRoute(albumId))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text("Leave a Review")
                    }
                }
            }
        }

    }
}

@Composable
fun AlbumDetailContent(album: Album, reviews: List<Review>) {
    LazyColumn(modifier = Modifier.padding(16.dp)) {
        item {
            Text(album.name, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text("by ${album.artists.joinToString { it.name }}", style = MaterialTheme.typography.titleMedium)
        }

        item {
            DetailRow("Release Date", album.formattedReleaseDate)
            DetailRow("Label", album.label)
            DetailRow("Total Tracks", album.totalTracks.toString())
            DetailRow("Album Type", album.albumType.replaceFirstChar { it.uppercase() })
        }

        item {
            Text("Identifiers", style = MaterialTheme.typography.titleMedium)
            DetailRow("ISRC", album.isrc ?: "N/A")
            DetailRow("EAN", album.ean ?: "N/A")
            DetailRow("UPC", album.upc ?: "N/A")
        }

        item {
            Spacer(Modifier.height(16.dp))
            Text("User Reviews", style = MaterialTheme.typography.headlineSmall)
        }

        if (reviews.isEmpty()) {
            item {
                Text("No reviews yet. Be the first!", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            items(reviews) { review ->
                ReviewCard(
                    reviewItem = FeedItem.ReviewItem(review = review, album = album),
                    onClick = { /* Handle review click */ }
                )
            }
        }
    }
}


@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}