package com.example.pitchapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.pitchapp.data.model.FeedItem
import kotlin.math.floor
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.example.pitchapp.R
import com.example.pitchapp.data.model.Review
import com.example.pitchapp.ui.screens.review.StarRating

@Composable
fun ReviewCard(
    reviewItem: FeedItem.ReviewItem,
    onClick: ()->Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        onClick = onClick,
    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.Top
        ) {
        Box(
            modifier = Modifier
                .size(80.dp)
            .clip(MaterialTheme.shapes.small)
        ) {
        reviewItem.album?.artworkUrl?.takeIf { it.isNotEmpty() }?.let { url ->
            AsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } ?: run {
            Image(
                painter = painterResource(id = R.drawable.record_icon), // Replace with your app logo
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
        }
    }

        Spacer(Modifier.width(16.dp))

                Column{
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = reviewItem.review.username,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )

                    }

                    reviewItem.album?.title?.let { title ->
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    reviewItem.review.albumId.let { name ->
                        Text(
                            text = name,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                        RatingDisplay(
                            rating = reviewItem.review.rating.toFloat(),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )


                    Text(
                        text = reviewItem.review.content,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    reviewItem.review.favoriteTrack?.takeIf { it.isNotEmpty() }?.let {
                        Text(
                            text = "Favorite Track: $it",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

    }
}
