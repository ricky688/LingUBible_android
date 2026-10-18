package com.lingubible.app.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
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

/** Keep fades at edges with remaining content, including at rest. Reached edges clear immediately. */
fun Modifier.fadingBlurEdges(
    scrollState: ScrollState,
    surfaceColor: Color? = null,
    baseFadeHeight: Dp = 22.dp,
    scrollingFadeHeight: Dp = 34.dp,
    bottomInset: Dp = 0.dp
): Modifier = composed {
    scrollFadingEdges(scrollState.isScrollInProgress, scrollState.canScrollBackward, scrollState.canScrollForward,
        surfaceColor, baseFadeHeight, scrollingFadeHeight, bottomInset)
}

fun Modifier.fadingBlurEdges(
    lazyListState: LazyListState,
    surfaceColor: Color? = null,
    baseFadeHeight: Dp = 22.dp,
    scrollingFadeHeight: Dp = 34.dp,
    bottomInset: Dp = 0.dp
): Modifier = composed {
    scrollFadingEdges(lazyListState.isScrollInProgress, lazyListState.canScrollBackward, lazyListState.canScrollForward,
        surfaceColor, baseFadeHeight, scrollingFadeHeight, bottomInset)
}

fun Modifier.fadingBlurEdges(
    lazyGridState: LazyGridState,
    surfaceColor: Color? = null,
    bottomInset: Dp = 0.dp
): Modifier = composed {
    scrollFadingEdges(lazyGridState.isScrollInProgress, lazyGridState.canScrollBackward, lazyGridState.canScrollForward,
        surfaceColor, 22.dp, 34.dp, bottomInset)
}

internal data class ScrollEdgeVisibility(val top: Boolean, val bottom: Boolean)
internal fun scrollEdgeVisibility(canScrollBackward: Boolean, canScrollForward: Boolean) =
    ScrollEdgeVisibility(canScrollBackward, canScrollForward)

internal fun scrollEdgeAlpha(animatedAlpha: Float, canScroll: Boolean): Float =
    if (canScroll) animatedAlpha.coerceIn(0f, 1f) else 0f

@Composable
private fun Modifier.scrollFadingEdges(
    isScrolling: Boolean, canScrollUp: Boolean, canScrollDown: Boolean,
    surfaceColor: Color?, baseFadeHeight: Dp, scrollingFadeHeight: Dp, bottomInset: Dp
): Modifier {
    val isDark = isAppDarkTheme()
    val resolvedSurfaceColor = surfaceColor ?: MaterialTheme.colorScheme.background
    val visibility = scrollEdgeVisibility(canScrollUp, canScrollDown)
    val animatedFadeHeight by animateDpAsState(
        if (isScrolling) scrollingFadeHeight else baseFadeHeight,
        spring(stiffness = Spring.StiffnessMediumLow), label = "scrollFadeHeight"
    )
    val animatedTopAlpha by animateFloatAsState(
        if (visibility.top) 1f else 0f,
        spring(stiffness = Spring.StiffnessMediumLow), label = "scrollFadeTop"
    )
    val animatedBottomAlpha by animateFloatAsState(
        if (visibility.bottom) 1f else 0f,
        spring(stiffness = Spring.StiffnessMediumLow), label = "scrollFadeBottom"
    )
    // Springs may overshoot; a reached boundary must never retain its previous mask.
    val topAlpha = scrollEdgeAlpha(animatedTopAlpha, canScrollUp)
    val bottomAlpha = scrollEdgeAlpha(animatedBottomAlpha, canScrollDown)
    return this
        .clipToBounds()
        .graphicsLayer {
            compositingStrategy = if (topAlpha > 0.001f || bottomAlpha > 0.001f) CompositingStrategy.Offscreen else CompositingStrategy.Auto
        }
        .drawWithContent {
            drawContent()

            val fadePx = animatedFadeHeight.toPx().coerceIn(0f, (size.height - bottomInset.toPx()).coerceAtLeast(0f) / 2f)

            if (topAlpha > 0.001f && fadePx > 0f) {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 1f - topAlpha),
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
                            resolvedSurfaceColor.copy(alpha = 0.95f * topAlpha),
                            resolvedSurfaceColor.copy(alpha = 0.70f * topAlpha),
                            resolvedSurfaceColor.copy(alpha = 0.35f * topAlpha),
                            resolvedSurfaceColor.copy(alpha = 0.10f * topAlpha),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = fadePx
                    ),
                    topLeft = Offset.Zero,
                    size = Size(size.width, fadePx)
                )

                drawLine(
                    color = (if (isDark) Color.White else Color(0xFF94A3B8)).copy(alpha = 0.12f * topAlpha),
                    start = Offset.Zero,
                    end = Offset(size.width, 0f),
                    strokeWidth = 0.5.dp.toPx()
                )
            }

            val (fadeStart, fadeEnd) = bottomFadeRange(size.height, bottomInset.toPx(), fadePx)
            if (bottomAlpha > 0.001f && fadeEnd > fadeStart) {
                // Keep the mask clamped below the bar so faded content cannot reappear underneath it.
                drawRect(
                    brush = Brush.verticalGradient(
                        colorStops = arrayOf(
                            0f to Color.Black,
                            0.25f to Color.Black.copy(alpha = 1f - 0.15625f * bottomAlpha),
                            0.5f to Color.Black.copy(alpha = 1f - 0.5f * bottomAlpha),
                            0.75f to Color.Black.copy(alpha = 1f - 0.84375f * bottomAlpha),
                            1f to Color.Black.copy(alpha = 1f - bottomAlpha)
                        ),
                        startY = fadeStart,
                        endY = fadeEnd
                    ),
                    blendMode = BlendMode.DstIn,
                    topLeft = Offset(0f, fadeStart),
                    size = Size(size.width, size.height - fadeStart)
                )
            }
        }
}

/** Place the fade above an overlaid navigation bar, including in very short viewports. */
internal fun bottomFadeRange(viewHeight: Float, bottomInset: Float, fadeHeight: Float): Pair<Float, Float> {
    val end = (viewHeight - bottomInset.coerceAtLeast(0f)).coerceAtLeast(0f)
    return (end - fadeHeight.coerceAtLeast(0f)).coerceAtLeast(0f) to end
}
