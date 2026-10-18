package com.lingubible.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.MonotonicFrameClock
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.MotionDurationScale
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StatsSkeletonPulseTest {
    @Test fun pulseMovesGentlyAndSuspendsWhenSystemAnimationsAreDisabled() = runTest {
        val scale = mutableFloatStateOf(1f)
        val motion = object : MotionDurationScale {
            override val scaleFactor get() = scale.floatValue
        }
        var frames = 0
        val clock = object : MonotonicFrameClock {
            override suspend fun <R> withFrameNanos(onFrame: (Long) -> R): R {
                delay(16); frames++; Snapshot.sendApplyNotifications()
                return onFrame(testScheduler.currentTime * 1_000_000)
            }
        }
        val alpha = Animatable(0.7f)
        val job = backgroundScope.launch(clock + motion) { animateSkeletonPulse(alpha) }
        runCurrent(); advanceTimeBy(400); runCurrent()
        assertTrue(alpha.value > 0.7f && alpha.value <= 0.95f)
        advanceTimeBy(1500); runCurrent()
        assertTrue(alpha.value in 0.55f..0.95f)
        scale.floatValue = 0f; Snapshot.sendApplyNotifications(); runCurrent()
        assertEquals(0.7f, alpha.value, 0f)
        val stoppedFrames = frames
        advanceTimeBy(2000); runCurrent()
        assertEquals(stoppedFrames, frames)
        scale.floatValue = 1f; Snapshot.sendApplyNotifications(); runCurrent()
        advanceTimeBy(400); runCurrent()
        assertTrue(alpha.value > 0.7f)
        job.cancel()
    }
}
