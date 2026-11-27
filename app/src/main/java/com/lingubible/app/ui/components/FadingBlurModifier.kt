package com.lingubible.app.ui.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Scroll fade-out blur disabled app-wide per user request; keep clean unmasked scroll edges. */
@Suppress("UNUSED_PARAMETER")
fun Modifier.fadingBlurEdges(
    scrollState: ScrollState,
    surfaceColor: Color? = null,
    baseFadeHeight: Dp = 22.dp,
    scrollingFadeHeight: Dp = 34.dp,
    bottomInset: Dp = 0.dp
): Modifier = this

@Suppress("UNUSED_PARAMETER")
fun Modifier.fadingBlurEdges(
    lazyListState: LazyListState,
    surfaceColor: Color? = null,
    baseFadeHeight: Dp = 22.dp,
    scrollingFadeHeight: Dp = 34.dp,
    bottomInset: Dp = 0.dp
): Modifier = this

@Suppress("UNUSED_PARAMETER")
fun Modifier.fadingBlurEdges(
    lazyGridState: LazyGridState,
    surfaceColor: Color? = null,
    bottomInset: Dp = 0.dp
): Modifier = this

internal data class ScrollEdgeVisibility(val top: Boolean, val bottom: Boolean)
internal fun scrollEdgeVisibility(canScrollBackward: Boolean, canScrollForward: Boolean) =
    ScrollEdgeVisibility(canScrollBackward, canScrollForward)

internal fun scrollEdgeAlpha(animatedAlpha: Float, canScroll: Boolean): Float =
    if (canScroll) animatedAlpha.coerceIn(0f, 1f) else 0f

/** Place the fade above an overlaid navigation bar, including in very short viewports. */
internal fun bottomFadeRange(viewHeight: Float, bottomInset: Float, fadeHeight: Float): Pair<Float, Float> {
    val end = (viewHeight - bottomInset.coerceAtLeast(0f)).coerceAtLeast(0f)
    return (end - fadeHeight.coerceAtLeast(0f)).coerceAtLeast(0f) to end
}
