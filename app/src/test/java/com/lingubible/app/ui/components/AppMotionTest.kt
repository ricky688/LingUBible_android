package com.lingubible.app.ui.components

import androidx.compose.animation.core.TargetBasedAnimation
import androidx.compose.animation.core.VectorConverter
import com.lingubible.app.core.theme.AppMotion
import org.junit.Assert.*
import org.junit.Test

class AppMotionTest {
    @Test fun effectSpringsKeepOpacityValidInBothDirections() {
        for ((from, to) in listOf(0f to 1f, 1f to 0f)) {
            val animation = TargetBasedAnimation(
                animationSpec = AppMotion.effects<Float>(),
                typeConverter = Float.VectorConverter,
                initialValue = from,
                targetValue = to
            )
            assertTrue(animation.durationNanos > 0L)
            for (step in 0..100) {
                val value = animation.getValueFromNanos(animation.durationNanos * step / 100)
                assertTrue("Opacity must not overshoot: $value", value in 0f..1f)
            }
            assertEquals(to, animation.getValueFromNanos(animation.durationNanos), 0f)
        }
    }
}
