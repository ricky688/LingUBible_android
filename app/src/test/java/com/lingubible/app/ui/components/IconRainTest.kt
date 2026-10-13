package com.lingubible.app.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IconRainTest {
    @Test
    fun iconsStartOutsideScreenAndWaitForTheirStagger() {
        assertEquals(-64f, iconRainY(0f, 0.2f, 1000f, 32f), 0.01f)
        assertEquals(-64f, iconRainY(0.19f, 0.2f, 1000f, 32f), 0.01f)
    }

    @Test
    fun entireIconLeavesScreenAtEndAcrossWindowSizes() {
        for (height in listOf(240f, 1000f, 2400f)) {
            for (delay in listOf(0f, 0.14f, 0.28f)) {
                assertTrue(iconRainY(1f, delay, height, 46f) > height)
            }
        }
    }

    @Test
    fun fallNeverReversesOrReentersAfterCompletion() {
        val positions = (0..110).map { iconRainY(it / 100f, 0.28f, 1000f, 32f) }
        assertTrue(positions.zipWithNext().all { (before, after) -> after >= before })
        assertEquals(positions[100], positions[110], 0f)
    }

    @Test
    fun fallTravelsLessDuringEachEqualProgressInterval() {
        val positions = (0..4).map { iconRainY(0.2f + it * 0.2f, 0.2f, 1000f, 32f) }
        val distances = positions.zipWithNext { before, after -> after - before }
        assertTrue(distances.zipWithNext().all { (earlier, later) -> earlier > later })
    }
}
