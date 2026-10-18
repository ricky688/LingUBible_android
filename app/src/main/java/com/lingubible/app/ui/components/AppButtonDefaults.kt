package com.lingubible.app.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lingubible.app.core.theme.TactileMotionTokens

/** Shared press feedback: morph the outline without shrinking the button or its content. */
object AppButtonDefaults {
    val PressedCornerRadius = 8.dp

    @Composable
    fun shapes(shape: Shape = ButtonDefaults.shape) = ButtonDefaults.shapes(
        shape = shape,
        pressedShape = RoundedCornerShape(PressedCornerRadius)
    )

    @Composable
    fun iconShapes(shape: Shape = IconButtonDefaults.standardShape) = IconButtonDefaults.shapes(
        shape = shape,
        pressedShape = RoundedCornerShape(PressedCornerRadius)
    )

    @Composable
    fun pressCorner(source: InteractionSource, restingCorner: Dp): State<Dp> {
        val pressed by source.collectIsPressedAsState()
        return animateDpAsState(
            targetValue = if (pressed) PressedCornerRadius else restingCorner,
            animationSpec = TactileMotionTokens.gentleSpring(),
            label = "buttonPressCorner"
        )
    }
}
