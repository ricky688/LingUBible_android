package com.lingubible.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun FavoriteButton(
    isFavorited: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 20.dp
) {
    var isToggled by remember(isFavorited) { mutableStateOf(isFavorited) }
    var triggerBounce by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (triggerBounce) 1.25f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        finishedListener = {
            if (triggerBounce) triggerBounce = false
        },
        label = "favoriteScale"
    )

    IconButton(
        onClick = {
            val newState = !isToggled
            isToggled = newState
            triggerBounce = true
            onToggle(newState)
        },
        modifier = modifier
            .size(size + 16.dp)
            .pointerHoverIcon(PointerIcon.Hand)
    ) {
        Icon(
            imageVector = if (isToggled) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = if (isToggled) "Remove from favorites" else "Add to favorites",
            tint = if (isToggled) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier
                .size(size)
                .scale(scale)
        )
    }
}
