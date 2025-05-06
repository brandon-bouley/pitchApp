package com.example.pitchapp.ui.screens.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pitchapp.data.model.UserSummary

@Composable
fun UserList(
    title: String,
    users: List<UserSummary>,
    onUserClick: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text(text = title, style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn {
            items(users) { user ->
                Text(
                    text = user.username,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onUserClick(user.uid) }
                        .padding(8.dp)
                )
                Divider()
            }
        }
    }
}
