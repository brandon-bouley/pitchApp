package com.example.pitchapp.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pitchapp.data.model.Album
import com.example.pitchapp.data.model.FeedItem

@Composable
fun AlbumCard(albumItem: FeedItem.AlbumItem, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ){
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(albumItem.album.name, style = MaterialTheme.typography.headlineSmall)
            Text("★ ${"%.1f".format(albumItem.averageRating)}")
            Text("Released: ${albumItem.album.formattedReleaseDate}")
            Text("By ${albumItem.album.artists.joinToString { it.name }}")
        }
    }
}
}

private fun Float.roundToDecimals(decimals: Int): String {
    return "%.${decimals}f".format(this)
}