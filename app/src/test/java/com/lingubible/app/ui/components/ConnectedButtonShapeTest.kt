package com.lingubible.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.TargetBasedAnimation
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.MonotonicFrameClock
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.lingubible.app.core.theme.TactileMotionTokens
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConnectedButtonShapeTest {
    private class StepClock(private val timeMillis: () -> Long) : MonotonicFrameClock {
        override suspend fun <R> withFrameNanos(onFrame: (Long) -> R): R {
            delay(16)
            return onFrame(timeMillis() * 1_000_000)
        }
    }

    private suspend fun TestScope.measurePress(holdMillis: Long): Long {
        val source = MutableInteractionSource()
        val progress = Animatable(0f)
        val held = mutableStateOf(false)
        val samples = mutableListOf<Pair<Long, Float>>()
        val start = testScheduler.currentTime
        val clock = StepClock { testScheduler.currentTime }
        val animation = backgroundScope.launch(clock) {
            animateCompletedConnectedPress(source, progress) { held.value }
        }
        val observation = backgroundScope.launch {
            snapshotFlow { progress.value }.collect { samples += (testScheduler.currentTime - start) to it }
        }
        runCurrent()
        val press = PressInteraction.Press(Offset.Zero)
        held.value = true
        source.emit(press)
        runCurrent()
        advanceTimeBy(holdMillis)
        runCurrent()
        if (holdMillis >= 900) assertEquals(1f, progress.value, 0f)
        held.value = false
        source.emit(PressInteraction.Release(press))
        runCurrent()
        if (holdMillis == 100L) assertEquals(1f, progress.targetValue, 0f)
        advanceTimeBy(1500)
        runCurrent()
        assertEquals(0f, progress.value, 0f)
        assertTrue(samples.any { it.second == 1f })
        animation.cancel()
        observation.cancel()
        return samples.first { it.second == 1f }.first
    }

    @Test
    fun quickTapAndHeldPressCompleteTheSameInwardAnimation() = runTest {
        val quickPeak = measurePress(100)
        val heldPeak = measurePress(900)
        assertEquals(quickPeak, heldPeak)
        assertTrue(quickPeak > 100)
    }

    @Test
    fun rapidPressesFinishWithoutAQueueOfStaleAnimations() = runTest {
        val source = MutableInteractionSource()
        val progress = Animatable(0f)
        val held = mutableStateOf(false)
        val animation = backgroundScope.launch(StepClock { testScheduler.currentTime }) {
            animateCompletedConnectedPress(source, progress) { held.value }
        }
        runCurrent()
        repeat(5) {
            val press = PressInteraction.Press(Offset.Zero)
            held.value = true
            source.emit(press)
            runCurrent()
            advanceTimeBy(40)
            held.value = false
            source.emit(PressInteraction.Release(press))
            advanceTimeBy(20)
        }
        advanceTimeBy(1200)
        runCurrent()
        assertEquals(0f, progress.value, 0f)
        assertEquals(0f, progress.targetValue, 0f)
        animation.cancel()
    }

    @Test
    fun shapeReachesTheNativePressedOutlineAndReturnsToTheCurrentSelection() {
        val unchecked = RoundedCornerShape(topStart = 20.dp, topEnd = 8.dp, bottomEnd = 8.dp, bottomStart = 20.dp)
        val checked = RoundedCornerShape(20.dp)
        val pressed = RoundedCornerShape(8.dp)
        val size = Size(140f, 40f)
        val density = Density(1f)
        for (direction in listOf(LayoutDirection.Ltr, LayoutDirection.Rtl)) {
            for (selection in listOf(0f, 1f)) {
                val atRest = CompletedConnectedButtonShape(unchecked, checked, pressed, selection, 0f)
                val atPress = CompletedConnectedButtonShape(unchecked, checked, pressed, selection, 1f)
                val rest = if (selection == 1f) checked else unchecked
                assertEquals((rest.createOutline(size, direction, density) as Outline.Rounded).roundRect,
                    (atRest.createOutline(size, direction, density) as Outline.Rounded).roundRect)
                assertEquals((pressed.createOutline(size, direction, density) as Outline.Rounded).roundRect,
                    (atPress.createOutline(size, direction, density) as Outline.Rounded).roundRect)
            }
        }
    }

    @Test
    fun collapsedHeaderCanHaveAZeroSizedOutline() {
        val shape = RoundedCornerShape(8.dp)
        val animated = CompletedConnectedButtonShape(shape, shape, shape, 1f, 0.5f)
        val outline = animated.createOutline(Size.Zero, LayoutDirection.Ltr, Density(1f)) as Outline.Rounded
        assertEquals(0f, outline.roundRect.width, 0f)
        assertEquals(0f, outline.roundRect.height, 0f)
    }

    @Test
    fun lightColorSpringTakesAboutHalfThePreviousDuration() {
        fun duration(stiffness: Float) = TargetBasedAnimation(
            animationSpec = spring<Float>(stiffness = stiffness),
            typeConverter = Float.VectorConverter,
            initialValue = 0f,
            targetValue = 1f
        ).durationNanos
        assertTrue(duration(TactileMotionTokens.CONNECTED_COLOR_STIFFNESS) < duration(Spring.StiffnessLow) * 0.6)
    }
}
