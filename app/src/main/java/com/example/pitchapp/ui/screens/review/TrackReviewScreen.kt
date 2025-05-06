package com.example.pitchapp.ui.screens.review

import android.util.Log
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
import com.example.pitchapp.data.repository.MusicRepository
import com.example.pitchapp.viewmodel.AuthViewModel
import com.example.pitchapp.viewmodel.ReviewViewModel
import com.example.pitchapp.viewmodel.SearchViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun AddTrackReviewScreen(
    navController: NavController,
    reviewViewModel: ReviewViewModel,
    authViewModel: AuthViewModel,
    trackId: String?
) {
    val selectedTrack by reviewViewModel.selectedTrack.collectAsState()
    val reviewText by reviewViewModel.reviewText.collectAsState()
    val rating by reviewViewModel.rating.collectAsState()
    val submissionResult by reviewViewModel.submissionResult.collectAsState()
    val userId by authViewModel.userId.collectAsState()
    Log.d("add track review","in add track review")


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
                onValueChange = { reviewViewModel.updateTrackText(it) },
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
                onRatingChange = { reviewViewModel.updateTrackRating(it.toFloat()) }
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    reviewViewModel.submitTrackReview {
                        Log.d("Track Id in Review","track id $trackId")
                        if (trackId != null) {
                            navController.navigate(Screen.AddTrackReview.createRoute(trackId))
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
                submissionResult?.let {
                    when (it) {
                        is Result.Error -> Text(
                            it.exception.message ?: "Unknown error",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        else -> {
                            reviewViewModel.clearReviewState()
                        }
                    }
                }

            }


        }
    }
}
