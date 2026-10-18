package com.lingubible.app.ui.components

import com.lingubible.app.ui.common.hapticClick

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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun FavoriteButton(
    type: String,
    itemId: String,
    modifier: Modifier = Modifier,
    size: Dp = 20.dp
) {
    val repository = org.koin.compose.koinInject<com.lingubible.app.data.repository.AppwriteFavoritesRepository>()
    val state by repository.state.collectAsStateWithLifecycle()
    val isToggled = com.lingubible.app.data.repository.FavoriteKey(type, itemId) in state.items
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    var busy by remember { mutableStateOf(false) }
    var triggerBounce by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (triggerBounce) 1.25f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        finishedListener = {
            if (triggerBounce) triggerBounce = false
        },
        label = "favoriteScale"
    )

    IconButton(shapes = AppButtonDefaults.iconShapes(),
        onClick = hapticClick {
            busy = true
            scope.launch {
                try {
                    repository.toggle(type, itemId).onSuccess { triggerBounce = true }.onFailure {
                        android.widget.Toast.makeText(context, it.localizedMessage ?: "收藏失敗 · Could not save favourite", android.widget.Toast.LENGTH_LONG).show()
                    }
                } finally { busy = false }
            }
        },
        enabled = !busy && !state.loading,
        modifier = modifier
            .size((size + 16.dp).coerceAtLeast(48.dp))
            .pointerHoverIcon(PointerIcon.Hand)
    ) {
        Icon(
            imageVector = if (isToggled) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = if (isToggled) "Remove from favorites: $itemId" else "Add to favorites: $itemId",
            tint = if (isToggled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier
                .size(size)
                .scale(scale)
        )
    }
}
