package com.lingubible.app.ui.components

import com.lingubible.app.core.settings.AppColorScheme
import com.lingubible.app.core.theme.colorSchemeFor
import org.junit.Assert.*
import org.junit.Test

class ConnectedButtonColorsTest {
    private val palettes = listOf(AppColorScheme.LINGNAN_RED, AppColorScheme.GOOGLE_BLUE, AppColorScheme.EMERALD_GREEN)

    @Test
    fun lightPressTransfersVisualFocusBeforeSelectionCommits() {
        assertEquals(1, connectedButtonFocusIndex(selectedIndex = 0, pressedIndex = 1, isDark = false))
        assertEquals(1, connectedButtonFocusIndex(selectedIndex = 1, pressedIndex = -1, isDark = false))
    }

    @Test
    fun canceledPressRestoresVisualFocusToTheSelectedButton() {
        assertEquals(1, connectedButtonFocusIndex(0, 1, false))
        assertEquals(0, connectedButtonFocusIndex(0, -1, false))
    }

    @Test
    fun darkPressLeavesFocusOnTheCommittedSelection() {
        assertEquals(0, connectedButtonFocusIndex(0, 1, true))
    }

    @Test
    fun lightPressUsesPrimaryFocusColorsAndKeepsTheLabelDistinct() {
        for (palette in palettes) {
            val scheme = colorSchemeFor(palette, isDark = false)
            for (selected in listOf(false, true)) {
                val pressed = connectedButtonTargetColors(scheme, selected, isDark = false, isPressed = true)
                assertEquals(scheme.primary to scheme.onPrimary, pressed)
                assertNotEquals(pressed.first, pressed.second)
                assertEquals(1f, pressed.first.alpha, 0f)
                val released = connectedButtonTargetColors(scheme, selected, isDark = false, isPressed = false)
                assertNotEquals(released.first, released.second)
            }
        }
    }

    @Test
    fun releaseReturnsToSelectedPaletteColors() {
        val scheme = colorSchemeFor(AppColorScheme.GOOGLE_BLUE, isDark = false)
        assertEquals(scheme.primary to scheme.onPrimary, connectedButtonTargetColors(scheme, true, false, false))
        assertEquals(scheme.primaryContainer.copy(alpha = 0.35f) to scheme.primary, connectedButtonTargetColors(scheme, false, false, false))
    }

    @Test
    fun darkModeNeverConverges() {
        for (palette in palettes) {
            val scheme = colorSchemeFor(palette, isDark = true)
            for (selected in listOf(false, true)) {
                assertEquals(
                    connectedButtonTargetColors(scheme, selected, true, false),
                    connectedButtonTargetColors(scheme, selected, true, true)
                )
            }
        }
    }
}
