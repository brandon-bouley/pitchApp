package com.example.pitchapp.ui.screens.feed

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pitchapp.data.model.FeedItem
import com.example.pitchapp.ui.components.AlbumCard
import com.example.pitchapp.ui.components.ReviewCard
import com.example.pitchapp.ui.components.SectionHeader
import com.example.pitchapp.viewmodel.FeedViewModel

@Composable
fun FeedScreen(navController: NavController, viewModel: FeedViewModel = viewModel()) {
    val feedState by viewModel.feedState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadFeed()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = feedState) {
            is FeedViewModel.FeedState.Loading ->
                CircularProgressIndicator(Modifier.align(Alignment.Center))

            is FeedViewModel.FeedState.Error ->
                Text(
                    text = state.message,
                    modifier = Modifier.align(Alignment.Center)
                )

            is FeedViewModel.FeedState.Success ->
                FeedListContent(
                    items = state.items,
                    navController = navController
                )
        }
    }
}

@Composable
private fun FeedListContent(
    items: List<FeedItem>,
    navController: NavController
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(
            items = items,
            key = { item ->
                when (item) {
                    is FeedItem.SectionHeader -> "header_${item.title}"
                    is FeedItem.ReviewItem -> "review_${item.review.id}"
                    is FeedItem.AlbumItem -> "album_${item.album.id}"
                }
            }
        ) { item ->
            when (item) {
                is FeedItem.SectionHeader ->
                    SectionHeader(title = item.title)

                is FeedItem.ReviewItem ->
                    ReviewCard(
                        reviewItem = item,
                        onClick = { navController.navigate("album/${item.album?.id}") }
                    )

                is FeedItem.AlbumItem ->
                    AlbumCard(
                        albumItem = item,
                        onClick = {
                            navController.navigate("album/${item.album.id}")
                        }
                    )

            }
        }
    }
}