package com.example.pitchapp.ui.screens.profile

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import com.example.pitchapp.data.model.Profile
import com.example.pitchapp.data.model.Review
import com.example.pitchapp.ui.components.ErrorMessage
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel,
    userId: String,
    authViewModel: AuthViewModel,
    onThemeChanged: () -> Unit
) {
    val profileState by viewModel.profileState.collectAsState()
    var showPreferencesDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                actions = {
                    IconButton(onClick = { showPreferencesDialog = true }) {
                        Icon(Icons.Default.Settings, "Preferences")
                    }
                }
            )
        }
    ) { padding ->
        when (val state = profileState) {
            is ProfileViewModel.ProfileState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    SpinningRecord()
                }
            }

            is ProfileViewModel.ProfileState.Error -> {
                ErrorMessage(message = state.message)
            }

            is ProfileViewModel.ProfileState.Success -> {
                ProfileContent(
                    profile = state.profile,
                    userId = userId,
                    padding = padding,
                    authViewModel = authViewModel,
                    viewModel = viewModel,
                    onThemeChanged = onThemeChanged
                )
            }
        }

        // Move preferences dialog here with proper state access
        if (showPreferencesDialog) {
            when (val currentState = profileState) {
                is ProfileViewModel.ProfileState.Success -> {
                    PreferencesDialog(
                        currentTheme = currentState.profile.themePreference,
                        isPublic = currentState.profile.isPublic,
                        onDismiss = { showPreferencesDialog = false },
                        onThemeChanged = { newTheme: String ->
                            userId?.let {
                                viewModel.updateThemePreference(it, newTheme)
                                authViewModel.setThemePreference(newTheme)
                                onThemeChanged()
                            }
                        },
                        onPrivacyChanged = { newPrivacy: Boolean ->
                            userId?.let {
                                viewModel.updatePrivacyPreference(it, newPrivacy)
                            }
                        }
                    )
                }
                else -> {
                    // Handle other states if needed
                }
            }
        }
    }
}

@Composable
private fun ProfileContent(
    profile: Profile,
    userId: String?,
    padding: PaddingValues,
    authViewModel: AuthViewModel,
    viewModel: ProfileViewModel,
    onThemeChanged: () -> Unit
) {
    var isEditingBio by remember { mutableStateOf(false) }
    var editedBio by remember { mutableStateOf(profile.bio) }

    Column(
        modifier = Modifier
            .padding(padding)
            .verticalScroll(rememberScrollState())
    ) {
        // Profile Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Initials Avatar
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                Text(
                    text = profile.displayName.take(2).uppercase(),
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = profile.displayName,
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                text = "Member since ${formatTimestamp(profile.createdAt)}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Stats Cards
        StatsRow(
            reviewCount = profile.reviewCount,
            averageRating = profile.averageRating,
            followers = profile.followers.size,
            following = profile.following.size,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Bio Section
        editedBio?.let { it ->
            BioSection(
                bio = profile.bio,
                isEditing = isEditingBio,
                editedBio = it,
                onBioChanged = { editedBio = it },
                onSave = {
                    viewModel.updateBio(userId!!, editedBio!!)
                    isEditingBio = false
                },
                onEditToggle = { isEditingBio = !isEditingBio }
            )
        }

        // Recent Reviews
        RecentReviewsSection(
            reviews = profile.recentReviews,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@Composable
private fun StatsRow(
    reviewCount: Int,
    averageRating: Float,
    followers: Int,
    following: Int,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(value = reviewCount.toString(), label = "Reviews")
            StatItem(value = "%.1f".format(averageRating), label = "Avg Rating")
            StatItem(value = followers.toString(), label = "Followers")
            StatItem(value = following.toString(), label = "Following")
        }
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BioSection(
    bio: String?,
    isEditing: Boolean,
    editedBio: String,
    onBioChanged: (String) -> Unit,
    onSave: () -> Unit,
    onEditToggle: () -> Unit
) {
    Card(modifier = Modifier.padding(horizontal = 16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Bio",
                    style = MaterialTheme.typography.titleLarge
                )
                IconButton(onClick = onEditToggle) {
                    Icon(
                        imageVector = if (isEditing) Icons.Default.Close else Icons.Default.Edit,
                        contentDescription = if (isEditing) "Cancel" else "Edit"
                    )
                }
            }

            if (isEditing) {
                OutlinedTextField(
                    value = editedBio,
                    onValueChange = { if (it.length <= 150) onBioChanged(it) },
                    label = { Text("Edit bio") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onSave,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Save Changes")
                }
            } else {
                Text(
                    text = bio ?: "No bio yet",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun RecentReviewsSection(reviews: List<Review>, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(
            text = "Recent Reviews",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(reviews) { review ->
                ReviewCard(review = review)
            }
        }
    }
}

@Composable
private fun ReviewCard(review: Review) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .padding(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = review.albumDetails?.title ?: "Unknown Album",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            RatingBar(rating = review.rating)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = review.content,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun RatingBar(rating: Float) {
    Row {
        repeat(5) { index ->
            Icon(
                imageVector = if (rating >= index + 1) Icons.Filled.Star
                else if (rating > index) Icons.AutoMirrored.Filled.StarHalf
                else Icons.Filled.StarOutline,
                contentDescription = "Rating",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun PreferencesDialog(
    currentTheme: String,
    isPublic: Boolean,
    onDismiss: () -> Unit,
    onThemeChanged: (String) -> Unit,
    onPrivacyChanged: (Boolean) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Account Preferences") },
        text = {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                // Theme Preference
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Dark Theme", modifier = Modifier.weight(1f))
                    Switch(
                        checked = currentTheme == "dark",
                        onCheckedChange = { checked ->
                            onThemeChanged(if (checked) "dark" else "light")
                        }
                    )
                }

                // Privacy Setting
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Public Profile", modifier = Modifier.weight(1f))
                    Switch(
                        checked = isPublic,
                        onCheckedChange = { onPrivacyChanged(it) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

fun formatTimestamp(timestamp: com.google.firebase.Timestamp): String {
    val date = timestamp.toDate()
    val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    return sdf.format(date)
}