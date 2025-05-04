package com.example.pitchapp.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pitchapp.data.model.FeedItem
import com.example.pitchapp.data.model.RandomTrack

@Composable
fun RandomTrackCard(trackItem: FeedItem.TrackItem) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .clickable {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(trackItem.track.url))
                context.startActivity(intent)
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🎵 ${trackItem.track.name}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "👤 Artist: ${trackItem.track.artist.name}",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "🔊 Playcount: ${trackItem.track.playcount}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "📥 Listeners: ${trackItem.track.listeners}",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "🌐 Tap to open track URL",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Blue
            )
        }
    }
}
