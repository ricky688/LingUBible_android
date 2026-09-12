package com.lingubible.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
 * - Spring press-scale interaction physics
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
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()

    // Press and hover scale with spring physics
    val fabScale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.93f
            isHovered -> 1.035f
            else -> 1.0f
        },
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "fabScale"
    )

    // Fast, snappy Google M3 collapse (StiffnessHigh)
    // and smooth spring expand (StiffnessMedium)
    val translationX by animateDpAsState(
        targetValue = if (expanded) 0.dp else collapsedTranslationX,
        animationSpec = if (expanded) {
            spring(stiffness = Spring.StiffnessMedium)
        } else {
            spring(stiffness = Spring.StiffnessHigh)
        },
        label = "fabTranslationX"
    )
    val translationY by animateDpAsState(
        targetValue = if (expanded) 0.dp else collapsedTranslationY,
        animationSpec = if (expanded) {
            spring(stiffness = Spring.StiffnessMedium)
        } else {
            spring(stiffness = Spring.StiffnessHigh)
        },
        label = "fabTranslationY"
    )

    val shadowColor = MaterialTheme.colorScheme.primary.copy(alpha = if (isHovered) 0.5f else 0.35f)

    Surface(
        onClick = onClick,
        modifier = modifier
            .offset(x = translationX, y = translationY)
            .pointerHoverIcon(PointerIcon.Hand)
            .graphicsLayer {
                scaleX = fabScale
                scaleY = fabScale
            }
            .shadow(
                elevation = if (isHovered) 12.dp else 8.dp,
                shape = shape,
                ambientColor = shadowColor,
                spotColor = shadowColor
            ),
        shape = shape,
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
                    animationSpec = if (expanded) {
                        spring(stiffness = Spring.StiffnessMedium)
                    } else {
                        spring(stiffness = Spring.StiffnessHigh)
                    }
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
                    animationSpec = spring(stiffness = Spring.StiffnessMedium)
                ) + expandHorizontally(
                    animationSpec = spring(stiffness = Spring.StiffnessMedium),
                    expandFrom = Alignment.Start
                ),
                exit = fadeOut(
                    animationSpec = tween(durationMillis = 100, easing = FastOutLinearInEasing)
                ) + shrinkHorizontally(
                    animationSpec = spring(stiffness = Spring.StiffnessHigh),
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
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()

    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.95f
            isHovered -> 1.03f
            else -> 1.0f
        },
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "inlineButtonScale"
    )

    Button(
        onClick = onClick,
        modifier = modifier
            .pointerHoverIcon(PointerIcon.Hand)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = when {
                    isPressed -> 8.dp
                    isHovered -> 7.dp
                    else -> 4.dp
                },
                shape = RoundedCornerShape(22.dp),
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = if (isHovered) 0.45f else 0.3f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = if (isHovered) 0.45f else 0.3f)
            ),
        shape = RoundedCornerShape(22.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
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
    return remember(lazyListState) {
        var previousIndex = lazyListState.firstVisibleItemIndex
        var previousScrollOffset = lazyListState.firstVisibleItemScrollOffset
        derivedStateOf {
            val currentIndex = lazyListState.firstVisibleItemIndex
            val currentOffset = lazyListState.firstVisibleItemScrollOffset

            val isScrollingDown = if (currentIndex != previousIndex) {
                currentIndex > previousIndex
            } else {
                currentOffset > previousScrollOffset
            }
            val isScrollingUp = if (currentIndex != previousIndex) {
                currentIndex < previousIndex
            } else {
                currentOffset < previousScrollOffset
            }

            previousIndex = currentIndex
            previousScrollOffset = currentOffset

            if (currentIndex == 0 && currentOffset <= 4) {
                true
            } else if (isScrollingDown) {
                false
            } else if (isScrollingUp) {
                true
            } else {
                true
            }
        }
    }
}
