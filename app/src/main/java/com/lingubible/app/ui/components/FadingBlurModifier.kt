package com.lingubible.app.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lingubible.app.core.theme.isAppDarkTheme
import com.lingubible.app.core.theme.isOledBlackActive

/**
 * Material 3 Expressive Dynamic Scroll-Fading Blur Modifier.
 *
 * Smoothly dissolves the top and bottom edges of a scrollable container into a frosted blur effect
 * when the user scrolls:
 * - Top edge fades out smoothly when content can scroll backward (user has scrolled down).
 * - Bottom edge fades out smoothly when content can scroll forward (more items remain below).
 * - While the user is actively scrolling (`isScrollInProgress`), the fade-out blur depth and
 *   opacity dynamically expand with pure Material 3 Expressive spring stiffness motion.
 * - Employs hardware-accelerated Offscreen compositing with [BlendMode.DstIn] for an exact
 *   alpha mask dissolve, combined with a multi-stop cubic frosted mist gradient overlay
 *   to emulate true optical blur dissipation.
 */
fun Modifier.fadingBlurEdges(
    scrollState: ScrollState,
    surfaceColor: Color? = null,
    baseFadeHeight: Dp = 22.dp,
    scrollingFadeHeight: Dp = 34.dp
): Modifier = composed {
    val isDark = isAppDarkTheme()
    val isOled = isOledBlackActive()
    val resolvedSurfaceColor = surfaceColor ?: if (isDark) {
        if (isOled) Color.Black else MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.surface
    }

    val isScrolling = scrollState.isScrollInProgress

    // Dynamic fade height: expands with bouncy spring motion while the user is actively scrolling
    val targetFadeHeight = if (isScrolling) scrollingFadeHeight else baseFadeHeight
    val animatedFadeHeight by animateDpAsState(
        targetValue = targetFadeHeight,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "fadingBlurEdgesHeight"
    )

    // Top fade: active when user can scroll backward (up)
    val canScrollUp = scrollState.canScrollBackward
    val targetTopAlpha = if (canScrollUp) (if (isScrolling) 1.0f else 0.90f) else 0f
    val animatedTopAlpha by animateFloatAsState(
        targetValue = targetTopAlpha,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "fadingBlurEdgesTopAlpha"
    )

    // Bottom fade: active when user can scroll forward (down)
    val canScrollDown = scrollState.canScrollForward
    val targetBottomAlpha = if (canScrollDown) (if (isScrolling) 1.0f else 0.90f) else 0f
    val animatedBottomAlpha by animateFloatAsState(
        targetValue = targetBottomAlpha,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "fadingBlurEdgesBottomAlpha"
    )

    this
        .clipToBounds()
        .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
        .drawWithContent {
            // 1. Draw the underlying child content
            drawContent()

            val fadePx = animatedFadeHeight.toPx()

            // 2. Alpha fade out at top edge (dissolve content to 0 alpha)
            if (animatedTopAlpha > 0.001f && fadePx > 0f) {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 1f - animatedTopAlpha),
                            Color.Black
                        ),
                        startY = 0f,
                        endY = fadePx
                    ),
                    blendMode = BlendMode.DstIn,
                    topLeft = Offset.Zero,
                    size = Size(size.width, fadePx)
                )

                // Frosted mist multi-stop gradient overlay to emulate optical blur dissipation
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            resolvedSurfaceColor.copy(alpha = 0.95f * animatedTopAlpha),
                            resolvedSurfaceColor.copy(alpha = 0.70f * animatedTopAlpha),
                            resolvedSurfaceColor.copy(alpha = 0.35f * animatedTopAlpha),
                            resolvedSurfaceColor.copy(alpha = 0.10f * animatedTopAlpha),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = fadePx
                    ),
                    topLeft = Offset.Zero,
                    size = Size(size.width, fadePx)
                )

                // Specular hairline accent at top boundary
                drawLine(
                    color = (if (isDark) Color.White else Color(0xFF94A3B8)).copy(alpha = 0.12f * animatedTopAlpha),
                    start = Offset.Zero,
                    end = Offset(size.width, 0f),
                    strokeWidth = 0.5.dp.toPx()
                )
            }

            // 3. Alpha fade out at bottom edge (dissolve content to 0 alpha)
            if (animatedBottomAlpha > 0.001f && fadePx > 0f) {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black,
                            Color.Black.copy(alpha = 1f - animatedBottomAlpha)
                        ),
                        startY = size.height - fadePx,
                        endY = size.height
                    ),
                    blendMode = BlendMode.DstIn,
                    topLeft = Offset(0f, size.height - fadePx),
                    size = Size(size.width, fadePx)
                )

                // Frosted mist multi-stop gradient overlay to emulate optical blur dissipation
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            resolvedSurfaceColor.copy(alpha = 0.10f * animatedBottomAlpha),
                            resolvedSurfaceColor.copy(alpha = 0.35f * animatedBottomAlpha),
                            resolvedSurfaceColor.copy(alpha = 0.70f * animatedBottomAlpha),
                            resolvedSurfaceColor.copy(alpha = 0.95f * animatedBottomAlpha)
                        ),
                        startY = size.height - fadePx,
                        endY = size.height
                    ),
                    topLeft = Offset(0f, size.height - fadePx),
                    size = Size(size.width, fadePx)
                )

                // Specular hairline accent at bottom boundary
                drawLine(
                    color = (if (isDark) Color.White else Color(0xFF94A3B8)).copy(alpha = 0.12f * animatedBottomAlpha),
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 0.5.dp.toPx()
                )
            }
        }
}

