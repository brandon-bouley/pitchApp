package com.example.pitchapp.ui.components

import android.widget.RatingBar
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pitchapp.data.model.FeedItem

@Composable
fun ReviewCard(reviewItem: FeedItem.ReviewItem, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(reviewItem.review.author, style = MaterialTheme.typography.labelSmall)
            Text(reviewItem.album?.name ?: "", style = MaterialTheme.typography.titleMedium)
            RatingBar(rating = reviewItem.review.rating)
            Text(reviewItem.review.content, style = MaterialTheme.typography.bodyMedium)
        }
    }
}