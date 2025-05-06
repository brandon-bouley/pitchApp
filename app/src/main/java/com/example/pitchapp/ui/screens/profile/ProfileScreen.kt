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
import com.example.pitchapp.data.model.UserSummary
import java.util.*

//Displays user profile information
// fetches state data using collectAsState()
//This file allows users to set their bio, light/dark mode prefs, view followers/following, and
//their average rating

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel,
    profileUserId: String,
    authViewModel: AuthViewModel,
    onThemeChanged: () -> Unit
) {
    val profileState by viewModel.profileState.collectAsState()
    val userId by authViewModel.userId.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var isEditing by remember { mutableStateOf(false) }
    var editedBio by remember { mutableStateOf("") }
    var editedTheme by remember { mutableStateOf("light") }
    var followers by remember { mutableStateOf<List<UserSummary>>(emptyList()) }
    var following by remember { mutableStateOf<List<UserSummary>>(emptyList()) }

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
//if not logged in prompt user to log in/create account
            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = { navController.navigate("signup") }) {
                Text("Create Account")
            }
        }
    } else {
        LaunchedEffect(userId) {
            userId?.let { uid ->
                viewModel.loadProfile(uid)
            }
        }
        //else load profile

        when (val state = profileState) {
            is ProfileViewModel.ProfileState.Loading -> {
                SpinningRecord()
            }

            is ProfileViewModel.ProfileState.Error -> {
                Text("Error: ${state.message}")
            }

            is ProfileViewModel.ProfileState.Success -> {
                val profile = state.profile
                //if eeverything loaded correctly, set the theme/bio/followers to the saved state
                //or default if not created
                LaunchedEffect(profile.themePreference) {
                    editedTheme = profile.themePreference
                }
                LaunchedEffect(profile) {
                    viewModel.fetchUserSummaries(profile.followers) {
                        followers = it
                    }
                    viewModel.fetchUserSummaries(profile.following) {
                        following = it
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    Text(profile.username, style = MaterialTheme.typography.headlineMedium)
                    Text("Joined in: ${formatTimestamp(profile.createdAt)}")
                    UserList(title = "Followers", users = followers) { uid ->
                        navController.navigate("profile/$uid")
                    }
                    //use simple time format function to display just month and year

                    UserList(title = "Following", users = following) { uid ->
                        navController.navigate("profile/$uid")
                    }
                    Text("Songs Rated: ${profile.reviewCount}")
                    Text("Average Rating: ${"%.1f".format(profile.averageRating)}")
                    Text("Theme: ${profile.themePreference.capitalize()}")
                    Spacer(modifier = Modifier.height(16.dp))
                    //this handles the bio part
                    if (isEditing) {
                        OutlinedTextField(
                            value = editedBio,
                            onValueChange = {
                                if (it.length <= 150) editedBio = it //set char limit to 150
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
                                checked = editedTheme == "dark",    //logic handling the toggle
                                onCheckedChange = {
                                    editedTheme = if (it) "dark" else "light"
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        //this is the save changes button logic
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    viewModel.updateProfile(
                                        userId = userId!!,
                                        newBio = editedBio,
                                        newTheme = editedTheme
                                    )
                                    authViewModel.setThemePreference(editedTheme)
                                    onThemeChanged()
                                    isEditing = false
                                    //if changes are made, save the changes and refresh the profile
                                    //to display latest changes
                                    viewModel.refreshProfile(userId!!)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save")
                        }
                    } else {
                        //default values
                        Text("Bio: ${profile.bio ?: "No bio set."}")
                        Spacer(modifier = Modifier.height(8.dp))
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

                    Spacer(modifier = Modifier.height(24.dp))

                    LazyColumn {
                        items(profile.reviews) { review ->
                            FeedItem.ReviewItem(review)
                        }
                    }
                }
            }
        }
    }
}
//simple helper function using Firebase timestamp info
fun formatTimestamp(timestamp: com.google.firebase.Timestamp): String {
    val date = timestamp.toDate()
    val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    return sdf.format(date)
}

