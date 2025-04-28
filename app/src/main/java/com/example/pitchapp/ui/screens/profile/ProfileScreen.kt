package com.example.pitchapp.ui.screens.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import com.example.pitchapp.data.model.FeedItem
import com.example.pitchapp.ui.screens.search.SpinningRecord
import com.example.pitchapp.viewmodel.ProfileViewModel
import com.example.pitchapp.viewmodel.AuthViewModel

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel,
    userId: String,
    authViewModel: AuthViewModel
) {
    val profileState by viewModel.profileState.collectAsState()
    val userId by authViewModel.userId.collectAsState()

    if (userId == null) {
        // Not logged in
        Column {
            Text("Welcome to PitchApp!")

            Button(onClick = { navController.navigate("login") }) {
                Text("Login")
            }

            Button(onClick = { navController.navigate("signup") }) {
                Text("Create Account")
            }
        }
    } else {
        // User is logged in, load profile
        LaunchedEffect(userId) {
            viewModel.loadProfile(userId!!)
        }

        when (val state = profileState) {
            is ProfileViewModel.ProfileState.Loading -> {
                SpinningRecord()
            }
            is ProfileViewModel.ProfileState.Success -> {
                Column {
                    Text(state.profile.displayName)
                    Text("${state.profile.reviewCount} reviews")
                    Text("Average rating: ${state.profile.averageRating}")

                    LazyColumn {
                        items(state.profile.reviews) { review ->
                            FeedItem.ReviewItem(review)
                        }
                    }

                    Button(onClick = { viewModel.refreshProfile(userId!!) }) {
                        Text("Refresh Reviews")
                    }

                    Button(onClick = { authViewModel.logout() }) {
                        Text("Logout")
                    }
                }
            }
            is ProfileViewModel.ProfileState.Error -> {
                Text("Error: ${state.message}")
            }
        }
    }
}