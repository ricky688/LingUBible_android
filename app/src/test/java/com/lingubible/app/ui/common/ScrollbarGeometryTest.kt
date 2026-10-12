package com.lingubible.app.ui.common

import org.junit.Assert.assertEquals
import org.junit.Test

class ScrollbarGeometryTest {
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
