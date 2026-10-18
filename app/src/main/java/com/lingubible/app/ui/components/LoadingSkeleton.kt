package com.lingubible.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive

@Composable
internal fun rememberSkeletonAlpha(loading: Boolean): State<Float> {
    val alpha = remember { Animatable(0.7f) }
    LaunchedEffect(loading) {
        if (loading) animateSkeletonPulse(alpha)
    }
    return alpha.asState()
}

internal suspend fun animateSkeletonPulse(alpha: Animatable<Float, AnimationVector1D>) {
    val motionScale = currentCoroutineContext()[MotionDurationScale]
    snapshotFlow { motionScale?.scaleFactor ?: 1f }.collectLatest { scale ->
        // A zero-duration spring loop would otherwise spin when system animations are off.
        if (scale == 0f) {
            alpha.snapTo(0.7f)
        } else {
            while (currentCoroutineContext().isActive) {
                alpha.animateTo(0.95f, spring(stiffness = Spring.StiffnessVeryLow))
                alpha.animateTo(0.55f, spring(stiffness = Spring.StiffnessVeryLow))
            }
        }
    }
}

internal fun Modifier.contentSkeleton(loading: Boolean, color: Color, alpha: State<Float>): Modifier =
    if (!loading) this else clearAndSetSemantics {}.drawWithContent {
        drawRoundRect(
            color = color,
            topLeft = Offset(0f, size.height * 0.18f),
            size = Size(size.width, size.height * 0.64f),
            cornerRadius = CornerRadius(6.dp.toPx()),
            alpha = alpha.value
        )
    }
