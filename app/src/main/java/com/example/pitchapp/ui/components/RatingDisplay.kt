package com.example.pitchapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.dp
import kotlin.math.floor

@Composable
fun RatingDisplay(
    rating: Float,
    modifier: Modifier = Modifier,
) {
    val starSize = 32
    val filledColor = MaterialTheme.colorScheme.onPrimary
    val emptyColor = MaterialTheme.colorScheme.outline
    val filledStars = floor(rating).toInt()
    val halfStar = rating - filledStars >= 0.5f

    Row(modifier = modifier) {
        repeat(filledStars) {
            StarIcon(filled = true, size = starSize, color = filledColor)
        }
        if (halfStar) {
            HalfStarIcon(size = starSize, filledColor = filledColor, outlineColor = emptyColor)
        }
        repeat(5 - filledStars - if (halfStar) 1 else 0) {
            StarIcon(filled = false, size = starSize, color = emptyColor)
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = "%.1f".format(rating),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun StarIcon(
    filled: Boolean,
    size: Int,
    color: androidx.compose.ui.graphics.Color
) {
    Canvas(modifier = Modifier.size(size.dp)) {
        val path = Path().apply {
            moveTo(12f, 2f)
            lineTo(15.09f, 8.26f)
            lineTo(22f, 9.27f)
            lineTo(17f, 14.14f)
            lineTo(18.18f, 21.02f)
            lineTo(12f, 17.77f)
            lineTo(5.82f, 21.02f)
            lineTo(7f, 14.14f)
            lineTo(2f, 9.27f)
            lineTo(8.91f, 8.26f)
            lineTo(12f, 2f)
            close()
        }

        drawPath(
            path = path,
            color = color
        )
    }
}

@Composable
private fun HalfStarIcon(
    size: Int,
    filledColor: androidx.compose.ui.graphics.Color,
    outlineColor: androidx.compose.ui.graphics.Color
) {
    Canvas(modifier = Modifier.size(size.dp)) {
        val starPath = Path().apply {
            moveTo(12f, 2f)
            lineTo(15.09f, 8.26f)
            lineTo(22f, 9.27f)
            lineTo(17f, 14.14f)
            lineTo(18.18f, 21.02f)
            lineTo(12f, 17.77f)
            lineTo(5.82f, 21.02f)
            lineTo(7f, 14.14f)
            lineTo(2f, 9.27f)
            lineTo(8.91f, 8.26f)
            lineTo(12f, 2f)
            close()
        }

        clipPath(starPath) {
            drawRect(
                color = filledColor,
                topLeft = Offset.Zero,
                size = androidx.compose.ui.geometry.Size(this.size.width / 2, this.size.height)
            )
        }
        drawPath(
            path = starPath,
            color = outlineColor,
            style = Stroke(1.dp.toPx())
        )
    }
}