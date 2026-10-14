package com.lingubible.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.lingubible.app.core.theme.TactileMotionTokens
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
internal fun rememberCompletedConnectedPress(source: InteractionSource, isPressed: Boolean): State<Float> {
    val progress = remember(source) { Animatable(0f) }
    val held = rememberUpdatedState(isPressed)
    LaunchedEffect(source) {
        animateCompletedConnectedPress(source, progress) { held.value }
    }
    return progress.asState()
}

internal suspend fun animateCompletedConnectedPress(
    source: InteractionSource,
    progress: Animatable<Float, AnimationVector1D>,
    isPressed: () -> Boolean
) = coroutineScope {
    var animation: Job? = null
    source.interactions.collect { interaction ->
        if (interaction is PressInteraction.Press &&
            (animation?.isActive != true || progress.targetValue != 1f)
        ) {
            animation?.cancel()
            animation = launch {
                // Releasing early must not retarget the inward shape animation.
                progress.animateTo(1f, TactileMotionTokens.gentleSpring())
                snapshotFlow { isPressed() }.first { !it }
                progress.animateTo(0f, TactileMotionTokens.gentleSpring())
            }
        }
    }
}

// A non-RoundedCornerShape lets ToggleButton use this completed animation without retargeting it.
internal class CompletedConnectedButtonShape(
    private val unchecked: Shape,
    private val checked: Shape,
    private val pressed: Shape,
    private val selectionProgress: Float,
    private val pressProgress: Float
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        fun Shape.roundRect(): RoundRect = when (val outline = createOutline(size, layoutDirection, density)) {
            is Outline.Rounded -> outline.roundRect
            is Outline.Rectangle -> RoundRect(outline.rect, CornerRadius.Zero)
            is Outline.Generic -> error("Connected buttons require rounded or rectangular outlines")
        }
        return Outline.Rounded(connectedButtonRoundRect(
            unchecked.roundRect(), checked.roundRect(), pressed.roundRect(), selectionProgress, pressProgress
        ))
    }
}

internal fun connectedButtonRoundRect(
    unchecked: RoundRect, checked: RoundRect, pressed: RoundRect, selection: Float, press: Float
): RoundRect = lerp(lerp(unchecked, checked, selection.coerceIn(0f, 1f)), pressed, press.coerceIn(0f, 1f))
