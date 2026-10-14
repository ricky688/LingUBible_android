package com.lingubible.app.ui.common

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

internal val LocalCardCornerRadius = staticCompositionLocalOf { 28.dp }

@Composable
internal fun rememberCardPressShape(interactionSource: InteractionSource): RoundedCornerShape {
    val isPressed by interactionSource.collectIsPressedAsState()
    val restingCorner = LocalCardCornerRadius.current
    val corner by animateDpAsState(
        targetValue = if (isPressed) maxOf(8.dp, restingCorner - 12.dp) else restingCorner,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "cardPressCorner"
    )
    return RoundedCornerShape(corner)
}
