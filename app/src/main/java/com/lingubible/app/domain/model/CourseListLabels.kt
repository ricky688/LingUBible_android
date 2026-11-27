package com.lingubible.app.domain.model

fun courseTermLabel(term: String, isZh: Boolean = com.lingubible.app.core.settings.AppSettingsManager.isZh): String = when (term.trim().uppercase()) {
    "TERM 1", "T1" -> if (isZh) "第一學期 Term 1" else "Term 1"
    "TERM 2", "T2" -> if (isZh) "第二學期 Term 2" else "Term 2"
    "SUMMER TERM", "SUMMER", "S", "TS", "T3", "T0" -> if (isZh) "夏季學期 Summer" else "Summer"
    "", "UNKNOWN TERM" -> if (isZh) "學期未知 Unknown term" else "Unknown term"
    else -> term.trim()
}

fun teachingTermLabel(record: TeachingRecord, isZh: Boolean = com.lingubible.app.core.settings.AppSettingsManager.isZh): String {
    val year = Regex("^(\\d{4})(?:-\\d{2,4})?-(T[12]|T[03S]|S|SUMMER)$", RegexOption.IGNORE_CASE).matchEntire(record.term.trim())
    val academicYear = if (record.academicYear.matches(Regex("\\d{4}[-–]\\d{2,4}"))) record.academicYear
        else (year?.groupValues?.get(1)?.toIntOrNull())?.let { "$it–${it + 1}" } ?: record.academicYear
    val term = courseTermLabel(year?.groupValues?.get(2) ?: record.term, isZh)
    return listOf(academicYear, term).filter { it.isNotBlank() }.distinct().joinToString(" · ")
}

fun teachingSessionLabel(session: String, isZh: Boolean = com.lingubible.app.core.settings.AppSettingsManager.isZh): String = when (session.trim().uppercase()) {
    "LEC", "LECTURE" -> if (isZh) "講課 Lecture" else "Lecture"
    "TUT", "TUTORIAL" -> if (isZh) "導修 Tutorial" else "Tutorial"
    "PRJ", "PROJECT" -> if (isZh) "專題 Project" else "Project"
    "SEM", "SEMINAR" -> if (isZh) "研討 Seminar" else "Seminar"
    "" -> ""
    else -> if (isZh) "組別 Section ${session.trim()}" else "Section ${session.trim()}"
}
