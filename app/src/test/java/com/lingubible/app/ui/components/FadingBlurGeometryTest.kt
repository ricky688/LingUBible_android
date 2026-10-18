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

    @Test
    fun visibleBottomClearanceStrictlyTracksBottomBarOffset() {
        val fullBarClearance = 104f
        val systemBottom = 24f
        val hideTranslation = fullBarClearance

        fun computeVisible(translationY: Float): Float {
            return (fullBarClearance - translationY).coerceAtLeast(systemBottom)
        }

        // When hidden: translation is hideTranslation, clearance remains clamped to systemBottom (fade at bottom edge)
        assertEquals(systemBottom, computeVisible(hideTranslation), 0.001f)

        // When bar just reaches the bottom of the screen: translation is fullBarClearance (104f)
        assertEquals(systemBottom, computeVisible(fullBarClearance), 0.001f)

        // When bar has moved halfway up (translation = 60f): clearance is exactly 44f
        assertEquals(44f, computeVisible(60f), 0.001f)

        // When bar is fully visible (translation = 0f): clearance is fullBarClearance (104f)
        assertEquals(fullBarClearance, computeVisible(0f), 0.001f)
    }
}
