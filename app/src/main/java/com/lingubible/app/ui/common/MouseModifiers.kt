package com.lingubible.app.ui.common

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Ensures any interactive component shows a hand pointer cursor when hovered with a mouse.
 */
fun Modifier.mousePointer(enabled: Boolean = true): Modifier =
    this.pointerHoverIcon(if (enabled) PointerIcon.Hand else PointerIcon.Default)

/**
 * Text selection cursor for selectable text or editable input fields.
 */
fun Modifier.mouseTextPointer(): Modifier =
    this.pointerHoverIcon(PointerIcon.Text)

/**
 * Clickable modifier with built-in mouse hand pointer cursor.
 */
fun Modifier.mouseClickable(
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    interactionSource: MutableInteractionSource? = null,
    onClick: () -> Unit
): Modifier = composed {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    this
        .mousePointer(enabled)
        .clickable(
            enabled = enabled,
            onClickLabel = onClickLabel,
            role = role,
            interactionSource = source,
            indication = null,
            onClick = hapticClick(onClick)
        )
}

/**
 * Hover feedback; press outlines are owned by the shared button/card styles.
 */
fun Modifier.mouseHoverSpring(
    hoverScale: Float = 1.015f,
    interactionSource: MutableInteractionSource? = null
): Modifier = composed {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val isHovered by source.collectIsHoveredAsState()

    val scale by animateFloatAsState(
        targetValue = when {
            isHovered -> hoverScale
            else -> 1.0f
        },
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "mouseHoverScale"
    )

    this
        .mousePointer(true)
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
}

/**
 * A sleek, semi-transparent vertical scrollbar for mouse users navigating long lists.
 */
fun Modifier.mouseScrollbar(
    state: LazyListState,
    width: Dp = 4.dp,
    thumbColor: Color = Color.Gray.copy(alpha = 0.45f)
): Modifier = drawWithContent {
    drawContent()
    val layoutInfo = state.layoutInfo
    val visibleItems = layoutInfo.visibleItemsInfo
    val totalItemsCount = layoutInfo.totalItemsCount

    if (totalItemsCount > 0 && (state.canScrollBackward || state.canScrollForward)) {
        // Pinned headers remain visible after their row has scrolled offscreen.
        val first = visibleItems.firstOrNull { it.index == state.firstVisibleItemIndex }
        val last = visibleItems.lastOrNull { it.index >= state.firstVisibleItemIndex }
        if (first != null && last != null) {
            val spacing = layoutInfo.mainAxisItemSpacing
            val firstPosition = first.index +
                (state.firstVisibleItemScrollOffset.toFloat() / (first.size + spacing).coerceAtLeast(1)).coerceIn(0f, 1f)
            val lastPosition = last.index +
                ((layoutInfo.viewportEndOffset - last.offset).toFloat() / (last.size + spacing).coerceAtLeast(1)).coerceIn(0f, 1f)
            // Lists include measured navigation clearance in their trailing padding.
            val viewHeight = (size.height - layoutInfo.afterContentPadding).coerceAtLeast(0f)
            val minThumbHeight = 36.dp.toPx()
            val thumbHeight = scrollbarThumbHeight(viewHeight, (lastPosition - firstPosition) / totalItemsCount, minThumbHeight)
            val scrollableRange = viewHeight - thumbHeight
            val scrollProgress = when {
                !state.canScrollBackward -> 0f
                !state.canScrollForward -> 1f
                else -> scrollbarProgress(firstPosition, lastPosition, totalItemsCount)
            }
            val thumbOffsetY = scrollProgress * scrollableRange

            drawRoundRect(
                color = thumbColor,
                topLeft = Offset(size.width - width.toPx() - 2.dp.toPx(), thumbOffsetY),
                size = Size(width.toPx(), thumbHeight),
                cornerRadius = CornerRadius(width.toPx() / 2, width.toPx() / 2)
            )
        }
    }
}

/**
 * A sleek vertical scrollbar for regular ScrollState (e.g. Column with verticalScroll).
 */
fun Modifier.mouseScrollbar(
    state: androidx.compose.foundation.ScrollState,
    width: Dp = 4.dp,
    thumbColor: Color = Color.Gray.copy(alpha = 0.45f)
): Modifier = drawWithContent {
    drawContent()
    val maxValue = state.maxValue
    if (maxValue > 0) {
        val viewHeight = size.height
        val minThumbHeight = 36.dp.toPx()
        val totalHeight = viewHeight + maxValue
        val thumbHeight = scrollbarThumbHeight(viewHeight, viewHeight / totalHeight, minThumbHeight)
        val scrollableRange = viewHeight - thumbHeight
        val scrollProgress = (state.value.toFloat() / maxValue).coerceIn(0f, 1f)
        val thumbOffsetY = scrollProgress * scrollableRange

        drawRoundRect(
            color = thumbColor,
            topLeft = Offset(size.width - width.toPx() - 2.dp.toPx(), thumbOffsetY),
            size = Size(width.toPx(), thumbHeight),
            cornerRadius = CornerRadius(width.toPx() / 2, width.toPx() / 2)
        )
    }
}

/** Fractional rows keep mixed-height sections continuous without counting pinned headers. */
internal fun scrollbarProgress(firstPosition: Float, lastPosition: Float, totalItemsCount: Int): Float {
    val scrollableItems = totalItemsCount - (lastPosition - firstPosition)
    return if (scrollableItems > 0f) (firstPosition / scrollableItems).coerceIn(0f, 1f) else 0f
}

/** Resize can briefly draw a viewport smaller than the preferred thumb, including zero. */
internal fun scrollbarThumbHeight(viewHeight: Float, visibleFraction: Float, minThumbHeight: Float): Float {
    if (viewHeight <= 0f) return 0f
    return (viewHeight * visibleFraction).coerceIn(minThumbHeight.coerceAtMost(viewHeight), viewHeight)
}
