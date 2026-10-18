package com.lingubible.app

import com.lingubible.app.domain.model.*
import android.content.Context
import android.content.SharedPreferences
import com.lingubible.app.data.repository.TimetableRepository
import io.mockk.*
import org.junit.Assert.*
import org.junit.Test

class TimetableAppearanceTest {
    private val term = DEFAULT_TERMS.first()
    private val meeting = TimetableMeeting("MON", 540, 599, "09:00", "09:59", "MBG11", "LEC")
    private val section = TimetableSection("1", "1", "ACT2200", "Accounting", meetings = listOf(meeting))
    @Test fun invalidOptionsCannotHideEverythingOrCreateInvalidExports() {
        assertNull(timetableAppearanceError(TimetableAppearance(), term))
        for (options in listOf(TimetableAppearance(days = emptySet()), TimetableAppearance(fields = setOf("venue")), TimetableAppearance(startMinutes = 600, endMinutes = 600), TimetableAppearance(colors = mapOf("ACT2200" to "red")), TimetableAppearance(icsStart = "2026-99-01"), TimetableAppearance(icsStart = "2026-12-01", icsEnd = "2026-11-01"))) assertNotNull(timetableAppearanceError(options, term))
        assertEquals(1440, parseTimetableClock("24:00")); assertNull(parseTimetableClock("24:01")); assertNull(parseTimetableClock("9:00"))
    }
    @Test fun selectedFieldsAndDaysUseTheSameModelForScreenAndExport() {
        val options = TimetableAppearance(fields = setOf("code", "venue", "credits"), days = setOf("SUN", "MON"), mondayFirst = true)
        assertEquals(listOf("MON", "SUN"), options.orderedDays)
        assertEquals(listOf("ACT2200", "MBG11", "3 學分 Credits"), timetableBlockLines(section, meeting, options, 3))
        assertEquals(510 to 750, timetableMinuteRange(listOf(section), options))
        assertEquals(600 to 720, timetableMinuteRange(listOf(section), options.copy(autoRange = false, startMinutes = 600, endMinutes = 720)))
    }
    @Test fun appearanceJsonSurvivesRepositoryRecreationWithoutChangingSavedCourses() {
        val data = mutableMapOf<String, String>()
        val context = mockk<Context>(); val prefs = mockk<SharedPreferences>(); val editor = mockk<SharedPreferences.Editor>()
        every { context.getSharedPreferences("lingubible_timetable_prefs", Context.MODE_PRIVATE) } returns prefs
        every { prefs.getString(any(), any()) } answers { data[firstArg()] ?: secondArg() }
        every { prefs.getStringSet(any(), any()) } returns setOf("saved-course")
        every { prefs.edit() } returns editor
        every { editor.putString(any(), any()) } answers { data[firstArg()] = secondArg(); editor }
        every { editor.apply() } just Runs
        val custom = TimetableAppearance(title = "My timetable", colors = mapOf("ACT2200" to "#00FFFF"), fields = setOf("code", "credits"), days = setOf("MON", "WED"))
        TimetableRepository(context).saveAppearance(term.id, custom)
        val restored = TimetableRepository(context)
        assertEquals(custom, restored.getAppearance(term.id)); assertEquals(TimetableAppearance(), restored.getAppearance(DEFAULT_TERMS[1].id))
        assertEquals(setOf("saved-course"), restored.getSelectedSectionIds(term.id))
        data["appearance_${term.id}"] = "broken json"; assertEquals(TimetableAppearance(), restored.getAppearance(term.id))
        verify(exactly = 0) { editor.putStringSet(any(), any()) }
    }
    @Test fun customColorsRemainReadableAndResolutionIsBounded() {
        assertTrue(timetableUsesLightText("#DC2626")); assertFalse(timetableUsesLightText("#FFFFFF")); assertFalse(timetableUsesLightText("#FFFF00"))
        assertEquals(1200 to 700, timetableExportSize(2400, 1400, 1)); assertEquals(3600 to 2100, timetableExportSize(2400, 1400, 3))
    }
}
