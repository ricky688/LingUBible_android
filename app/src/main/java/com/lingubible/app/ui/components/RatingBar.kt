package com.lingubible.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.floor

@Composable
fun RatingBar(
    rating: Double,
    modifier: Modifier = Modifier,
    maxRating: Int = 5,
    starSize: Dp = 18.dp,
    starColor: Color = Color(0xFFF59E0B),
    onRatingChanged: ((Double) -> Unit)? = null
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (rating == -1.0) {
            Text(
                text = "N/A",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            val fullStars = floor(rating).toInt()
            val hasHalf = (rating - fullStars) >= 0.25 && (rating - fullStars) < 0.75
            val roundedFull = if ((rating - fullStars) >= 0.75) fullStars + 1 else fullStars

            for (i in 1..maxRating) {
                val icon = when {
                    i <= roundedFull -> Icons.Filled.Star
                    i == roundedFull + 1 && hasHalf -> Icons.Filled.StarHalf
                    else -> Icons.Outlined.StarOutline
                }

                Icon(
                    imageVector = icon,
                    contentDescription = "Star $i",
                    tint = starColor,
                    modifier = Modifier
                        .size(starSize)
                        .then(
                            if (onRatingChanged != null) {
                                Modifier
                                    .pointerHoverIcon(PointerIcon.Hand)
                                    .clickable { onRatingChanged(i.toDouble()) }
                            } else Modifier
                        )
                )
            }
        }
    }
}
