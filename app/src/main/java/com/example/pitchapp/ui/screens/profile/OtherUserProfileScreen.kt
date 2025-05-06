package com.example.pitchapp.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.pitchapp.viewmodel.ProfileViewModel
import com.example.pitchapp.ui.screens.search.SpinningRecord
import java.util.*
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue


@Composable
fun OtherUserProfileScreen(
    navController: NavController,
    profileUserId: String,
    viewModel: ProfileViewModel,
    currentUserId: String
) {
    val profileState by viewModel.profileState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(profileUserId) {
        viewModel.loadProfile(profileUserId)
    }

    when (val state = profileState) {
        is ProfileViewModel.ProfileState.Loading -> {
            SpinningRecord()
        }
        is ProfileViewModel.ProfileState.Error -> {
            Text("Error loading user: ${state.message}")
        }
        is ProfileViewModel.ProfileState.Success -> {
            val profile = state.profile
            val isFollowing = profile.followers.contains(currentUserId)
            Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                Text(profile.username, style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Bio: ${profile.bio ?: "No bio set."}")
                Text("Joined in: ${formatTimestamp(profile.createdAt)}")
                Text("Followers: ${profile.followers.size}")
                Text("Following: ${profile.following.size}")
                Text("Songs Rated: ${profile.reviewCount}")
                Text("Average Rating: ${"%.1f".format(profile.averageRating)}")

                Button(
                    onClick = {
                        coroutineScope.launch {
                            if (isFollowing) {
                                viewModel.unfollowUser(
                                    currentUserId,
                                    profileUserId,
                                    onSuccess = { viewModel.refreshProfile(profileUserId) },
                                    onFailure = { /* Handle UI error */ }
                                )
                            } else {
                                viewModel.followUser(
                                    currentUserId,
                                    profileUserId,
                                    onSuccess = { viewModel.refreshProfile(profileUserId) },
                                    onFailure = { /* Handle UI error */ }
                                )
                            }
                        }
                    }
                ) {
                    Text(if (isFollowing) "Unfollow" else "Follow")
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { navController.popBackStack() }) {
                    Text("Back")
                }
            }
        }
    }
}