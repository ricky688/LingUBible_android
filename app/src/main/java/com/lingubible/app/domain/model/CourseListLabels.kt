package com.lingubible.app.domain.model

fun courseTermLabel(term: String): String = when (term.trim().uppercase()) {
    "TERM 1", "T1" -> "第一學期 Term 1"
    "TERM 2", "T2" -> "第二學期 Term 2"
    "SUMMER TERM", "SUMMER", "S", "TS", "T3", "T0" -> "夏季學期 Summer"
    "", "UNKNOWN TERM" -> "學期未知 Unknown term"
    else -> term.trim()
}

fun teachingTermLabel(record: TeachingRecord): String {
    val year = Regex("^(\\d{4})(?:-\\d{2,4})?-(T[12]|T[03S]|S|SUMMER)$", RegexOption.IGNORE_CASE).matchEntire(record.term.trim())
    val academicYear = if (record.academicYear.matches(Regex("\\d{4}[-–]\\d{2,4}"))) record.academicYear
        else (year?.groupValues?.get(1)?.toIntOrNull())?.let { "$it–${it + 1}" } ?: record.academicYear
    val term = courseTermLabel(year?.groupValues?.get(2) ?: record.term)
    return listOf(academicYear, term).filter { it.isNotBlank() }.distinct().joinToString(" · ")
}

fun teachingSessionLabel(session: String): String = when (session.trim().uppercase()) {
    "LEC", "LECTURE" -> "講課 Lecture"
    "TUT", "TUTORIAL" -> "導修 Tutorial"
    "PRJ", "PROJECT" -> "專題 Project"
    "SEM", "SEMINAR" -> "研討 Seminar"
    "" -> ""
    else -> "組別 Section ${session.trim()}"
}
