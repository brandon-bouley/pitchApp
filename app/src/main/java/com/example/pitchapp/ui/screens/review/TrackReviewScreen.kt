package com.example.pitchapp.ui.screens.review

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.pitchapp.ui.navigation.Screen
import com.example.pitchapp.viewmodel.TrackReviewViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.example.pitchapp.data.model.Result

@Composable
fun AddTrackReviewScreen(
    navController: NavController,
    trackReviewViewModel: TrackReviewViewModel
) {
    val selectedTrack by trackReviewViewModel.selectedTrack.collectAsState()
    val reviewText by trackReviewViewModel.reviewText.collectAsState()
    val rating by trackReviewViewModel.rating.collectAsState()
    val submissionResult by trackReviewViewModel.submissionResult.collectAsState()

    Scaffold { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            selectedTrack?.let { track ->
                Text("Reviewing: ${track.name} by ${track.artist.name}", style = MaterialTheme.typography.titleMedium)
            } ?: Text("No track selected", style = MaterialTheme.typography.titleMedium)

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = reviewText,
                onValueChange = { trackReviewViewModel.updateReviewText(it) },
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

            Spacer(Modifier.height(16.dp))

            Text("Rating:", style = MaterialTheme.typography.titleMedium)
            StarRating(
                rating = rating,
                onRatingChange = { trackReviewViewModel.updateRating(it.toFloat()) }
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    val userId = Firebase.auth.currentUser?.uid ?: return@Button
                    val username = Firebase.auth.currentUser?.displayName ?: "Anonymous"
                    trackReviewViewModel.submitReview(userId, username) {
                        val trackId = selectedTrack?.mbid
                        if (trackId != null) {
                            navController.navigate(Screen.AlbumDetail.createRoute(trackId))
                            navController.navigate(Screen.Feed.route) {
                                popUpTo(Screen.Feed.route) { inclusive = true }
                            }
                        }
                    }

                    },
                enabled = reviewText.isNotBlank() && rating > 0f,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Submit Review")
            }

            submissionResult?.let {
                when (it) {
                    is Result.Error -> Text(
                        it.exception.message ?: "Unknown error",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    else -> {
                        trackReviewViewModel.clearReviewState()
                    }
                }
            }
        }
    }
}
