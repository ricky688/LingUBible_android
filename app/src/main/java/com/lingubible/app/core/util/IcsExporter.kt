package com.lingubible.app.core.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import androidx.core.content.FileProvider
import com.lingubible.app.domain.model.AcademicEvent
import com.lingubible.app.domain.model.TimetableSection
import com.lingubible.app.domain.model.TimetableMeeting
import com.lingubible.app.domain.model.TimetableTerm
import java.io.File
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.*

object IcsExporter {

    private val BYDAY = mapOf(
        "MON" to "MO", "TUE" to "TU", "WED" to "WE", "THU" to "TH",
        "FRI" to "FR", "SAT" to "SA", "SUN" to "SU"
    )

    private val timetableZone = ZoneId.of("Asia/Hong_Kong")

    private fun escapeText(value: String): String {
        return value
            .replace("\\", "\\\\")
            .replace(";", "\\;")
            .replace(",", "\\,")
            .replace("\n", "\\n")
    }

    private fun fold(line: String): String {
        if (line.length <= 75) return line
        val sb = StringBuilder()
        var out = line.substring(0, 75)
        var rest = line.substring(75)
        sb.append(out)
        while (rest.isNotEmpty()) {
            val chunkLength = Math.min(74, rest.length)
            sb.append("\r\n ").append(rest.substring(0, chunkLength))
            rest = rest.substring(chunkLength)
        }
        return sb.toString()
    }

    private fun pad2(n: Int) = String.format(Locale.US, "%02d", n)

    private fun hms(minutes: Int): String {
        val h = minutes / 60
        val m = minutes % 60
        return "${pad2(h)}${pad2(m)}00"
    }

    private fun firstOccurrence(startYmd: String, day: String): String {
        return firstMeetingDate(startYmd, day).format(DateTimeFormatter.BASIC_ISO_DATE)
    }

    private fun firstMeetingDate(startYmd: String, day: String): LocalDate {
        require(day in BYDAY) { "Invalid meeting weekday" }
        val weekday = DayOfWeek.values().first { it.name.startsWith(day) }
        return LocalDate.parse(startYmd).with(TemporalAdjusters.nextOrSame(weekday))
    }

    private fun recurrenceEnd(endYmd: String): String =
        DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'", Locale.US)
            .withZone(ZoneId.of("UTC"))
            .format(LocalDate.parse(endYmd).plusDays(1).atStartOfDay(timetableZone).minusSeconds(1))

    internal fun timetableMeetingTimes(meeting: TimetableMeeting, term: TimetableTerm): Pair<Long, Long> {
        require(meeting.startMinutes in 0..1439 && meeting.endMinutes in meeting.startMinutes..1439)
        val date = firstMeetingDate(term.startDate, meeting.day)
        require(!date.isAfter(LocalDate.parse(term.endDate))) { "No meeting within term" }
        val midnight = date.atStartOfDay(timetableZone)
        return midnight.plusMinutes(meeting.startMinutes.toLong()).toInstant().toEpochMilli() to
            midnight.plusMinutes(meeting.endMinutes.toLong() + 1).toInstant().toEpochMilli()
    }

    private fun nowStamp(): String {
        val sdf = SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }

