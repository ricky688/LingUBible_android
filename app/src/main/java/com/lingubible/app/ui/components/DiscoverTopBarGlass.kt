package com.lingubible.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lingubible.app.core.theme.isAppDarkTheme
import com.lingubible.app.core.theme.isOledBlackActive

/** A translucent glass edge that blends Discover's top bar into its scrolling content. */
@Composable
fun DiscoverTopBarGlass(modifier: Modifier = Modifier) {
    val isDark = isAppDarkTheme()
    val surface = if (isOledBlackActive()) Color.Black else MaterialTheme.colorScheme.surface
    val highlight = MaterialTheme.colorScheme.outlineVariant
    val primary = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(32.dp)
            .drawWithCache {
                val frost = Brush.verticalGradient(
                    0f to surface.copy(alpha = 0.90f),
                    0.20f to surface.copy(alpha = 0.70f),
                    0.60f to surface.copy(alpha = 0.28f),
                    1f to surface.copy(alpha = 0f)
                )
                val sheen = Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = if (isDark) 0.035f else 0.16f), Color.Transparent)
                )
                val glow = Brush.radialGradient(
                    colors = listOf(primary.copy(alpha = if (isDark) 0.035f else 0.045f), Color.Transparent),
                    center = Offset(size.width * 0.5f, -size.width * 0.25f),
                    radius = size.width * 0.4f
                )
                val edge = Brush.horizontalGradient(
                    listOf(Color.Transparent, highlight.copy(alpha = 0.28f), Color.Transparent)
                )
                onDrawBehind {
                    drawRect(frost)
                    drawRect(sheen)
                    drawRect(glow)
                    drawLine(edge, Offset.Zero, Offset(size.width, 0f), strokeWidth = 0.5.dp.toPx())
                }
            }
    )
}
