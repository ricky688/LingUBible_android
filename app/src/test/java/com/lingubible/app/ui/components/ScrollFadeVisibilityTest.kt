package com.lingubible.app.ui.components

import org.junit.Assert.*
import org.junit.Test

class ScrollFadeVisibilityTest {
    @Test fun middleKeepsBothFadesWithoutDependingOnScrolling() {
        assertEquals(ScrollEdgeVisibility(true, true), scrollEdgeVisibility(true, true))
    }
    @Test fun boundariesShowOnlyTheEdgeWithRemainingContent() {
        assertEquals(ScrollEdgeVisibility(false, true), scrollEdgeVisibility(false, true))
        assertEquals(ScrollEdgeVisibility(true, false), scrollEdgeVisibility(true, false))
        assertEquals(ScrollEdgeVisibility(false, false), scrollEdgeVisibility(false, false))
    }
    @Test fun reachedEdgesClearImmediatelyAndSpringOvershootCannotDarkenContent() {
        assertEquals(0f, scrollEdgeAlpha(1f, false), 0f)
        assertEquals(0f, scrollEdgeAlpha(-.2f, true), 0f)
        assertEquals(1f, scrollEdgeAlpha(1.2f, true), 0f)
        assertEquals(.5f, scrollEdgeAlpha(.5f, true), 0f)
    }
}