    /**
     * Build standard RFC 5545 .ics text for a selected timetable with weekly recurring meetings.
     */
    fun buildTimetableIcs(
        sections: List<TimetableSection>,
        startYmd: String = "2026-09-01",
        endYmd: String = "2026-11-30",
        calendarName: String = "LingUBible Course Timetable"
    ): String {
        require(!LocalDate.parse(endYmd).isBefore(LocalDate.parse(startYmd))) { "Invalid calendar date range" }
        val lines = mutableListOf<String>()
        val push = { line: String -> lines.add(fold(line)) }

        push("BEGIN:VCALENDAR")
        push("VERSION:2.0")
        push("PRODID:-//LingUBible//Timetable//EN")
        push("CALSCALE:GREGORIAN")
        push("METHOD:PUBLISH")
        push("X-WR-CALNAME:${escapeText(calendarName)}")
        push("X-WR-TIMEZONE:Asia/Hong_Kong")

        push("BEGIN:VTIMEZONE")
        push("TZID:Asia/Hong_Kong")
        push("BEGIN:STANDARD")
        push("DTSTART:19700101T000000")
        push("TZOFFSETFROM:+0800")
        push("TZOFFSETTO:+0800")
        push("TZNAME:HKT")
        push("END:STANDARD")
        push("END:VTIMEZONE")

        val stamp = nowStamp()
        val until = recurrenceEnd(endYmd)

        for (s in sections) {
            for ((idx, m) in s.meetings.withIndex()) {
                val date = firstOccurrence(startYmd, m.day)
                if (date > endYmd.replace("-", "")) continue
                val byDay = BYDAY[m.day] ?: "MO"

                val description = listOfNotNull(
                    "${s.courseCode} ${s.courseTitle}",
                    "Type: ${m.type}${if (s.section.isNotBlank()) " (Section ${s.section})" else ""}",
                    if (s.instructors.isNotEmpty()) "Instructor: ${s.instructors.joinToString(", ")}" else null,
                    if (m.venue.isNotBlank()) "Venue: ${m.venue}" else null,
                    if (s.crn.isNotBlank()) "CRN: ${s.crn}" else null
                ).joinToString("\n")

                push("BEGIN:VEVENT")
                push("UID:${s.crn.ifBlank { s.id }}-${m.day}-${hms(m.startMinutes)}-$idx@lingubible.com")
                push("DTSTAMP:$stamp")
                push("DTSTART;TZID=Asia/Hong_Kong:${date}T${hms(m.startMinutes)}")
                push("DTEND;TZID=Asia/Hong_Kong:${date}T${hms(m.endMinutes + 1)}")
                push("RRULE:FREQ=WEEKLY;UNTIL=$until;BYDAY=$byDay")
                push("SUMMARY:${escapeText("${s.courseCode} ${s.courseTitle}")}")
                if (m.venue.isNotBlank()) push("LOCATION:${escapeText(m.venue)}")
                push("DESCRIPTION:${escapeText(description)}")
                push("END:VEVENT")
            }
        }

        push("END:VCALENDAR")
        return lines.joinToString("\r\n")
    }

    fun buildTimetableMeetingIcs(section: TimetableSection, meeting: TimetableMeeting, term: TimetableTerm): String {
        timetableMeetingTimes(meeting, term)
        return buildTimetableIcs(
            listOf(section.copy(meetings = listOf(meeting))), term.startDate, term.endDate,
            "LingUBible ${term.name}"
        )
    }

