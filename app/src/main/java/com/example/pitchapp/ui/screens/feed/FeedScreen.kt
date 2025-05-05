package com.example.pitchapp.ui.screens.feed


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pitchapp.data.model.FeedItem
import com.example.pitchapp.ui.components.AlbumCard
import com.example.pitchapp.ui.components.RandomTrackCard
import com.example.pitchapp.ui.components.ReviewCard
import com.example.pitchapp.ui.screens.search.SpinningRecord
import com.example.pitchapp.viewmodel.FeedViewModel
import com.example.pitchapp.viewmodel.ReviewViewModel


@Composable
fun FeedScreen(navController: NavController, viewModel: FeedViewModel = viewModel(), reviewViewModel: ReviewViewModel) {
    val feedState by viewModel.feedState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadFeed()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = feedState) {
            is FeedViewModel.FeedState.Loading ->
                SpinningRecord()

            is FeedViewModel.FeedState.Error ->
                Text(
                    text = state.message,
                    modifier = Modifier.align(Alignment.Center)
                )

            is FeedViewModel.FeedState.Success ->
                FeedListContent(
                    items = state.items,
                    navController = navController,
                    reviewViewModel = reviewViewModel
                )
        }
    }
}

@Composable
private fun FeedListContent(
    items: List<FeedItem>,
    navController: NavController,
    reviewViewModel: ReviewViewModel
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(
            items = items,
            key = { item ->
                when (item) {
                    is FeedItem.SectionHeader -> "header_${item.title}"
                    is FeedItem.ReviewItem -> "review_${item.review?.id}"
                    is FeedItem.AlbumItem -> "album_${item.album.id}"
                    is FeedItem.TrackItem -> "track_${item.track.mbid}"
                }
            }
        ) { item ->
            when (item) {
                is FeedItem.SectionHeader ->
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )

                is FeedItem.ReviewItem ->
                    ReviewCard(
                        reviewItem = item,
                        onClick = { navController.navigate("album/${item.review?.albumId}") },
                    )

                is FeedItem.AlbumItem ->
                    AlbumCard(
                        albumItem = item,
                        onClick = {
                            navController.navigate("album/${item.album.id}")
                        }
                    )
                is FeedItem.TrackItem ->
                    RandomTrackCard(
                        trackItem = item,
                    )


            }
        }
    }
}