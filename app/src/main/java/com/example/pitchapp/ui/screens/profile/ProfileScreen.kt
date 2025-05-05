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
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import com.example.pitchapp.data.model.Profile
import java.util.*



@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel,
    profileUserId: String,
    authViewModel: AuthViewModel
) {
    val profileState by viewModel.profileState.collectAsState()
    val userId by authViewModel.userId.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var isEditing by remember { mutableStateOf(false) }
    var editedBio by remember { mutableStateOf("") }
    var editedTheme by remember { mutableStateOf("light") }

    if (userId == null) {
        // Not logged in
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text("Welcome to PitchApp!")

            Button(onClick = { navController.navigate("login") }) {
                Text("Login")
            }

            Spacer(modifier = Modifier.height(8.dp))

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

            is ProfileViewModel.ProfileState.Error -> {
                Text("Error: ${state.message}")
            }

            is ProfileViewModel.ProfileState.Success -> {
                val profile = state.profile
                val isOwnProfile = profileUserId == userId
                val isFollowing = profile.followers.contains(userId)

                // Follow/Unfollow Button
                if (!isOwnProfile) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp)
                    ) {
                        Text(profile.displayName, style = MaterialTheme.typography.headlineMedium)
                        Text("Bio: ${profile.bio ?: "No bio set."}")
                        Text("Followers: ${profile.followers.size}")
                        Text("Following: ${profile.following.size}")
                        Text("Joined in: ${formatTimestamp(profile.createdAt)}")
                        Text("Songs Rated: ${profile.reviewCount}")
                        Text("Average Rating: ${"%.1f".format(profile.averageRating)}")
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    if (isFollowing) {
                                        viewModel.unfollowUser(
                                            userId!!,
                                            profileUserId,
                                            onSuccess = { viewModel.refreshProfile(profileUserId) },
                                            onFailure = { /* show toast or error */ }
                                        )
                                    } else {
                                        viewModel.followUser(
                                            userId!!,
                                            profileUserId,
                                            onSuccess = { viewModel.refreshProfile(profileUserId) },
                                            onFailure = { /* show toast or error */ }
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (isFollowing) "Unfollow" else "Follow")
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    if (isOwnProfile) {
                        LaunchedEffect(profile.themePreference) {
                            editedTheme = profile.themePreference
                        }
                        Column(
                            modifier = Modifier.fillMaxSize().padding(24.dp)
                        ) {
                            Text(
                                profile.displayName,
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Text("Bio: ${profile.bio ?: "No bio set."}")
                            Text("Followers: ${profile.followers.size}")
                            Text("Following: ${profile.following.size}")
                            Text("Joined in: ${formatTimestamp(profile.createdAt)}")
                            Text("Songs Rated: ${profile.reviewCount}")
                            Text("Average Rating: ${"%.1f".format(profile.averageRating)}")
                            Text("Theme: ${profile.themePreference.capitalize()}")
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Dark Mode")
                                Spacer(modifier = Modifier.width(8.dp))
                                Switch(
                                    checked = editedTheme == "dark",
                                    onCheckedChange = {
                                        editedTheme = if (it) "dark" else "light"
                                        coroutineScope.launch {
                                            viewModel.updateProfile(
                                                userId = userId!!,
                                                newBio = profile.bio ?: "",
                                                newTheme = editedTheme
                                            )
                                            viewModel.refreshProfile(userId!!)
                                        }
                                    }
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))

                            if (isEditing) {
                                OutlinedTextField(
                                    value = editedBio,
                                    onValueChange = {
                                        if (it.length <= 150) editedBio = it
                                    },
                                    label = { Text("Edit Bio") },
                                    maxLines = 3,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text("${editedBio.length}/150")

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Dark Mode")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Switch(
                                        checked = editedTheme == "dark",
                                        onCheckedChange = {
                                            editedTheme = if (it) "dark" else "light"
                                        }
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            viewModel.updateProfile(
                                                userId = userId!!,
                                                newBio = editedBio,
                                                newTheme = editedTheme
                                            )
                                            isEditing = false
                                            viewModel.refreshProfile(userId!!)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Save")
                                }
                            } else {
                                Button(
                                    onClick = {
                                        isEditing = true
                                        editedBio = profile.bio ?: ""
                                        editedTheme = profile.themePreference
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Edit Profile")
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { authViewModel.logout() }) {
                                Text("Logout")
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        LazyColumn {
                            items(profile.reviews) { review ->
                                FeedItem.ReviewItem(review,review.albumDetails!!)
                            }
                        }
                    }

                }
            }
        }
    }
}


fun formatTimestamp(timestamp: com.google.firebase.Timestamp): String {
    val date = timestamp.toDate()
    val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    return sdf.format(date)
}