    fun createTimetableMeetingInsertIntent(section: TimetableSection, meeting: TimetableMeeting, term: TimetableTerm): Intent {
        val (begin, end) = timetableMeetingTimes(meeting, term)
        return Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, "${section.courseCode} ${section.courseTitle}")
            putExtra(CalendarContract.Events.EVENT_LOCATION, meeting.venue)
            putExtra(CalendarContract.Events.DESCRIPTION,
                "Section ${section.section} · ${meeting.type}\n${section.instructors.joinToString(", ")}\nCRN: ${section.crn}")
            putExtra(CalendarContract.Events.EVENT_TIMEZONE, timetableZone.id)
            putExtra(CalendarContract.Events.RRULE, "FREQ=WEEKLY;UNTIL=${recurrenceEnd(term.endDate)};BYDAY=${BYDAY.getValue(meeting.day)}")
            putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, false)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, begin)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, end)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    /**
     * Build RFC 5545 .ics text for a single academic event.
     */
    fun buildSingleEventIcs(event: AcademicEvent): String {
        val lines = mutableListOf<String>()
        val push = { line: String -> lines.add(fold(line)) }

        push("BEGIN:VCALENDAR")
        push("VERSION:2.0")
        push("PRODID:-//LingUBible//AcademicCalendar//EN")
        push("CALSCALE:GREGORIAN")
        push("METHOD:PUBLISH")

        val stamp = nowStamp()
        val startStr = event.start.replace("-", "")

        // For all-day events in ICS: end date is non-inclusive day after end
        val endStr = if (event.end != null) {
            try {
                val parts = event.end.split("-").map { it.toInt() }
                val cal = Calendar.getInstance()
                cal.set(parts[0], parts[1] - 1, parts[2])
                cal.add(Calendar.DAY_OF_MONTH, 1)
                "${cal.get(Calendar.YEAR)}${pad2(cal.get(Calendar.MONTH) + 1)}${pad2(cal.get(Calendar.DAY_OF_MONTH))}"
            } catch (e: Exception) {
                event.end.replace("-", "")
            }
        } else {
            try {
                val parts = event.start.split("-").map { it.toInt() }
                val cal = Calendar.getInstance()
                cal.set(parts[0], parts[1] - 1, parts[2])
                cal.add(Calendar.DAY_OF_MONTH, 1)
                "${cal.get(Calendar.YEAR)}${pad2(cal.get(Calendar.MONTH) + 1)}${pad2(cal.get(Calendar.DAY_OF_MONTH))}"
            } catch (e: Exception) {
                startStr
            }
        }

        push("BEGIN:VEVENT")
        push("UID:${event.id}@lingubible.com")
        push("DTSTAMP:$stamp")
        push("DTSTART;VALUE=DATE:$startStr")
        push("DTEND;VALUE=DATE:$endStr")
        push("SUMMARY:${escapeText(event.displayTitle)}")
        push("DESCRIPTION:${escapeText("${event.title}\n分類: ${event.category.labelZh}")}")
        push("CATEGORIES:${escapeText(event.category.labelEn)}")
        push("END:VEVENT")

        push("END:VCALENDAR")
        return lines.joinToString("\r\n")
    }

    /**
     * Build RFC 5545 .ics text for multiple academic events.
     */
    fun buildMultipleEventsIcs(events: List<AcademicEvent>): String {
        val lines = mutableListOf<String>()
        val push = { line: String -> lines.add(fold(line)) }

        push("BEGIN:VCALENDAR")
        push("VERSION:2.0")
        push("PRODID:-//LingUBible//AcademicCalendar//EN")
        push("CALSCALE:GREGORIAN")
        push("METHOD:PUBLISH")

        val stamp = nowStamp()

        for (event in events) {
            val startStr = event.start.replace("-", "")
            val endStr = if (event.end != null) {
                try {
                    val parts = event.end.split("-").map { it.toInt() }
                    val cal = Calendar.getInstance()
                    cal.set(parts[0], parts[1] - 1, parts[2])
                    cal.add(Calendar.DAY_OF_MONTH, 1)
                    "${cal.get(Calendar.YEAR)}${pad2(cal.get(Calendar.MONTH) + 1)}${pad2(cal.get(Calendar.DAY_OF_MONTH))}"
                } catch (e: Exception) {
                    event.end.replace("-", "")
                }
            } else {
                try {
                    val parts = event.start.split("-").map { it.toInt() }
                    val cal = Calendar.getInstance()
                    cal.set(parts[0], parts[1] - 1, parts[2])
                    cal.add(Calendar.DAY_OF_MONTH, 1)
                    "${cal.get(Calendar.YEAR)}${pad2(cal.get(Calendar.MONTH) + 1)}${pad2(cal.get(Calendar.DAY_OF_MONTH))}"
                } catch (e: Exception) {
                    startStr
                }
            }

            push("BEGIN:VEVENT")
            push("UID:${event.id}@lingubible.com")
            push("DTSTAMP:$stamp")
            push("DTSTART;VALUE=DATE:$startStr")
            push("DTEND;VALUE=DATE:$endStr")
            push("SUMMARY:${escapeText(event.displayTitle)}")
            push("DESCRIPTION:${escapeText("${event.title}\n分類: ${event.category.labelZh}")}")
            push("CATEGORIES:${escapeText(event.category.labelEn)}")
            push("END:VEVENT")
        }

        push("END:VCALENDAR")
        return lines.joinToString("\r\n")
    }

    /**
     * Create an Android Intent to directly insert this event into the device's default Calendar application.
     */
    fun createCalendarInsertIntent(event: AcademicEvent): Intent {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("Asia/Hong_Kong")

        val startMillis = try {
            sdf.parse(event.start)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }

        val endMillis = if (event.end != null) {
            try {
                val d = sdf.parse(event.end)
                // Add 1 day for inclusive end in Android CalendarContract
                (d?.time ?: startMillis) + 24 * 60 * 60 * 1000
            } catch (e: Exception) {
                startMillis + 24 * 60 * 60 * 1000
            }
        } else {
            startMillis + 24 * 60 * 60 * 1000
        }

        return Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, event.displayTitle)
            putExtra(CalendarContract.Events.DESCRIPTION, "${event.title}\n分類: ${event.category.labelZh}")
            putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    /**
     * Write .ics content to cache and launch Android share sheet.
     */
    fun shareIcs(context: Context, icsContent: String, fileName: String = "calendar.ics"): Result<Unit> {
        return runCatching {
            val cacheFile = File(context.cacheDir, fileName)
            cacheFile.writeText(icsContent, Charsets.UTF_8)

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                cacheFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/calendar"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, fileName.removeSuffix(".ics"))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "匯出日曆 Export Calendar")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        }.onFailure { it.printStackTrace() }
    }
}
