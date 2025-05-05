package com.example.pitchapp.ui.screens.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState

import coil.compose.AsyncImage
import com.example.pitchapp.data.model.Album

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import com.example.pitchapp.data.model.FeedItem
import com.example.pitchapp.ui.components.ReviewCard
import com.example.pitchapp.ui.navigation.Screen
import com.example.pitchapp.viewmodel.AlbumDetailViewModel
import com.example.pitchapp.viewmodel.ReviewViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumDetailScreen(
    albumId: String,
    viewModel: AlbumDetailViewModel,
    reviewViewModel: ReviewViewModel,
    navController: NavController
) {
    val album = viewModel.albumDetails.value
    val isLoading = viewModel.isLoading.value
    val error = viewModel.error.value
    val reviews by reviewViewModel.reviews.collectAsState()

    LaunchedEffect(albumId) {
        if (album == null || album.id != albumId) {
            viewModel.loadAlbumDetails(albumId)
        }
        reviewViewModel.loadReviews(albumId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Album Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
                floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    navController.navigate(Screen.AddReview.createRoute(albumId)) {
                        launchSingleTop = true
                    }
                },
                icon = { Icon(Icons.Default.Add, "Add Review") },
                text = { Text("Add Review") }
            )
        }
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> SpinningRecord()
                error != null -> Text("Error: $error", color = MaterialTheme.colorScheme.error)
                else -> LazyColumn(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Safe access to album
                    album?.let { nonNullAlbum ->
                        item { AlbumHeader(album = nonNullAlbum) }

                        if (nonNullAlbum.tracks.isNotEmpty()) {
                            item { Text("Track List", style = MaterialTheme.typography.titleLarge) }
                            items(nonNullAlbum.tracks) { track ->
                                TrackItem(track)
                            }
                        }

                        item { Text("Reviews", style = MaterialTheme.typography.titleLarge) }
                        items(reviews) { review ->
                            // Ensure review.albumDetails is not null
                            ReviewCard(
                                reviewItem = FeedItem.ReviewItem(
                                    review,
                                    review.albumDetails ?: nonNullAlbum // Fallback to current album
                                ),
                                onClick = {
                                    navController.navigate(
                                        Screen.Profile.createRoute(review.username)
                                    )
                                },
                            )
                        }
                    } ?: run {
                        item {
                            Text(
                                "Album information unavailable",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AlbumHeader(album: Album) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Album artwork
        AsyncImage(
            model = album.artworkUrl,
            contentDescription = "Album cover",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(300.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))


        Text(
            text = album.title,
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = album.artist,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Stats
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            StatItem("Summary", album.summary.toString())
            StatItem("Plays", album.playCount.toString())
        }
    }
}

fun formatDuration(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format(Locale.US, "%d:%02d", minutes, remainingSeconds)
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium)
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}


@Composable
private fun TrackItem(track: Album.Track) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${track.position}.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.width(32.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = formatDuration(track.duration),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}