package com.example.pitchapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.size
@Composable
fun RatingBar(
    modifier: Modifier = Modifier,
    rating: Int,
    maxStars: Int = 5,
    onRatingChanged: (Int) -> Unit = {}
) {
    Row(modifier) {
        repeat(maxStars) { index ->
            val starValue = index + 1
            Icon(
                imageVector = if (starValue <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = "Rating",
                tint = if (starValue <= rating) Color(0xFFFFD700) else Color.Gray,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onRatingChanged(starValue) }
            )
        }
    }
}