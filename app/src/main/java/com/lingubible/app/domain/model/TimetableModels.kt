package com.lingubible.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class TimetableTerm(
    val id: String,
    val name: String,
    val short: String,
    val assetPath: String,
    val startDate: String = "2026-09-01",
    val endDate: String = "2026-11-30"
)

val DEFAULT_TERMS = listOf(
    // 2026–27
    TimetableTerm(
        id = "2026-27-t1",
        name = "2026–27 Term 1",
        short = "2627-T1",
        assetPath = "data/2026-T1.csv",
        startDate = "2026-09-01",
        endDate = "2026-11-30"
    ),
    TimetableTerm(
        id = "2026-27-t2",
        name = "2026–27 Term 2",
        short = "2627-T2",
        assetPath = "data/2026-T2.csv",
        startDate = "2027-01-08",
        endDate = "2027-04-20"
    ),
    // 2025–26
    TimetableTerm(
        id = "2025-26-t1",
        name = "2025–26 Term 1",
        short = "2526-T1",
        assetPath = "data/2025-T1.csv",
        startDate = "2025-09-01",
        endDate = "2025-11-28"
    ),
    TimetableTerm(
        id = "2025-26-t2",
        name = "2025–26 Term 2",
        short = "2526-T2",
        assetPath = "data/2025-T2.csv",
        startDate = "2026-01-09",
        endDate = "2026-04-21"
    ),
    TimetableTerm(
        id = "2025-26-s",
        name = "2025–26 Summer Term",
        short = "2526-S",
        assetPath = "data/2025-S.csv",
        startDate = "2026-05-25",
        endDate = "2026-07-15"
    ),
    // 2024–25
    TimetableTerm(
        id = "2024-25-t1",
        name = "2024–25 Term 1",
        short = "2425-T1",
        assetPath = "data/2024-T1.csv",
        startDate = "2024-09-02",
        endDate = "2024-11-29"
    ),
    TimetableTerm(
        id = "2024-25-t2",
        name = "2024–25 Term 2",
        short = "2425-T2",
        assetPath = "data/2024-T2.csv",
        startDate = "2025-01-10",
        endDate = "2025-04-22"
    ),
    TimetableTerm(
        id = "2024-25-s",
        name = "2024–25 Summer Term",
        short = "2425-S",
        assetPath = "data/2024-S.csv",
        startDate = "2025-05-26",
        endDate = "2025-07-16"
    )
)

val TimetableTerm.academicYear: String
    get() {
        val raw = name.substringBefore(" Term").substringBefore(" Summer")
        return raw.trim()
    }

val TimetableTerm.termLabelZh: String
    get() = when {
        name.contains("Term 1") -> "第一學期"
        name.contains("Term 2") -> "第二學期"
        name.contains("Summer") -> "暑期學期"
        else -> name
    }

val TimetableTerm.termLabelEn: String
    get() = when {
        name.contains("Term 1") -> "Term 1"
        name.contains("Term 2") -> "Term 2"
        name.contains("Summer") -> "Summer Term"
        else -> name
    }

val TimetableTerm.termBadge: String
    get() = when {
        name.contains("Term 1") -> "T1"
        name.contains("Term 2") -> "T2"
        name.contains("Summer") -> "SUM"
        else -> "T"
    }

val TimetableTerm.termDateSummary: String
    get() = when {
        startDate.isNotBlank() && endDate.isNotBlank() -> {
            val s = startDate.replace("-", "/")
            val e = endDate.replace("-", "/")
            "$s ~ $e"
        }
        else -> ""
    }


@Serializable
data class TimetableMeeting(
    val day: String, // MON, TUE, WED, THU, FRI, SAT, SUN
    val startMinutes: Int, // e.g. 13:30 -> 810
    val endMinutes: Int, // e.g. 16:29 -> 989
    val start: String, // "13:30"
    val end: String, // "16:29"
    val venue: String = "",
    val type: String = "LEC"
) {
    /** Returns whether this meeting overlaps in time with another on the same weekday */
    fun overlaps(other: TimetableMeeting): Boolean {
        if (day != other.day) return false
        return startMinutes < other.endMinutes && other.startMinutes < endMinutes
    }
}

@Serializable
data class TimetableSection(
    val id: String, // unique key, typically CRN
    val crn: String,
    val courseCode: String,
    val courseTitle: String,
    val courseTitleZh: String = "",
    val section: String = "1",
    val language: String = "E",
    val types: List<String> = listOf("LEC"),
    val instructors: List<String> = emptyList(),
    val instructorEmails: List<String> = emptyList(),
    val meetings: List<TimetableMeeting> = emptyList(),
    val colorHex: String = "#DC2626"
) {
    val department: String
        get() = courseCode.takeWhile { it.isLetter() }

    /** Returns whether any meeting of this section overlaps with another section */
    fun conflictsWith(other: TimetableSection): List<Pair<TimetableMeeting, TimetableMeeting>> {
        if (id == other.id) return emptyList()
        val conflicts = mutableListOf<Pair<TimetableMeeting, TimetableMeeting>>()
        for (m1 in meetings) {
            for (m2 in other.meetings) {
                if (m1.overlaps(m2)) {
                    conflicts.add(m1 to m2)
                }
            }
        }
        return conflicts
    }
}

data class TimeConflict(
    val sectionA: TimetableSection,
    val sectionB: TimetableSection,
    val day: String,
    val timeDescription: String
)

val COURSE_PALETTE = listOf(
    "#DC2626", // Lingnan Red
    "#2563EB", // Royal Blue
    "#059669", // Emerald Green
    "#D97706", // Amber
    "#7C3AED", // Violet
    "#0891B2", // Cyan
    "#DB2777", // Pink
    "#4F46E5", // Indigo
    "#EA580C", // Orange
    "#0D9488", // Teal
    "#65A30D", // Lime
    "#9333EA"  // Purple
)

fun getCourseColor(courseCode: String, index: Int = 0): String {
    val hash = kotlin.math.abs(courseCode.hashCode() + index)
    return COURSE_PALETTE[hash % COURSE_PALETTE.size]
}
