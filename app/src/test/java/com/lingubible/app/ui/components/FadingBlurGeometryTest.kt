package com.lingubible.app.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

class FadingBlurGeometryTest {
    @Test
    fun fadeEndsAtViewportEdgeWithoutNavigationInset() {
        assertEquals(78f to 100f, bottomFadeRange(100f, 0f, 22f))
    }

    @Test
    fun fadeEndsAboveOverlaidNavigation() {
        assertEquals(38f to 60f, bottomFadeRange(100f, 40f, 22f))
    }

    @Test
    fun fadeClampsToShortVisibleViewport() {
        assertEquals(0f to 15f, bottomFadeRange(20f, 5f, 34f))
        assertEquals(0f to 0f, bottomFadeRange(20f, 40f, 22f))
    }
}
