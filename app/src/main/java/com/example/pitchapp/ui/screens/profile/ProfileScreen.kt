package com.example.pitchapp.ui.screens.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import com.example.pitchapp.data.model.FeedItem
import com.example.pitchapp.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel,
    userId: String
) {
    val profileState by viewModel.profileState.collectAsState()

    LaunchedEffect(userId) {
        viewModel.loadProfile(userId)
    }

    when (val state = profileState) {
        is ProfileViewModel.ProfileState.Loading -> {
            CircularProgressIndicator()
        }
        is ProfileViewModel.ProfileState.Success -> {
            Column {
                // Profile header
                Text(state.profile.displayName)
                Text("${state.profile.reviewCount} reviews")
                Text("Average rating: ${state.profile.averageRating}")

                // Reviews list
                LazyColumn {
                    items(state.profile.reviews) { review ->
                        FeedItem.ReviewItem(review)
                    }
                }

                Button(onClick = { viewModel.refreshProfile(userId) }) {
                    Text("Refresh Reviews")
                }
            }
        }
        is ProfileViewModel.ProfileState.Error -> {
            Text("Error: ${state.message}")
        }
    }
}

