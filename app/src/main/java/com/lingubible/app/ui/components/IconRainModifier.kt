package com.lingubible.app.ui.components

import android.animation.ValueAnimator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.lingubible.app.R
import kotlin.random.Random

private data class FallingIcon(
    val x: Float,
    val delay: Float,
    val size: Float,
    val drift: Float,
    val rotation: Float
)

/** A bounded, touch-through burst drawn above the app, without a persistent animation loop. */
fun Modifier.iconRain(trigger: Int): Modifier = composed {
    val painter = painterResource(R.drawable.ic_lingubible_logo)
    val progress = remember { Animatable(1f) }
    val icons = remember(trigger) {
        if (trigger == 0) emptyList() else List(36) {
            FallingIcon(
                x = Random.nextFloat(),
                delay = Random.nextFloat() * 0.28f,
                size = 22f + Random.nextFloat() * 24f,
                drift = Random.nextFloat() * 80f - 40f,
                rotation = Random.nextFloat() * 540f - 270f
            )
        }
    }

    LaunchedEffect(trigger) {
        if (trigger != 0 && ValueAnimator.areAnimatorsEnabled()) {
            progress.snapTo(0f)
            progress.animateTo(1f, spring(stiffness = 12f))
        } else {
            progress.snapTo(1f)
        }
    }

    drawWithContent {
        drawContent()
        val frame = progress.value
        if (frame < 1f) {
            icons.forEach { icon ->
                val iconSize = icon.size.dp.toPx()
                val y = iconRainY(frame, icon.delay, size.height, iconSize)
                if (y + iconSize > 0f && y < size.height) {
                    val x = icon.x * (size.width - iconSize).coerceAtLeast(0f) + icon.drift.dp.toPx() * frame
                    withTransform({
                        translate(x, y)
                        rotate(icon.rotation * frame, Offset(iconSize / 2, iconSize / 2))
                    }) {
                        with(painter) { draw(Size(iconSize, iconSize)) }
                    }
                }
            }
        }
    }
}

internal fun iconRainY(progress: Float, delay: Float, height: Float, iconSize: Float): Float {
    val fall = ((progress - delay) / (1f - delay)).coerceIn(0f, 1f)
    return -2f * iconSize + (height + 4f * iconSize) * LinearOutSlowInEasing.transform(fall)
}
