package com.example.pitchapp.ui.screens.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.pitchapp.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel,
    username: String
) {
    val user    by viewModel.user.collectAsState()
    val reviews by viewModel.reviews.collectAsState()

    // load on first composition or when username changes
    LaunchedEffect(username) {
        viewModel.loadProfile(username)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Profile: $username") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            if (user == null) {
                Text("Loading user…", style = MaterialTheme.typography.bodyMedium)
            } else {
                Text("Name: ${user!!.name}", style = MaterialTheme.typography.titleLarge)
                Text("Email: ${user!!.email}", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))
                Text("Bio:", style = MaterialTheme.typography.titleMedium)
                Text(user!!.bio, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(16.dp))

                Text("Reviews by $username:", style = MaterialTheme.typography.titleMedium)
                if (reviews.isEmpty()) {
                    Text("No reviews yet.", style = MaterialTheme.typography.bodyMedium)
                } else {
                    LazyColumn {
                        items(reviews) { review ->
                            Card(modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                            ) {
                                Column(Modifier.padding(12.dp)) {
                                    Text(review.albumTitle, style = MaterialTheme.typography.titleMedium)
                                    Text("${review.rating}/5", style = MaterialTheme.typography.bodyMedium)
                                    Text(review.content, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
