package com.lingubible.app

import com.lingubible.app.core.util.IcsExporter
import com.lingubible.app.domain.model.TimetableMeeting
import com.lingubible.app.domain.model.TimetableSection
import com.lingubible.app.domain.model.TimetableTerm
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant
import java.util.TimeZone

class TimetableExportTest {
    private val term = TimetableTerm("test", "2026–27 Term 1", "2627-T1", "", "2026-09-01", "2026-11-30")
    private val tuesday = TimetableMeeting("TUE", 1020, 1109, "17:00", "18:29", "LKK101", "LEC")
    private val thursday = TimetableMeeting("THU", 750, 839, "12:30", "13:59", "LKK101", "TUT")
    private val section = TimetableSection("6", "6", "ACT3300", "Company Law", meetings = listOf(tuesday, thursday))

    @Test fun customDateRangeSkipsMeetingsThatStartAfterItsEnd() {
        val monday = TimetableMeeting("MON", 540, 599, "09:00", "09:59")
        val section = TimetableSection("test", "test", "ACT2200", "Accounting", meetings = listOf(monday))
        val calendar = IcsExporter.buildTimetableIcs(listOf(section), "2026-09-01", "2026-09-01")
        assertFalse(calendar.contains("BEGIN:VEVENT")); assertTrue(calendar.contains("END:VCALENDAR"))
        assertThrows(IllegalArgumentException::class.java) { IcsExporter.buildTimetableIcs(listOf(section), "2026-09-02", "2026-09-01") }
    }
    @Test fun singleExportContainsOnlyTheClickedMeetingAndRepeatsThroughTerm() {
        val ics = IcsExporter.buildTimetableMeetingIcs(section, tuesday, term).replace("\r\n ", "")
        assertEquals(1, Regex("BEGIN:VEVENT").findAll(ics).count())
        assertTrue(ics.contains("DTSTART;TZID=Asia/Hong_Kong:20260901T170000"))
        assertTrue(ics.contains("DTEND;TZID=Asia/Hong_Kong:20260901T183000"))
        assertTrue(ics.contains("RRULE:FREQ=WEEKLY;UNTIL=20261130T155959Z;BYDAY=TU"))
        assertTrue(ics.contains("LOCATION:LKK101"))
        assertTrue(ics.contains("SUMMARY:ACT3300 Company Law"))
        assertFalse(ics.contains("BYDAY=TH"))
        assertFalse(ics.contains("T123000"))
    }

    @Test fun wholeTimetableStillExportsEveryMeeting() {
        val ics = IcsExporter.buildTimetableIcs(listOf(section), term.startDate, term.endDate)
        assertEquals(2, Regex("BEGIN:VEVENT").findAll(ics).count())
        assertTrue(ics.contains("BYDAY=TU"))
        assertTrue(ics.contains("BYDAY=TH"))
    }

    @Test fun insertTimesMatchHongKongTimeAndInclusiveMinuteEnd() {
        val (begin, end) = IcsExporter.timetableMeetingTimes(tuesday, term)
        assertEquals(Instant.parse("2026-09-01T09:00:00Z").toEpochMilli(), begin)
        assertEquals(Instant.parse("2026-09-01T10:30:00Z").toEpochMilli(), end)
    }

    @Test fun firstOccurrenceMovesForwardToSelectedWeekday() {
        val monday = tuesday.copy(day = "MON")
        val (begin, _) = IcsExporter.timetableMeetingTimes(monday, term)
        assertEquals(Instant.parse("2026-09-07T09:00:00Z").toEpochMilli(), begin)
    }

    @Test fun inclusiveLastMinuteCanEndAtTheNextMidnight() {
        val meeting = tuesday.copy(startMinutes = 1439, endMinutes = 1439)
        val (begin, end) = IcsExporter.timetableMeetingTimes(meeting, term)
        assertEquals(Instant.parse("2026-09-01T15:59:00Z").toEpochMilli(), begin)
        assertEquals(Instant.parse("2026-09-01T16:00:00Z").toEpochMilli(), end)
    }

    @Test fun deviceTimezoneCannotMoveTheClassOrTermBoundary() {
        val original = TimeZone.getDefault()
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"))
            val (begin, _) = IcsExporter.timetableMeetingTimes(tuesday, term)
            assertEquals(Instant.parse("2026-09-01T09:00:00Z").toEpochMilli(), begin)
            assertTrue(IcsExporter.buildTimetableMeetingIcs(section, tuesday, term).contains("UNTIL=20261130T155959Z"))
        } finally {
            TimeZone.setDefault(original)
        }
    }

    @Test fun leapDayAndEndOfDayAreHandledInTheTermTimezone() {
        val leapTerm = term.copy(startDate = "2028-02-28", endDate = "2028-02-29")
        val ics = IcsExporter.buildTimetableMeetingIcs(section, tuesday, leapTerm)
        assertTrue(ics.contains("20280229T170000"))
        assertTrue(ics.contains("UNTIL=20280229T155959Z"))
    }

    @Test fun invalidMeetingOrNoOccurrenceIsRejectedBeforeExport() {
        for (meeting in listOf(tuesday.copy(day = "???"), tuesday.copy(startMinutes = -1),
            tuesday.copy(endMinutes = 1019), tuesday.copy(endMinutes = 1440))) {
            assertThrows(IllegalArgumentException::class.java) { IcsExporter.buildTimetableMeetingIcs(section, meeting, term) }
        }
        assertThrows(IllegalArgumentException::class.java) {
            IcsExporter.buildTimetableMeetingIcs(section, thursday, term.copy(endDate = term.startDate))
        }
    }

    @Test fun exportedMeetingEscapesUserVisibleText() {
        val ics = IcsExporter.buildTimetableMeetingIcs(section.copy(courseTitle = "Law, business; ethics\nTutorial"), tuesday, term)
            .replace("\r\n ", "")
        assertTrue(ics.contains("SUMMARY:ACT3300 Law\\, business\\; ethics\\nTutorial"))
    }
}
