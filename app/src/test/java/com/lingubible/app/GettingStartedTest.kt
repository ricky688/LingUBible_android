package com.lingubible.app

import android.content.Context
import android.content.SharedPreferences
import com.lingubible.app.core.navigation.Screen
import com.lingubible.app.core.settings.AppSettingsManager
import com.lingubible.app.ui.components.GETTING_STARTED_STEPS
import com.lingubible.app.ui.components.shouldShowGettingStarted
import io.mockk.*
import org.junit.Assert.*
import org.junit.Test

class GettingStartedTest {
    @Test fun guideDismissalSurvivesRestartAndAppearanceResetWithoutChangingOtherPreferences() {
        val values = mutableMapOf<String, Any>("app_language" to "en", "oled_black" to false)
        val context = mockk<Context>(); val prefs = mockk<SharedPreferences>(); val editor = mockk<SharedPreferences.Editor>()
        every { context.getSharedPreferences("app_settings", any()) } returns prefs
        every { prefs.getString(any(), any()) } answers { values[firstArg()] as? String ?: secondArg() }
        every { prefs.getBoolean(any(), any()) } answers { values[firstArg()] as? Boolean ?: secondArg() }
        every { prefs.getFloat(any(), any()) } answers { values[firstArg()] as? Float ?: secondArg() }
        every { prefs.contains(any()) } answers { values.containsKey(firstArg()) }
        every { prefs.edit() } returns editor
        every { editor.putBoolean(any(), any()) } answers { values[firstArg()] = secondArg<Boolean>(); editor }
        every { editor.putString(any(), any()) } answers { values[firstArg()] = secondArg<String>(); editor }
        every { editor.putFloat(any(), any()) } answers { values[firstArg()] = secondArg<Float>(); editor }
        every { editor.remove(any()) } answers { values.remove(firstArg<String>()); editor }
        every { editor.apply() } just Runs
        val first = AppSettingsManager(context)
        assertFalse(first.hasSeenGuide.value)
        first.markGuideSeen(); assertTrue(first.hasSeenGuide.value)
        val restored = AppSettingsManager(context)
        assertTrue(restored.hasSeenGuide.value)
        restored.resetColorSchemeDefaults()
        assertTrue(AppSettingsManager(context).hasSeenGuide.value)
        assertEquals("en", values["app_language"]); assertEquals(false, values["oled_black"])
    }
    @Test fun firstLaunchIsLimitedToHomeAndDoesNotInterruptKeyboardOrOtherDestinations() {
        assertTrue(shouldShowGettingStarted(false, true, false))
        assertFalse(shouldShowGettingStarted(true, true, false))
        assertFalse(shouldShowGettingStarted(false, false, false))
        assertFalse(shouldShowGettingStarted(false, true, true))
    }
    @Test fun sevenWebTopicsHaveBilingualNativeInstructionsAndLinks() {
        assertEquals(listOf("welcome", "reviews", "materials", "planner", "gpa", "calendar", "contribute"), GETTING_STARTED_STEPS.map { it.id })
        GETTING_STARTED_STEPS.forEach { step ->
            assertTrue(step.title.zh.isNotBlank() && step.title.en.isNotBlank())
            assertTrue(step.bullets.isNotEmpty() && step.bullets.all { it.zh.isNotBlank() && it.en.isNotBlank() })
            assertTrue(step.links.isNotEmpty())
        }
        assertEquals(Screen.AcademicTools(0), GETTING_STARTED_STEPS.first { it.id == "planner" }.links.single().destination)
        assertEquals(Screen.AcademicTools(2), GETTING_STARTED_STEPS.first { it.id == "gpa" }.links.single().destination)
        assertEquals(Screen.AcademicTools(1), GETTING_STARTED_STEPS.first { it.id == "calendar" }.links.single().destination)
        assertEquals("Welcome to LingUBible", GETTING_STARTED_STEPS.first().title.primary(false))
        assertEquals("認識 LingUBible", GETTING_STARTED_STEPS.first().title.primary(true))
    }
}