/**
 * LazyListState overload for [Modifier.fadingBlurEdges].
 */
fun Modifier.fadingBlurEdges(
    lazyListState: LazyListState,
    surfaceColor: Color? = null,
    baseFadeHeight: Dp = 22.dp,
    scrollingFadeHeight: Dp = 34.dp
): Modifier = composed {
    val isDark = isAppDarkTheme()
    val isOled = isOledBlackActive()
    val resolvedSurfaceColor = surfaceColor ?: if (isDark) {
        if (isOled) Color.Black else MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.surface
    }

    val isScrolling = lazyListState.isScrollInProgress

    val targetFadeHeight = if (isScrolling) scrollingFadeHeight else baseFadeHeight
    val animatedFadeHeight by animateDpAsState(
        targetValue = targetFadeHeight,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "fadingBlurEdgesLazyHeight"
    )

    val canScrollUp = lazyListState.canScrollBackward
    val targetTopAlpha = if (canScrollUp) (if (isScrolling) 1.0f else 0.90f) else 0f
    val animatedTopAlpha by animateFloatAsState(
        targetValue = targetTopAlpha,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "fadingBlurEdgesLazyTopAlpha"
    )

    val canScrollDown = lazyListState.canScrollForward
    val targetBottomAlpha = if (canScrollDown) (if (isScrolling) 1.0f else 0.90f) else 0f
    val animatedBottomAlpha by animateFloatAsState(
        targetValue = targetBottomAlpha,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "fadingBlurEdgesLazyBottomAlpha"
    )

    this
        .clipToBounds()
        .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
        .drawWithContent {
            drawContent()

            val fadePx = animatedFadeHeight.toPx()

            if (animatedTopAlpha > 0.001f && fadePx > 0f) {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 1f - animatedTopAlpha),
                            Color.Black
                        ),
                        startY = 0f,
                        endY = fadePx
                    ),
                    blendMode = BlendMode.DstIn,
                    topLeft = Offset.Zero,
                    size = Size(size.width, fadePx)
                )

                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            resolvedSurfaceColor.copy(alpha = 0.95f * animatedTopAlpha),
                            resolvedSurfaceColor.copy(alpha = 0.70f * animatedTopAlpha),
                            resolvedSurfaceColor.copy(alpha = 0.35f * animatedTopAlpha),
                            resolvedSurfaceColor.copy(alpha = 0.10f * animatedTopAlpha),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = fadePx
                    ),
                    topLeft = Offset.Zero,
                    size = Size(size.width, fadePx)
                )

                drawLine(
                    color = (if (isDark) Color.White else Color(0xFF94A3B8)).copy(alpha = 0.12f * animatedTopAlpha),
                    start = Offset.Zero,
                    end = Offset(size.width, 0f),
                    strokeWidth = 0.5.dp.toPx()
                )
            }

            if (animatedBottomAlpha > 0.001f && fadePx > 0f) {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black,
                            Color.Black.copy(alpha = 1f - animatedBottomAlpha)
                        ),
                        startY = size.height - fadePx,
                        endY = size.height
                    ),
                    blendMode = BlendMode.DstIn,
                    topLeft = Offset(0f, size.height - fadePx),
                    size = Size(size.width, fadePx)
                )

                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            resolvedSurfaceColor.copy(alpha = 0.10f * animatedBottomAlpha),
                            resolvedSurfaceColor.copy(alpha = 0.35f * animatedBottomAlpha),
                            resolvedSurfaceColor.copy(alpha = 0.70f * animatedBottomAlpha),
                            resolvedSurfaceColor.copy(alpha = 0.95f * animatedBottomAlpha)
                        ),
                        startY = size.height - fadePx,
                        endY = size.height
                    ),
                    topLeft = Offset(0f, size.height - fadePx),
                    size = Size(size.width, fadePx)
                )

                drawLine(
                    color = (if (isDark) Color.White else Color(0xFF94A3B8)).copy(alpha = 0.12f * animatedBottomAlpha),
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 0.5.dp.toPx()
                )
            }
        }
}
