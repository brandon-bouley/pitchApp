package com.example.pitchapp.ui.screens.profile

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.pitchapp.data.model.FeedItem
import com.example.pitchapp.data.model.Profile
import com.example.pitchapp.ui.navigation.Screen
import com.example.pitchapp.ui.screens.search.SpinningRecord
import com.example.pitchapp.viewmodel.ProfileViewModel
import com.example.pitchapp.viewmodel.AuthViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel,
    authViewModel: AuthViewModel
) {
    val TAG = "ProfileScreen"
    val profileState by viewModel.profileState.collectAsState()
    val currentUsername by authViewModel.username.collectAsState()
    val currentUserId by authViewModel.userId.collectAsState()
    val userId by authViewModel.userId.collectAsState()
    val coroutineScope = rememberCoroutineScope()


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
                    IconButton(onClick = { navController.navigate(Screen.UserSearch.route) }) {
                        Icon(Icons.Default.Search, contentDescription = "Find Users")
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
                    val profile = state.profile
                    val isCurrentUser = profile.username == currentUsername

                    ProfileContent(
                        profile = profile,
                        isCurrentUser = isCurrentUser,
                        currentUserId = currentUserId,
                        onToggleFollow = { targetUserId ->
                            currentUserId?.let { userId ->
                                viewModel.toggleFollow(userId, targetUserId)
                            }
                        },
                        authViewModel
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
}

@Composable
fun ProfileContent(
    profile: Profile,
    isCurrentUser: Boolean,
    currentUserId: String?,
    onToggleFollow: (String) -> Unit,
    authViewModel: AuthViewModel

) {


    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {

        item {
            ProfileHeader(
                profile = profile,
                isCurrentUser = isCurrentUser,
                currentUserId = currentUserId,
                onToggleFollow = onToggleFollow,
                authViewModel = authViewModel
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
            items(profile.recentReviews) { review ->
                FeedItem.ReviewItem(review = review, album = review.albumDetails)
            }
        }




    }



    }


@Composable
fun ProfileHeader(
    profile: Profile,
    isCurrentUser: Boolean,
    currentUserId: String?,
    onToggleFollow: (String) -> Unit,
    authViewModel: AuthViewModel
) {
    var isEditing by remember { mutableStateOf(false) }
    var editedBio by remember { mutableStateOf("") }
    var editedTheme by remember { mutableStateOf("light") }
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

        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { authViewModel.logout() }) {
            Text("Logout")
        }
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