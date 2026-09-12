package com.lingubible.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
 * Material 3 Expressive Split Button component.
 *
 * Implements Google Material Design 3 Expressive Split Button specifications:
 * - Dual connected interactive surfaces with complementary asymmetric shapes:
 *   Leading: Outer corner 20.dp, inner adjacent corner 4.dp
 *   Trailing: Inner adjacent corner 4.dp, outer corner 20.dp
 * - Spring stiffness physics for touch/press feedback (strictly no damping override)
 * - Mouse cursor hand indicator (PointerIcon.Hand)
 * - Cohesive 38dp height matching standard Material 3 OutlinedButton
 */
@Composable
fun M3SplitButton(
    selectedIndex: Int,
    onLeadingClick: () -> Unit,
    onTrailingClick: () -> Unit,
    leadingText: String,
    trailingText: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    height: Dp = 38.dp
) {
    val isDark = isAppDarkTheme()
    val borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.35f else 0.70f)

    // Shapes per Google M3 Expressive Split Button spec
    val leadingShape = RoundedCornerShape(
        topStart = 20.dp,
        bottomStart = 20.dp,
        topEnd = 4.dp,
        bottomEnd = 4.dp
    )
    val trailingShape = RoundedCornerShape(
        topStart = 4.dp,
        bottomStart = 4.dp,
        topEnd = 20.dp,
        bottomEnd = 20.dp
    )

    Row(
        modifier = modifier.height(height),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Leading Segment
        M3SplitButtonSegment(
            selected = selectedIndex == 0,
            onClick = onLeadingClick,
            text = leadingText,
            icon = leadingIcon,
            shape = leadingShape,
            isDark = isDark,
            borderColor = borderColor
        )

        Spacer(modifier = Modifier.width(2.dp))

        // Trailing Segment
        M3SplitButtonSegment(
            selected = selectedIndex == 1,
            onClick = onTrailingClick,
            text = trailingText,
            icon = trailingIcon,
            shape = trailingShape,
            isDark = isDark,
            borderColor = borderColor
        )
    }
}

@Composable
private fun M3SplitButtonSegment(
    selected: Boolean,
    onClick: () -> Unit,
    text: String,
    icon: ImageVector?,
    shape: RoundedCornerShape,
    isDark: Boolean,
    borderColor: Color
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "splitButtonScale"
    )

    val targetContainerColor = when {
        selected -> MaterialTheme.colorScheme.primary
        isDark -> MaterialTheme.colorScheme.surfaceContainerHigh
        else -> MaterialTheme.colorScheme.surfaceContainerHighest
    }
    val containerColor by animateColorAsState(
        targetValue = targetContainerColor,
        animationSpec = tween(180),
        label = "splitButtonContainer"
    )

    val targetContentColor = when {
        selected -> MaterialTheme.colorScheme.onPrimary
        isDark -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurface
    }
    val contentColor by animateColorAsState(
        targetValue = targetContentColor,
        animationSpec = tween(180),
        label = "splitButtonContent"
    )

    Surface(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary),
                onClick = onClick
            )
            .pointerHoverIcon(PointerIcon.Hand),
        shape = shape,
        color = containerColor,
        border = if (selected) null else BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp),
                    tint = contentColor
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}
