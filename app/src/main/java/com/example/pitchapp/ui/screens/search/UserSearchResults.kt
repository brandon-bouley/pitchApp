package com.example.pitchapp.ui.screens.search
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.clickable
import com.example.pitchapp.data.model.UserSummary
import androidx.compose.material3.ListItem
import androidx.compose.foundation.lazy.items



@Composable
fun UserSearchResults(
    users: List<UserSummary>,
    onUserClick: (String) -> Unit
) {
    LazyColumn {
        items(users) { user ->
            ListItem(
                headlineContent = { Text(user.displayName) },
                modifier = Modifier
                    .clickable { onUserClick(user.uid) }
                    .fillMaxWidth()
                    .padding(8.dp)
            )
        }
    }
}

