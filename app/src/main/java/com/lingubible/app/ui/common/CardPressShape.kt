package com.lingubible.app.ui.common

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import com.lingubible.app.core.theme.AppMotion
import com.lingubible.app.ui.components.getGroupedItemShape

internal val LocalCardCornerRadius = staticCompositionLocalOf { 18.dp }

@Composable
internal fun rememberCardPressShape(
    interactionSource: InteractionSource,
    groupIndex: Int = 0,
    groupSize: Int = 1,
    restingCorner: Dp = LocalCardCornerRadius.current,
    uniformCorners: Boolean = true
): RoundedCornerShape {
    val isPressed by interactionSource.collectIsPressedAsState()
    val corner by animateDpAsState(
        targetValue = if (isPressed) maxOf(8.dp, restingCorner - 8.dp) else restingCorner,
        animationSpec = AppMotion.press(),
        label = "cardPressCorner"
    )
    return if (uniformCorners || groupSize <= 1) {
        RoundedCornerShape(corner)
    } else {
        getGroupedItemShape(groupIndex, groupSize, outerRadius = corner)
    }
}
