package com.lingubible.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lingubible.app.core.theme.*

/**
 * Data item representing a button within an [M3ButtonGroup].
 */
data class M3ButtonGroupItem(
    val label: String,
    val icon: ImageVector? = null,
    val onClick: () -> Unit
)

/**
 * Material 3 Expressive Button Group component with Shape Morphing.
 *
 * Implements Google Material Design 3 Expressive Button Group specifications:
 * - Active state: Morphs into a distinct Full Pill shape (20dp corner radius on all corners)
 *   with solid high-contrast fill (LingnanRed), white text, and an expressive checkmark (✓).
 * - Inactive state: Morphs into a Squircle / Rounded Rectangle shape (8dp corner radius on all corners)
 *   with soft tonal pastel surface fill (Lingnan rose pastel / dark wine tonal) and colored text.
 * - Dynamic corner radius animation on state transition using bouncy spring physics.
 * - Connected 4dp spacing between items.
 * - Borderless smooth surfaces matching Google M3 Expressive Connected Button Group specs.
 * - Tactile motion physics with bouncy springs and haptic feedback.
 * - Mouse cursor hand indicator (PointerIcon.Hand).
 */
@Composable
fun M3ButtonGroup(
    selectedIndex: Int,
    items: List<M3ButtonGroupItem>,
    modifier: Modifier = Modifier,
    spacing: Dp = 4.dp,
    height: Dp = 38.dp,
    activeCornerRadius: Dp = 20.dp,
    inactiveCornerRadius: Dp = 8.dp,
    showCheckmarkOnSelected: Boolean = true
) {
    val isDark = isAppDarkTheme()

    Row(
        modifier = modifier.height(height),
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEachIndexed { index, item ->
            M3ButtonGroupButton(
                selected = selectedIndex == index,
                onClick = item.onClick,
                label = item.label,
                icon = item.icon,
                isDark = isDark,
                activeCornerRadius = activeCornerRadius,
                inactiveCornerRadius = inactiveCornerRadius,
                showCheckmarkOnSelected = showCheckmarkOnSelected
            )
        }
    }
}

/**
 * Scrollable variant of [M3ButtonGroup] for category lists with many items.
 *
 * Implements Google Material Design 3 Expressive Connected Button Group specifications:
 * - Dynamic shape morphing between active Full Pill (20dp) and inactive Squircle (8dp).
 * - Smooth horizontal scrolling without breaking spring animations.
 * - Connected spacing between items.
 * - Tactile motion physics with bouncy springs and haptic feedback.
 */
@Composable
fun ScrollableM3ButtonGroup(
    selectedIndex: Int,
    items: List<M3ButtonGroupItem>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    spacing: Dp = 6.dp,
    height: Dp = 38.dp,
    activeCornerRadius: Dp = 20.dp,
    inactiveCornerRadius: Dp = 8.dp,
    showCheckmarkOnSelected: Boolean = true
) {
    val isDark = isAppDarkTheme()
    val scrollState = rememberScrollState()

    val canScrollBackward = scrollState.value > 0
    val canScrollForward = scrollState.value < scrollState.maxValue && scrollState.maxValue > 0

    val leftFadeAlpha by animateFloatAsState(
        targetValue = if (canScrollBackward) 1f else 0f,
        animationSpec = androidx.compose.animation.core.spring(stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow),
        label = "leftFadeAlpha"
    )
    val rightFadeAlpha by animateFloatAsState(
        targetValue = if (canScrollForward) 1f else 0f,
        animationSpec = androidx.compose.animation.core.spring(stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow),
        label = "rightFadeAlpha"
    )

    val fadeWidth = 24.dp

    Row(
        modifier = modifier
            .height(height)
            .graphicsLayer { compositingStrategy = androidx.compose.ui.graphics.CompositingStrategy.Offscreen }
            .drawWithContent {
                drawContent()
                val fadePx = fadeWidth.toPx()

                // Smooth leading edge fade-out transition
                if (leftFadeAlpha > 0.01f && fadePx > 0f) {
                    drawRect(
                        brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 1f - leftFadeAlpha),
                                Color.Black
                            ),
                            startX = 0f,
                            endX = fadePx
                        ),
                        blendMode = androidx.compose.ui.graphics.BlendMode.DstIn,
                        topLeft = androidx.compose.ui.geometry.Offset.Zero,
                        size = androidx.compose.ui.geometry.Size(fadePx, size.height)
                    )
                }

                // Smooth trailing edge fade-out transition
                if (rightFadeAlpha > 0.01f && fadePx > 0f) {
                    drawRect(
                        brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                            colors = listOf(
                                Color.Black,
                                Color.Black.copy(alpha = 1f - rightFadeAlpha)
                            ),
                            startX = size.width - fadePx,
                            endX = size.width
                        ),
                        blendMode = androidx.compose.ui.graphics.BlendMode.DstIn,
                        topLeft = androidx.compose.ui.geometry.Offset(size.width - fadePx, 0f),
                        size = androidx.compose.ui.geometry.Size(fadePx, size.height)
                    )
                }
            }
            .horizontalScroll(scrollState)
            .padding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEachIndexed { index, item ->
            M3ButtonGroupButton(
                selected = selectedIndex == index,
                onClick = item.onClick,
                label = item.label,
                icon = item.icon,
                isDark = isDark,
                activeCornerRadius = activeCornerRadius,
                inactiveCornerRadius = inactiveCornerRadius,
                showCheckmarkOnSelected = showCheckmarkOnSelected
            )
        }
    }
}

