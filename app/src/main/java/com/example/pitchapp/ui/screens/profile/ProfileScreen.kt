package com.example.pitchapp.ui.screens.profile

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.pitchapp.data.model.Album
import com.example.pitchapp.data.model.FeedItem
import com.example.pitchapp.data.model.Profile
import com.example.pitchapp.data.repository.ReviewRepository
import com.example.pitchapp.ui.navigation.Screen
import com.example.pitchapp.ui.screens.search.SpinningRecord
import com.example.pitchapp.viewmodel.ProfileViewModel
import com.example.pitchapp.viewmodel.AuthViewModel
import com.example.pitchapp.ui.components.ReviewCard


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel,
    reviewRepository: ReviewRepository,
    authViewModel: AuthViewModel
) {
    val TAG = "ProfileScreen"
    val profileState by viewModel.profileState.collectAsState()
    val currentUsername by authViewModel.username.collectAsState()
    val currentUserId by authViewModel.userId.collectAsState()
    val currentBio by authViewModel.bio.collectAsState()
    val isAuthLoading by authViewModel.isLoading.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Debug logging
    LaunchedEffect(profileState) {
        when (profileState) {
            is ProfileViewModel.ProfileState.Loading ->
                Log.d(TAG, "Profile state: Loading")
            is ProfileViewModel.ProfileState.Success ->
                Log.d(TAG, "Profile state: Success with username ${(profileState as ProfileViewModel.ProfileState.Success).profile.username}")
            is ProfileViewModel.ProfileState.Error ->
                Log.e(TAG, "Profile state: Error - ${(profileState as ProfileViewModel.ProfileState.Error).message}")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    // Only show back button if we're not on our own profile
                    if (profileState is ProfileViewModel.ProfileState.Success &&
                        (profileState as ProfileViewModel.ProfileState.Success).profile.username != currentUsername) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    // Only show edit profile and logout options on your own profile
                    if (profileState is ProfileViewModel.ProfileState.Success &&
                        (profileState as ProfileViewModel.ProfileState.Success).profile.username == currentUsername) {

                        IconButton(onClick = { showEditDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Profile")
                        }

                        IconButton(onClick = { showLogoutDialog = true }) {
                            Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                        }
                    } else {
                        IconButton(onClick = { navController.navigate(Screen.UserSearch.route) }) {
                            Icon(Icons.Default.Search, contentDescription = "Find Users")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = profileState) {
                is ProfileViewModel.ProfileState.Loading -> {
                    SpinningRecord()
                }

                is ProfileViewModel.ProfileState.Success -> {
                    val profile = state.profile.copy(
                        recentReviews = state.profile.recentReviews
                            .sortedByDescending { it.timestamp }
                            .take(10)
                    )
                    val isCurrentUser = profile.username == currentUsername

                    ProfileContent(
                        profile = profile,
                        isCurrentUser = isCurrentUser,
                        currentUserId = currentUserId,
                        navController = navController,
                        onToggleFollow = { targetUserId ->
                            currentUserId?.let { userId ->
                                viewModel.toggleFollow(userId, targetUserId)
                            }
                        }
                    )
                }

                is ProfileViewModel.ProfileState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "Error: ${state.message}",
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(onClick = { navController.popBackStack() }) {
                            Text("Go Back")
                        }
                    }
                }
            }
        }
    }

    // Edit Profile Dialog
    if (showEditDialog) {
        ProfileEditDialog(
            currentBio = currentBio,
            isLoading = isAuthLoading,
            onDismiss = { showEditDialog = false },
            onSave = { newBio ->
                authViewModel.updateBio(
                    newBio = newBio,
                    onSuccess = {
                        showEditDialog = false
                        // Reload profile to reflect changes
                        currentUsername?.let { viewModel.loadProfile(it) }
                    },
                    onFailure = { error ->
                        // You could show a snackbar or toast here
                        Log.e(TAG, "Failed to update bio: $error")
                    }
                )
            }
        )
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                Button(
                    onClick = {
                        authViewModel.logout()
                        showLogoutDialog = false
                    }
                ) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ProfileContent(
    profile: Profile,
    isCurrentUser: Boolean,
    currentUserId: String?,
    navController: NavController,
    onToggleFollow: (String) -> Unit
) {

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            ProfileHeader(
                profile = profile,
                isCurrentUser = isCurrentUser,
                currentUserId = currentUserId,
                onToggleFollow = onToggleFollow
            )
        }

        if (profile.recentReviews.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No reviews yet")
                }
            }
        } else {
            items(
                items = profile.recentReviews,
                key = { it.id }
            ) { review ->
                val album = review.albumDetails ?: Album(
                    id = review.albumId,
                    title = "Unknown Album",
                    artist = "Unknown Artist",
                    artworkUrl = ""
                )
                ReviewCard(
                    reviewItem = FeedItem.ReviewItem(review, album),
                    onClick = {
                        navController.navigate(Screen.AlbumDetail.createRoute(album.id))
                    },
                )
            }
        }
    }
}

@Composable
fun ProfileHeader(
    profile: Profile,
    isCurrentUser: Boolean,
    currentUserId: String?,
    onToggleFollow: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile picture placeholder
            Card(
                modifier = Modifier.size(80.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Username and bio
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    profile.username,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                profile.bio?.let {
                    if (it.isNotEmpty()) {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Followers and following
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ProfileStat(value = profile.reviewCount, label = "Reviews")
            ProfileStat(value = profile.followers.size, label = "Followers")
            ProfileStat(value = profile.following.size, label = "Following")
        }

        // Follow/Unfollow button for other users
        if (!isCurrentUser && currentUserId != null) {
            val isFollowing = profile.followers.contains(currentUserId)
            Button(
                onClick = { onToggleFollow(profile.userId) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(if (isFollowing) "Unfollow" else "Follow")
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
    }
}

@Composable
fun ProfileStat(value: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "$value",
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
@Composable
fun ProfileEditDialog(
    currentBio: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    isLoading: Boolean = false
) {
    var bioText by remember { mutableStateOf(currentBio) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Edit Profile",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = bioText,
                    onValueChange = { bioText = it },
                    label = { Text("Bio") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    maxLines = 3,
                    placeholder = { Text("Tell us about yourself...") }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { onSave(bioText) },
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}
