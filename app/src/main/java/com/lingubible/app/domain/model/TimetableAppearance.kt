package com.lingubible.app.domain.model

import kotlinx.serialization.Serializable
import java.time.LocalDate
import kotlin.math.pow

val TIMETABLE_FIELDS = linkedMapOf("code" to "課程代碼 Code", "title" to "課程名稱 Title", "crn" to "CRN", "session" to "組別／課節 Section/session", "venue" to "地點 Location", "time" to "時間 Time", "instructors" to "講師 Instructors", "credits" to "學分 Credits")
val TIMETABLE_DAYS = listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")

@Serializable
data class TimetableAppearance(
    val title: String = "", val colors: Map<String, String> = emptyMap(),
    val fields: Set<String> = setOf("code", "title", "crn", "session", "venue", "time"),
    val days: Set<String> = TIMETABLE_DAYS.toSet(), val mondayFirst: Boolean = false,
    val autoRange: Boolean = true, val startMinutes: Int = 510, val endMinutes: Int = 1290,
    val resolution: Int = 2, val icsStart: String = "", val icsEnd: String = ""
) {
    val orderedDays: List<String> get() = (if (mondayFirst) TIMETABLE_DAYS.drop(1) + "SUN" else TIMETABLE_DAYS).filter { it in days }
}
fun timetableAppearanceError(options: TimetableAppearance, term: TimetableTerm): String? = when {
    options.title.length > 120 || options.title.any { it.isISOControl() } -> "標題上限 120 字元 Title: maximum 120 characters"
    options.days.isEmpty() || !TIMETABLE_DAYS.containsAll(options.days) -> "請選擇至少一天 Select at least one day"
    options.fields.none { it in setOf("code", "title") } || !TIMETABLE_FIELDS.keys.containsAll(options.fields) -> "保留課程代碼或名稱 Keep course code or title"
    options.startMinutes !in 0..1439 || options.endMinutes !in 1..1440 || options.endMinutes <= options.startMinutes -> "結束時間須晚於開始時間 End time must follow start time"
    options.resolution !in 1..3 -> "解像度無效 Invalid resolution"
    options.colors.any { (code, hex) -> code.length !in 1..32 || !hex.matches(Regex("#[0-9a-fA-F]{6}")) } -> "顏色須為 #RRGGBB Use a #RRGGBB color"
    runCatching { LocalDate.parse(options.icsEnd.ifBlank { term.endDate }) < LocalDate.parse(options.icsStart.ifBlank { term.startDate }) }.getOrDefault(true) -> "日曆日期無效或次序錯誤 Invalid calendar dates or date order"
    else -> null
}
fun timetableMinuteRange(sections: List<TimetableSection>, options: TimetableAppearance): Pair<Int, Int> {
    if (!options.autoRange) return options.startMinutes to options.endMinutes
    val meetings = sections.flatMap { it.meetings }.filter { it.day in options.days }
    if (meetings.isEmpty()) return 510 to 1290
    val start = ((meetings.minOf { it.startMinutes } / 30) * 30 - 30).coerceAtLeast(0)
    val end = (((meetings.maxOf { it.endMinutes + 1 } + 29) / 30) * 30 + 30).coerceAtMost(1440)
    val paddedEnd = maxOf(end, start + 240).coerceAtMost(1440)
    return minOf(start, paddedEnd - 240).coerceAtLeast(0) to paddedEnd
}
fun timetableClock(minutes: Int): String = "%02d:%02d".format(minutes / 60, minutes % 60)
fun parseTimetableClock(text: String): Int? {
    if (!text.matches(Regex("(?:[01][0-9]|2[0-3]):[0-5][0-9]|24:00"))) return null
    return text.take(2).toInt() * 60 + text.takeLast(2).toInt()
}
fun timetableBlockLines(section: TimetableSection, meeting: TimetableMeeting, options: TimetableAppearance, credits: Int?): List<String> = buildList {
    if ("code" in options.fields) add(section.courseCode)
    if ("title" in options.fields) add(section.courseTitle.ifBlank { section.courseTitleZh })
    val details = buildList {
        if ("session" in options.fields) add("S${section.section} · ${meeting.type}")
        if ("crn" in options.fields && section.crn.isNotBlank()) add("CRN ${section.crn}")
    }
    if (details.isNotEmpty()) add(details.joinToString(" · "))
    if ("venue" in options.fields) add(meeting.venue.ifBlank { "待定 TBD" })
    if ("time" in options.fields) add("${meeting.start}–${meeting.end}")
    if ("instructors" in options.fields) add(section.instructors.joinToString())
    if ("credits" in options.fields) add(credits?.let { "$it 學分 Credits" } ?: "學分未知 Credits unknown")
}.filter { it.isNotBlank() }
fun timetableUsesLightText(hex: String): Boolean {
    fun channel(offset: Int): Double {
        val value = hex.substring(offset, offset + 2).toInt(16) / 255.0
        return if (value <= .04045) value / 12.92 else ((value + .055) / 1.055).pow(2.4)
    }
    val luminance = .2126 * channel(1) + .7152 * channel(3) + .0722 * channel(5)
    return luminance < .179
}
fun timetableExportSize(width: Int, height: Int, resolution: Int): Pair<Int, Int> {
    require(width > 0 && height > 0 && resolution in 1..3)
    return (width * resolution / 2) to (height * resolution / 2)
}
