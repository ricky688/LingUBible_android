package com.lingubible.app.ui.components

import com.lingubible.app.core.theme.AppMotion

import com.lingubible.app.ui.common.hapticClick

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lingubible.app.core.theme.LingnanRed
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Material 3 Expressive Squircle Shape with continuous superellipse curvature.
 * Provides smooth continuous G2 curvature transition without circular arc discontinuities.
 */
class SquircleShape(
    val cornerRadius: Dp = 24.dp,
    val smoothness: Float = 0.75f
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val r = with(density) { cornerRadius.toPx() }.coerceAtMost(minOf(size.width, size.height) / 2f)
        val w = size.width
        val h = size.height

        val path = Path().apply {
            val c = r * (0.552285f + smoothness * 0.22f)

            moveTo(r, 0f)
            lineTo(w - r, 0f)
            cubicTo(w - r + c, 0f, w, r - c, w, r)
            lineTo(w, h - r)
            cubicTo(w, h - r + c, w - r + c, h, w - r, h)
            lineTo(r, h)
            cubicTo(r - c, h, 0f, h - r + c, 0f, h - r)
            lineTo(0f, r)
            cubicTo(0f, r - c, r - c, 0f, r, 0f)
            close()
        }
        return Outline.Generic(path)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SquircleShape) return false
        return cornerRadius == other.cornerRadius && smoothness == other.smoothness
    }

    override fun hashCode(): Int {
        var result = cornerRadius.hashCode()
        result = 31 * result + smoothness.hashCode()
        return result
    }
}

/**
 * Material 3 Expressive Extended Floating Action Button for entering review / comment writing.
 *
 * Features:
 * - Dynamic scroll-aware collapsing/expanding (shrinks to compact icon FAB when scrolling down, expands when scrolling up/idle)
 * - Material 3 Expressive squircle geometry (SquircleShape 24.dp)
 * - Spring corner-shape press interaction
 * - Subtle ambient red glow shadow
 * - High-contrast crisp iconography and bold typography
 */
@Composable
fun ExpressiveReviewFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    expanded: Boolean = true,
    text: String = "撰寫評價",
    contentDescription: String = "Write Review",
    collapsedTranslationX: androidx.compose.ui.unit.Dp = 0.dp,
    collapsedTranslationY: androidx.compose.ui.unit.Dp = 0.dp,
    shape: Shape = SquircleShape(cornerRadius = 24.dp, smoothness = 0.75f)
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val pressCorner by AppButtonDefaults.pressCorner(interactionSource, 24.dp)
    val animatedShape = if (pressCorner < 24.dp) RoundedCornerShape(pressCorner) else shape

    // Match the native Expressive button size/shape spring.
    val translationX by animateDpAsState(
        targetValue = if (expanded) 0.dp else collapsedTranslationX,
        animationSpec = AppMotion.press(),
        label = "fabTranslationX"
    )
    val translationY by animateDpAsState(
        targetValue = if (expanded) 0.dp else collapsedTranslationY,
        animationSpec = AppMotion.press(),
        label = "fabTranslationY"
    )

    val shadowColor = MaterialTheme.colorScheme.primary.copy(alpha = if (isHovered) 0.5f else 0.35f)

    Surface(
        onClick = hapticClick(onClick),
        modifier = modifier
            .offset(x = translationX, y = translationY)
            .pointerHoverIcon(PointerIcon.Hand)
            .shadow(
                elevation = if (isHovered) 12.dp else 8.dp,
                shape = animatedShape,
                ambientColor = shadowColor,
                spotColor = shadowColor
            ),
        shape = animatedShape,
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        interactionSource = interactionSource
    ) {
        Row(
            modifier = Modifier
                .defaultMinSize(minWidth = 56.dp, minHeight = 56.dp)
                .padding(
                    start = 16.dp,
                    end = if (expanded) 20.dp else 16.dp,
                    top = 14.dp,
                    bottom = 14.dp
                )
                .animateContentSize(
                    animationSpec = AppMotion.press()
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.RateReview,
                contentDescription = contentDescription,
                modifier = Modifier.size(24.dp),
                tint = Color.White
            )

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(
                    animationSpec = AppMotion.effects()
                ) + expandHorizontally(
                    animationSpec = AppMotion.press(),
                    expandFrom = Alignment.Start
                ),
                exit = fadeOut(
                    animationSpec = AppMotion.effects()
                ) + shrinkHorizontally(
                    animationSpec = AppMotion.press(),
                    shrinkTowards = Alignment.Start
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 10.dp)
                ) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.3.sp,
                        color = Color.White,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/**
 * Material 3 Expressive In-line Action Button for prominent empty states or hero sections.
 */
@Composable
fun ExpressiveInlineReviewButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = "搶先評價 Be First to Review"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Button(
        onClick = hapticClick(onClick),
        modifier = modifier
            .pointerHoverIcon(PointerIcon.Hand)
            .shadow(
                elevation = when {
                    isHovered -> 7.dp
                    else -> 4.dp
                },
                shape = RoundedCornerShape(22.dp),
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = if (isHovered) 0.45f else 0.3f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = if (isHovered) 0.45f else 0.3f)
            ),
        shapes = AppButtonDefaults.shapes(shape = RoundedCornerShape(22.dp)),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 4.dp
        ),
        interactionSource = interactionSource
    ) {
        Icon(
            imageVector = Icons.Filled.RateReview,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onPrimary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = Color.White
        )
    }
}

/**
 * Remembers whether the user is currently scrolling up or at the top of the list,
 * enabling Material 3 Expressive Extended FAB auto-collapse and auto-expand.
 */
@Composable
fun rememberIsScrollingUp(lazyListState: LazyListState): State<Boolean> {
    val expanded = remember(lazyListState) { mutableStateOf(!lazyListState.canScrollBackward) }
    LaunchedEffect(lazyListState) {
        var previousIndex = lazyListState.firstVisibleItemIndex
        var previousScrollOffset = lazyListState.firstVisibleItemScrollOffset
        snapshotFlow {
            lazyListState.firstVisibleItemIndex to lazyListState.firstVisibleItemScrollOffset
        }.distinctUntilChanged().collect { (currentIndex, currentOffset) ->
            expanded.value = when {
                currentIndex == 0 && currentOffset == 0 -> true
                currentIndex > previousIndex -> false
                currentIndex < previousIndex -> true
                currentOffset > previousScrollOffset -> false
                currentOffset < previousScrollOffset -> true
                else -> expanded.value
            }
            previousIndex = currentIndex
            previousScrollOffset = currentOffset
        }
    }
    return expanded
}
