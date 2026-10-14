package com.lingubible.app.ui.components

import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CatFootstepsTest {

    @Test
    fun zeroOrNegativeDimensionsReturnEmptySteps() {
        assertTrue(CatPathGenerator.generateSteps(0f, 1000f).isEmpty())
        assertTrue(CatPathGenerator.generateSteps(500f, 0f).isEmpty())
        assertTrue(CatPathGenerator.generateSteps(-100f, -100f).isEmpty())
    }

    @Test
    fun generateStepsReturnsRealisticCountAndPacing() {
        val width = 1080f
        val height = 2400f
        val startTime = 10_000L
        val interval = 380L

        val steps = CatPathGenerator.generateSteps(
            width = width,
            height = height,
            startTime = startTime,
            stepIntervalMs = interval
        )

        assertTrue(steps.size in 8..16)

        // Alternates left and right paws
        for (i in steps.indices) {
            assertEquals(i % 2 == 0, steps[i].isLeftPaw)
            assertEquals(startTime + i * interval, steps[i].spawnTime)
        }
    }

    @Test
    fun fiveTapsWithinWindowTriggersWalk() {
        val state = CatWalkState()
        var hapticFired = 0
        val onHaptic: (HapticFeedbackType) -> Unit = { hapticFired++ }

        // 4 taps should not trigger walk
        for (i in 1..4) {
            state.onLogoTap(1080f, 2400f, onHaptic)
            assertEquals(i, state.tapCount)
            assertTrue(state.steps.isEmpty())
        }
        assertEquals(4, hapticFired)

        // 5th tap triggers walk
        state.onLogoTap(1080f, 2400f, onHaptic)
        assertEquals(0, state.tapCount) // Resets on trigger
        assertEquals(5, hapticFired)
        assertFalse(state.steps.isEmpty())
    }

    @Test
    fun tapCounterResetsIfTimeExceedsWindow() {
        val state = CatWalkState()
        val onHaptic: (HapticFeedbackType) -> Unit = {}

        val t0 = 10_000L
        state.onLogoTap(1080f, 2400f, now = t0, onHaptic = onHaptic)
        assertEquals(1, state.tapCount)

        // 3 seconds later (exceeding 2.5s window)
        state.onLogoTap(1080f, 2400f, now = t0 + 3000L, onHaptic = onHaptic)
        assertEquals(1, state.tapCount)
        assertTrue(state.steps.isEmpty())
    }

    @Test
    fun tappingActivePawSpawnsParticlesAndMarksTapped() {
        val state = CatWalkState()
        state.triggerWalk(1080f, 2400f)
        assertFalse(state.steps.isEmpty())

        val firstStep = state.steps.first()
        assertFalse(firstStep.isTapped)
        assertTrue(state.particles.isEmpty())

        var hapticTriggered = false
        state.onPawTapped(firstStep) { hapticTriggered = true }

        assertTrue(firstStep.isTapped)
        assertTrue(hapticTriggered)
        assertEquals(7, state.particles.size)

        // Tapping again should be a no-op
        hapticTriggered = false
        state.onPawTapped(firstStep) { hapticTriggered = true }
        assertFalse(hapticTriggered)
        assertEquals(7, state.particles.size)
    }

    @Test
    fun pruneRemovesOldStepsAndParticles() {
        val state = CatWalkState()
        val now = 100_000L

        val step1 = CatStep(id = 1L, x = 100f, y = 100f, angle = 0f, isLeftPaw = true, spawnTime = now - 5000L)
        val step2 = CatStep(id = 2L, x = 150f, y = 150f, angle = 0f, isLeftPaw = false, spawnTime = now - 2000L)
        state.steps.addAll(listOf(step1, step2))

        val particle1 = CatParticle(id = 1L, startX = 100f, startY = 100f, vx = 0f, vy = 0f, symbol = "♥", color = androidx.compose.ui.graphics.Color.Red, spawnTime = now - 1000L)
        val particle2 = CatParticle(id = 2L, startX = 150f, startY = 150f, vx = 0f, vy = 0f, symbol = "★", color = androidx.compose.ui.graphics.Color.Yellow, spawnTime = now - 200L)
        state.particles.addAll(listOf(particle1, particle2))

        state.prune(now)

        // step1 (> 4200ms) removed, step2 kept
        assertEquals(1, state.steps.size)
        assertEquals(2L, state.steps.first().id)

        // particle1 (> 750ms) removed, particle2 kept
        assertEquals(1, state.particles.size)
        assertEquals(2L, state.particles.first().id)
    }
}
