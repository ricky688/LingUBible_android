package com.lingubible.app.ui.common

import org.junit.Assert.assertEquals
import org.junit.Test

class ScrollbarGeometryTest {
    @Test
    fun scrollingWithinOneTallSectionAdvancesTheThumb() {
        val before = scrollbarProgress(4.1f, 4.4f, 12)
        val after = scrollbarProgress(4.3f, 4.6f, 12)
        org.junit.Assert.assertTrue(after > before)
    }

    @Test
    fun fractionalViewportReachesBothEnds() {
        assertEquals(0f, scrollbarProgress(0f, 1.5f, 12), 0f)
        assertEquals(1f, scrollbarProgress(10.5f, 12f, 12), 0f)
        assertEquals(0f, scrollbarProgress(0f, 0f, 0), 0f)
    }

    @Test
    fun partialRowsDoNotCountAsFullyVisible() {
        assertEquals(0.5f, scrollbarProgress(4.5f, 7.5f, 12), 0f)
        // A pinned row at index 4 must not replace the actual first row at 8.
        assertEquals(0.8f, scrollbarProgress(8f, 10f, 12), 0f)
    }

    @Test
    fun zeroViewportDuringRotationHasNoThumb() {
        assertEquals(0f, scrollbarThumbHeight(0f, 0.5f, 40.5f), 0f)
    }

    @Test
    fun shortViewportContainsThePreferredMinimumThumb() {
        assertEquals(20f, scrollbarThumbHeight(20f, 0.1f, 40.5f), 0f)
    }

    @Test
    fun regularViewportPreservesProportionalAndMinimumSizes() {
        assertEquals(250f, scrollbarThumbHeight(1000f, 0.25f, 40.5f), 0f)
        assertEquals(40.5f, scrollbarThumbHeight(1000f, 0.01f, 40.5f), 0f)
    }
}