/**
 * Convenience overload for a standard two-action Material 3 Connected Button Group (e.g. Grid vs Add/Browse).
 */
@Composable
fun M3ButtonGroup(
    selectedIndex: Int,
    onLeadingClick: () -> Unit,
    onTrailingClick: () -> Unit,
    leadingText: String,
    trailingText: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    spacing: Dp = 4.dp,
    height: Dp = 38.dp,
    activeCornerRadius: Dp = 20.dp,
    inactiveCornerRadius: Dp = 8.dp,
    showCheckmarkOnSelected: Boolean = true
) {
    val items = listOf(
        M3ButtonGroupItem(label = leadingText, icon = leadingIcon, onClick = onLeadingClick),
        M3ButtonGroupItem(label = trailingText, icon = trailingIcon, onClick = onTrailingClick)
    )
    M3ButtonGroup(
        selectedIndex = selectedIndex,
        items = items,
        modifier = modifier,
        spacing = spacing,
        height = height,
        activeCornerRadius = activeCornerRadius,
        inactiveCornerRadius = inactiveCornerRadius,
        showCheckmarkOnSelected = showCheckmarkOnSelected
    )
}

@Composable
private fun M3ButtonGroupButton(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    icon: ImageVector?,
    isDark: Boolean,
    activeCornerRadius: Dp,
    inactiveCornerRadius: Dp,
    showCheckmarkOnSelected: Boolean
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val hapticFeedback = LocalHapticFeedback.current

    // Tactile press scale reduction using bouncy physics from ExpressiveLab
    val scale by animateFloatAsState(
        targetValue = if (isPressed) TactileMotionTokens.PRESS_SCALE_STANDARD else 1.0f,
        animationSpec = TactileMotionTokens.bouncySpring(),
        label = "connectedButtonScale"
    )

    // Dynamic shape morphing between active (Full Pill) and inactive (Squircle)
    // as specified in Material 3 Expressive guidelines
    val cornerRadius by animateDpAsState(
        targetValue = if (selected) activeCornerRadius else inactiveCornerRadius,
        animationSpec = TactileMotionTokens.bouncySpring(),
        label = "connectedButtonCornerAnim"
    )
    val buttonShape = RoundedCornerShape(cornerRadius)

    // Colors matching Google Material 3 Expressive Connected Button Group:
    // Active: High-contrast primary fill with onPrimary content
    // Inactive: Soft tonal pastel fill with primary/onPrimaryContainer content
    val targetContainerColor = when {
        selected -> MaterialTheme.colorScheme.primary
        isDark -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
    }
    val containerColor by animateColorAsState(
        targetValue = targetContainerColor,
        animationSpec = tween(200),
        label = "connectedButtonContainer"
    )

    val targetContentColor = when {
        selected -> MaterialTheme.colorScheme.onPrimary
        isDark -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.primary
    }
    val contentColor by animateColorAsState(
        targetValue = targetContentColor,
        animationSpec = tween(200),
        label = "connectedButtonContent"
    )

    // Clean display label: if the button has a '+' prefix, remove it when selected with checkmark
    val displayLabel = if (selected && showCheckmarkOnSelected && (label.startsWith("+ ") || label.startsWith("+"))) {
        label.removePrefix("+").trim()
    } else {
        label
    }

    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(buttonShape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary),
                onClick = {
                    hapticFeedback.performHapticFeedback(TactileMotionTokens.hapticTap)
                    onClick()
                }
            )
            .pointerHoverIcon(PointerIcon.Hand),
        shape = buttonShape,
        color = containerColor,
        shadowElevation = if (selected) 1.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (selected && showCheckmarkOnSelected) {
                // Expressive checkmark indicating active selection
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp),
                    tint = contentColor
                )
                Spacer(modifier = Modifier.width(5.dp))
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp),
                    tint = contentColor
                )
                Spacer(modifier = Modifier.width(5.dp))
            }

            Text(
                text = displayLabel,
                fontSize = 12.5.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                color = contentColor,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}
