package com.example.pitchapp.ui.screens.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import android.util.Log
import androidx.compose.material3.Scaffold

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter

import com.example.pitchapp.data.model.Artist
import com.example.pitchapp.data.model.FeedItem
import com.example.pitchapp.ui.components.AlbumCard
import com.example.pitchapp.viewmodel.SearchViewModel
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import com.example.pitchapp.R
import androidx.compose.foundation.clickable
import com.example.pitchapp.data.model.Album
import com.example.pitchapp.data.model.UserSummary








@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(navController: NavController, viewModel: SearchViewModel = viewModel()) {
    val searchState by viewModel.searchState.collectAsState()
    val results = searchState.results

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Search Results", style = MaterialTheme.typography.titleLarge)
                }
            )
        }
    ) { padding ->
        if (results.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text("No results found.")
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(padding)
            ) {
                items(results.size) { index ->
                    when (val item = results[index]) {
                        is Album -> AlbumCard(
                            albumItem = FeedItem.AlbumItem(item),
                            onClick = {
                                navController.navigate("album/${item.id}")
                            }
                        )
                        is UserSummary -> Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { navController.navigate("profile/${item.uid}") }
                                .padding(8.dp),
                            colors = CardDefaults.cardColors()
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(item.username, style = MaterialTheme.typography.titleMedium)
                            }
                        }
                        else -> Text("Unknown item type")
                    }
                }
            }
        }
    }
}
@Composable
fun SpinningRecord() {
    // Infinite rotation animation
    val infiniteTransition = rememberInfiniteTransition()

    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center

    ) {
        Image(
            painter = painterResource(id = R.drawable.record_icon),
            contentDescription = "Spinning Record",
            modifier = Modifier
                .rotate(angle)
                .size(100.dp),
        )
    }
}
