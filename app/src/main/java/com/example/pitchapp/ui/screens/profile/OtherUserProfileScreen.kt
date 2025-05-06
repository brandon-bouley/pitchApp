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

//Used to display other users profile information
//current user profile shows more data than what we would want others to see
//created this file to keep things seperate
//Also created follow button for these users, which should add the selected user to the current user's
//following list
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

    //load the profile

    when (val state = profileState) {
        is ProfileViewModel.ProfileState.Loading -> {
            SpinningRecord()
        }
        is ProfileViewModel.ProfileState.Error -> {
            Text("Error loading user: ${state.message}")
        }
        //if success we want to show that users data
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
//add follow button functionality
                Button(
                    onClick = {
                        coroutineScope.launch {
                            if (isFollowing) {
                                viewModel.unfollowUser(
                                    currentUserId,
                                    profileUserId,
                                    onSuccess = { viewModel.refreshProfile(profileUserId) },
                                    onFailure = { }
                                )
                            } else {
                                viewModel.followUser(
                                    currentUserId,
                                    profileUserId,
                                    onSuccess = { viewModel.refreshProfile(profileUserId) },
                                    onFailure = {  }
                                )
                            }
                        }
                    }
                ) {
                    Text(if (isFollowing) "Unfollow" else "Follow")
                    //add unfollow option
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { navController.popBackStack() }) {
                    //use popBackStack() to go back
                    Text("Back")
                }
            }
        }
    }
}