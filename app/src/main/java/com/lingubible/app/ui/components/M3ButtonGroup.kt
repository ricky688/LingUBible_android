package com.lingubible.app.ui.components

import com.lingubible.app.ui.common.hapticChange

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.ripple.RippleAlpha
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
 * - Native Material 3 pressed/checked shape transitions and haptic feedback.
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
    showCheckmarkOnSelected: Boolean = true,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    equalWeight: Boolean = false
) {
    val isDark = isAppDarkTheme()
    val sources = remember(items.size) { List(items.size) { MutableInteractionSource() } }
    val pressedIndex = sources.map { it.collectIsPressedAsState().value }.indexOfFirst { it }
    val focusIndex = connectedButtonFocusIndex(selectedIndex, pressedIndex, isDark)

    Row(
        modifier = modifier.height(height),
        horizontalArrangement = if (equalWeight) {
            Arrangement.spacedBy(spacing)
        } else if (horizontalArrangement == Arrangement.End) {
            Arrangement.spacedBy(spacing, Alignment.End)
        } else {
            Arrangement.spacedBy(spacing)
        },
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEachIndexed { index, item ->
            val buttonModifier = if (equalWeight) Modifier.weight(1f) else Modifier
            M3ButtonGroupButton(
                selected = selectedIndex == index,
                focused = focusIndex == index,
                interactionSource = sources[index],
                onClick = item.onClick,
                label = item.label,
                icon = item.icon,
                isDark = isDark,
                activeCornerRadius = activeCornerRadius,
                inactiveCornerRadius = inactiveCornerRadius,
                showCheckmarkOnSelected = showCheckmarkOnSelected,
                modifier = buttonModifier,
                contentHorizontalPadding = if (equalWeight) 4.dp else 14.dp,
                equalWeight = equalWeight,
                index = index,
                count = items.size
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
 * - Supports alignment (e.g. Arrangement.End for right-aligned layouts).
 * - Native Material 3 pressed/checked shape transitions and haptic feedback.
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
    showCheckmarkOnSelected: Boolean = true,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start
) {
    val isDark = isAppDarkTheme()
    val sources = remember(items.size) { List(items.size) { MutableInteractionSource() } }
    val pressedIndex = sources.map { it.collectIsPressedAsState().value }.indexOfFirst { it }
    val focusIndex = connectedButtonFocusIndex(selectedIndex, pressedIndex, isDark)
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
    val isAlignEnd = horizontalArrangement == Arrangement.End

    Box(
        modifier = modifier.height(height),
        contentAlignment = if (isAlignEnd) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier
                .graphicsLayer { compositingStrategy = androidx.compose.ui.graphics.CompositingStrategy.Offscreen }
                .drawWithContent {
                    drawContent()
                    val fadePx = fadeWidth.toPx()

                    // Smooth leading edge fade-out transition
                    if (canScrollBackward && leftFadeAlpha > 0.01f && fadePx > 0f) {
                        drawRect(
                            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 1f - leftFadeAlpha.coerceIn(0f, 1f)),
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
                    if (canScrollForward && rightFadeAlpha > 0.01f && fadePx > 0f) {
                        drawRect(
                            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Black,
                                    Color.Black.copy(alpha = 1f - rightFadeAlpha.coerceIn(0f, 1f))
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
            horizontalArrangement = if (isAlignEnd) Arrangement.spacedBy(spacing, Alignment.End) else Arrangement.spacedBy(spacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                M3ButtonGroupButton(
                    selected = selectedIndex == index,
                    focused = focusIndex == index,
                    interactionSource = sources[index],
                    onClick = item.onClick,
                    label = item.label,
                    icon = item.icon,
                    isDark = isDark,
                    activeCornerRadius = activeCornerRadius,
                    inactiveCornerRadius = inactiveCornerRadius,
                    showCheckmarkOnSelected = showCheckmarkOnSelected,
                    index = index,
                    count = items.size
                )
            }
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
    showCheckmarkOnSelected: Boolean = true,
    equalWeight: Boolean = false
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
        showCheckmarkOnSelected = showCheckmarkOnSelected,
        equalWeight = equalWeight
    )
}

@Composable
private fun M3ButtonGroupButton(
    selected: Boolean,
    focused: Boolean,
    interactionSource: MutableInteractionSource,
    onClick: () -> Unit,
    label: String,
    icon: ImageVector?,
    isDark: Boolean,
    activeCornerRadius: Dp,
    inactiveCornerRadius: Dp,
    showCheckmarkOnSelected: Boolean,
    modifier: Modifier = Modifier,
    contentHorizontalPadding: Dp = 14.dp,
    equalWeight: Boolean = false,
    index: Int,
    count: Int
) {
    val isPressed by interactionSource.collectIsPressedAsState()
    val focus = animateFloatAsState(
        targetValue = if (focused) 1f else 0f,
        animationSpec = spring(stiffness = TactileMotionTokens.CONNECTED_COLOR_STIFFNESS),
        label = "connectedButtonPerimeterFill"
    )
    val pressWave = remember { Animatable(1f) }
    LaunchedEffect(isPressed, isDark) {
        if (isDark) {
            pressWave.snapTo(1f)
        } else {
            // A selected button can replay inward focus without clearing its selection.
            if (isPressed && selected) pressWave.snapTo(0f)
            pressWave.animateTo(1f, spring(stiffness = TactileMotionTokens.CONNECTED_COLOR_STIFFNESS))
        }
    }

    val shapes = when {
        count == 1 -> ToggleButtonDefaults.shapes()
        index == 0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
        index == count - 1 -> ButtonGroupDefaults.connectedTrailingButtonShapes()
        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
    }.copy(
        shape = RoundedCornerShape(
            topStart = if (index == 0) activeCornerRadius else inactiveCornerRadius,
            bottomStart = if (index == 0) activeCornerRadius else inactiveCornerRadius,
            topEnd = if (index == count - 1) activeCornerRadius else inactiveCornerRadius,
            bottomEnd = if (index == count - 1) activeCornerRadius else inactiveCornerRadius
        ),
        checkedShape = RoundedCornerShape(activeCornerRadius)
    )
    val pressProgress by rememberCompletedConnectedPress(interactionSource, isPressed)
    val selectionProgress by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = TactileMotionTokens.gentleSpring(),
        label = "connectedButtonSelectionShape"
    )
    val animatedShape = CompletedConnectedButtonShape(
        shapes.shape, shapes.checkedShape, shapes.pressedShape, selectionProgress, pressProgress
    )

    // Colors matching Google Material 3 Expressive Connected Button Group:
    // Active: High-contrast primary fill with onPrimary content
    // Inactive: Soft tonal pastel fill with primary/onPrimaryContainer content
    val (targetContainerColor, targetContentColor) = connectedButtonTargetColors(
        MaterialTheme.colorScheme, selected, isDark, isPressed
    )
    val containerColor by animateColorAsState(
        targetValue = if (isDark) targetContainerColor
            else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "connectedButtonContainer"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isDark) targetContentColor else MaterialTheme.colorScheme.primary,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "connectedButtonContent"
    )

    // Clean display label: if the button has a '+' prefix, remove it when selected with checkmark
    val displayLabel = if (selected && showCheckmarkOnSelected && (label.startsWith("+ ") || label.startsWith("+"))) {
        label.removePrefix("+").trim()
    } else {
        label
    }

    // Replace the touch-origin ripple while retaining native keyboard-focus and hover layers.
    val rippleConfiguration = remember {
        RippleConfiguration(rippleAlpha = RippleAlpha(
            draggedAlpha = 0f, focusedAlpha = 0.12f, hoveredAlpha = 0.08f, pressedAlpha = 0f
        ))
    }
    CompositionLocalProvider(LocalRippleConfiguration provides rippleConfiguration) {
        ToggleButton(
            checked = selected,
            onCheckedChange = hapticChange {
                onClick()
            },
            modifier = modifier.fillMaxHeight().pointerHoverIcon(PointerIcon.Hand),
            shapes = ToggleButtonShapes(animatedShape, animatedShape, animatedShape),
            colors = ToggleButtonDefaults.toggleButtonColors(
                containerColor = containerColor,
                contentColor = contentColor,
                checkedContainerColor = containerColor,
                checkedContentColor = contentColor
            ),
            contentPadding = PaddingValues(0.dp),
            interactionSource = interactionSource
        ) {
            val rowModifier = if (equalWeight) {
                Modifier.fillMaxSize()
            } else {
                Modifier.fillMaxHeight()
            }.then(
                if (isDark) Modifier else Modifier.connectedButtonPerimeterFill(
                    focus = focus,
                    focused = focused,
                    pressWave = pressWave.asState(),
                    primary = MaterialTheme.colorScheme.primary,
                    highlight = MaterialTheme.colorScheme.onPrimary
                )
            ).padding(horizontal = contentHorizontalPadding)

            Row(
                modifier = rowModifier,
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
                    Spacer(modifier = Modifier.width(if (equalWeight) 3.dp else 5.dp))
                } else if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = contentColor
                    )
                    Spacer(modifier = Modifier.width(if (equalWeight) 3.dp else 5.dp))
                }

                Text(
                    text = displayLabel,
                    fontSize = if (equalWeight) 12.sp else 12.5.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                    color = contentColor,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

internal fun connectedButtonFocusIndex(selectedIndex: Int, pressedIndex: Int, isDark: Boolean): Int =
    if (isDark || pressedIndex < 0) selectedIndex else pressedIndex

internal fun connectedButtonTargetColors(
    scheme: ColorScheme,
    selected: Boolean,
    isDark: Boolean,
    isPressed: Boolean
): Pair<Color, Color> {
    val container = when {
        selected || (!isDark && isPressed) -> scheme.primary
        isDark -> scheme.primaryContainer.copy(alpha = 0.5f)
        else -> scheme.primaryContainer.copy(alpha = 0.35f)
    }
    val content = when {
        selected || (!isDark && isPressed) -> scheme.onPrimary
        isDark -> scheme.onPrimaryContainer
        else -> scheme.primary
    }
    return container to content
}
