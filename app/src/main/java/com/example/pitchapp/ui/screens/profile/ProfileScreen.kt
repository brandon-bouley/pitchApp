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
    authViewModel: AuthViewModel,
    onThemeChanged: () -> Unit
) {
    val profileState by viewModel.profileState.collectAsState()
    val userId by authViewModel.userId.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var isEditing by remember { mutableStateOf(false) }
    var editedBio by remember { mutableStateOf("") }
    var editedTheme by remember { mutableStateOf("light") }

    if (userId == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome to PitchApp!",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = { navController.navigate("login") }) {
                Text("Login")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(onClick = { navController.navigate("signup") }) {
                Text("Create Account")
            }
        }
    } else {
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

                LaunchedEffect(profile.themePreference) {
                    editedTheme = profile.themePreference
                }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Text(
                                profile.displayName,
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Text("Joined in: ${formatTimestamp(profile.createdAt)}")
                            Text("Songs Rated: ${profile.reviewCount}")
                            Text("Average Rating: ${"%.1f".format(profile.averageRating)}")
                            Text("Theme: ${profile.themePreference.capitalize()}")

                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        if (isEditing) {
                            OutlinedTextField(
                                value = editedBio,
                                onValueChange = { if (it.length <= 150) editedBio = it },
                                label = { Text("Edit Bio") },
                                maxLines = 3,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text("${editedBio.length}/150", style = MaterialTheme.typography.labelSmall)

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Dark Mode")
                                Switch(
                                    checked = editedTheme == "dark",
                                    onCheckedChange = { editedTheme = if (it) "dark" else "light" }
                                )
                            }

                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        viewModel.updateProfile(userId!!, editedBio, editedTheme)
                                        authViewModel.setThemePreference(editedTheme)
                                        onThemeChanged()
                                        isEditing = false
                                        viewModel.refreshProfile(userId!!)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Save")
                            }
                        } else {
                            profile.bio?.let {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("Bio", style = MaterialTheme.typography.titleMedium)
                                        Text(it, style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
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

fun formatTimestamp(timestamp: com.google.firebase.Timestamp): String {
    val date = timestamp.toDate()
    val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    return sdf.format(date)
}

