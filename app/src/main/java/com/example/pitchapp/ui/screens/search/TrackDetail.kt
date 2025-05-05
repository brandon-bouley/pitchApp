//package com.example.pitchapp.ui.screens.search
//
//import com.example.pitchapp.RandomTrackViewModelFactory
//import com.example.pitchapp.data.model.RandomTrack
//import com.example.pitchapp.viewmodel.TrackDetailViewModel
//import com.example.pitchapp.viewmodel.TrackReviewViewModel
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material3.Card
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.unit.dp
//import androidx.compose.runtime.collectAsState
//
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.automirrored.filled.ArrowBack
//import androidx.compose.material.icons.filled.Add
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material3.CircularProgressIndicator
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.ExtendedFloatingActionButton
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.TopAppBar
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.ui.text.style.TextAlign
//import androidx.navigation.NavController
//import com.example.pitchapp.data.model.FeedItem
//import com.example.pitchapp.ui.components.RandomTrackCard
//import com.example.pitchapp.ui.components.ReviewCard
//import com.example.pitchapp.ui.navigation.Screen
//import com.example.pitchapp.viewmodel.ReviewViewModel
//import java.util.Locale
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun TrackDetailScreen(
//    trackId: String,
//    viewModel: TrackDetailViewModel,
//    reviewViewModel: ReviewViewModel,
//    navController: NavController
//) {
//    val track = viewModel.trackDetails.value
//    val isLoading = viewModel.isLoading.value
//    val error = viewModel.error.value
//    val reviews by reviewViewModel.reviews.collectAsState()
//
//    LaunchedEffect(trackId) {
//        if (track == null || track.mbid != trackId) {
//            viewModel.loadTrackDetails(trackId)
//        }
//        reviewViewModel.loadReviews(trackId)
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Track Details") },
//                navigationIcon = {
//                    IconButton(onClick = {
//                        navController.navigate(Screen.Search.route) {
//                            popUpTo("search_root") {
//                                inclusive = true
//                            }
//                        }
//                        viewModel.clearState()
//                    }) {
//                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
//                    }
//                }
//            )
//        },
//        floatingActionButton = {
//            ExtendedFloatingActionButton(
//                onClick = {
//                    navController.navigate(Screen.AddReview.createRoute(trackId)) {
//                        launchSingleTop = true
//                    }
//                },
//                icon = { Icon(Icons.Default.Add, "Add Review") },
//                text = { Text("Add Review") }
//            )
//        }
//    ) { padding ->
//        Surface(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(padding)
//        ) {
//            when {
//                isLoading -> SpinningRecord()
//                error != null -> Text("Error: $error", color = MaterialTheme.colorScheme.error)
//                else -> LazyColumn(
//                    modifier = Modifier.padding(16.dp),
//                    verticalArrangement = Arrangement.spacedBy(16.dp)
//                ) {
//
//
//                        item { Text("Reviews", style = MaterialTheme.typography.titleLarge) }
//                        items(reviews) { review ->
//                            // Ensure review.trackDetails is not null
//                            ReviewCard(
//                                reviewItem = FeedItem.ReviewItem(
//                                    review = review,
//                                    track =
//                                ),
//                                onClick = {
//                                    navController.navigate(
//                                        Screen.Profile.createRoute(review.username)
//                                    )
//                                },
//                            )
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//
//
//
//
//
//@Composable
//private fun StatItem(label: String, value: String) {
//    Column(horizontalAlignment = Alignment.CenterHorizontally) {
//        Text(
//            text = value,
//            style = MaterialTheme.typography.titleMedium)
//        Text(
//            text = label,
//            style = MaterialTheme.typography.bodySmall,
//            color = MaterialTheme.colorScheme.onSurfaceVariant)
//    }
//}
//
