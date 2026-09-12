package com.lingubible.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lingubible.app.core.theme.*

/**
 * Material 3 Expressive Frosted Glass (Glassmorphism) Header Container.
 *
 * Adheres strictly to repository UI guidelines:
 * - Translucent surface layer (alpha = 0.70f - 0.85f)
 * - Fine specular linear gradient borders
 * - Top sheen specular highlights
 * - Subtle atmospheric radial glow underlays (Lingnan Red / Indigo)
 * - Smooth visual legibility for list items scrolling beneath
 */
@Composable
fun FrostedGlassHeader(
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    borderBottomOnly: Boolean = true,
    showSheen: Boolean = false,
    showGlow: Boolean = false,
    showShadow: Boolean = false,
    sheenHeight: Dp = 26.dp,
    borderWidth: Dp = 1.2.dp,
    surfaceAlpha: Float = 0.85f,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isAppDarkTheme()

    // 1. Translucent Surface Layer (0.75f - 0.85f)
    val targetFrostedSurfaceColor = if (isDark) {
        if (isOledBlackActive()) Color(0xFF000000).copy(alpha = surfaceAlpha)
        else MaterialTheme.colorScheme.surface.copy(alpha = surfaceAlpha)
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = surfaceAlpha + 0.02f)
    }
    val frostedSurfaceColor by animateColorAsState(
        targetValue = targetFrostedSurfaceColor,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "frostedHeaderSurface"
    )

    // 2. Specular Linear Gradient Border Colors
    val targetHighlightBase = if (isDark) {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.75f)
    } else {
        Color(0xFFCBD5E1).copy(alpha = 0.85f)
    }
    val highlightBase by animateColorAsState(
        targetValue = targetHighlightBase,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "frostedHeaderHighlight"
    )
    val glassBorderColors = remember(highlightBase) {
        listOf(
            highlightBase.copy(alpha = 0.25f),
            highlightBase,
            highlightBase.copy(alpha = 0.25f)
        )
    }

    // 3. Top Sheen Specular Highlight Colors
    val sheenBase = if (isDark) Color.White.copy(alpha = 0.10f) else Color.White.copy(alpha = 0.45f)
    val glassSheenColors = remember(sheenBase) {
        listOf(sheenBase, Color.Transparent)
    }

    // 4. Subtle Diffuse Shadow Colors directly underneath the bottom border
    val glassShadowColor = if (isDark) Color.Black.copy(alpha = 0.28f) else Color(0xFF94A3B8).copy(alpha = 0.14f)
    val glassShadowColors = remember(glassShadowColor) {
        listOf(glassShadowColor, Color.Transparent)
    }

    // 5. Subtle Atmospheric Radial Glow Underlay
    val glowPrimary = MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.15f else 0.07f)
    val glowSecondary = Color(0xFF6366F1).copy(alpha = if (isDark) 0.12f else 0.05f)
    val glowColors = remember(glowPrimary) { listOf(glowPrimary, Color.Transparent) }
    val glowSecondaryColors = remember(glowSecondary) { listOf(glowSecondary, Color.Transparent) }

    val borderModifier = if (!borderBottomOnly) {
        Modifier.border(
            width = borderWidth,
            brush = Brush.linearGradient(glassBorderColors),
            shape = shape
        )
    } else Modifier

    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(borderModifier)
            .clip(shape)
            .drawBehind {
                // 1. Frosted glass translucent base background
                drawRect(color = frostedSurfaceColor)

                // 2. Atmospheric ambient radial glow underlays (optional, off by default to prevent reflection glare)
                if (showGlow) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = glowColors,
                            center = Offset(size.width * 0.12f, size.height * 0.45f),
                            radius = 150.dp.toPx()
                        )
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = glowSecondaryColors,
                            center = Offset(size.width * 0.88f, size.height * 0.55f),
                            radius = 130.dp.toPx()
                        )
                    )
                }

                // 3. Specular top sheen overlay (optional, off by default to eliminate artificial reflection bands)
                if (showSheen && sheenHeight > 0.dp) {
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = glassSheenColors,
                            startY = 0f,
                            endY = sheenHeight.toPx()
                        )
                    )
                }

                // 4. Fine specular linear gradient border
                if (borderBottomOnly) {
                    val borderHalfStroke = borderWidth.toPx() / 2f
                    val borderY = size.height - borderHalfStroke
                    drawLine(
                        brush = Brush.horizontalGradient(glassBorderColors),
                        start = Offset(0f, borderY),
                        end = Offset(size.width, borderY),
                        strokeWidth = borderWidth.toPx()
                    )

                    // 5. Delicate glass shadow/diffuse fade directly beneath the border (optional)
                    if (showShadow) {
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = glassShadowColors,
                                startY = size.height,
                                endY = size.height + 5.dp.toPx()
                            ),
                            topLeft = Offset(0f, size.height),
                            size = Size(size.width, 5.dp.toPx())
                        )
                    }
                }
            }
    ) {
        content()
    }
}
