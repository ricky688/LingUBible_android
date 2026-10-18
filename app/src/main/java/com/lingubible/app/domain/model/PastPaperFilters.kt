package com.lingubible.app.domain.model

import com.lingubible.app.domain.util.PastPapersParser

fun pastPaperInstructors(paper: PastPaper, records: List<TeachingRecord>): List<String> {
    paper.instructorName?.takeIf { it.isNotBlank() }?.let { return listOf(it) }
    val year = paper.academicYear.substringBefore('-').toIntOrNull() ?: return emptyList()
    val suffixes = when (paper.term) { "Term 1" -> setOf("T1"); "Term 2" -> setOf("T2"); "Summer Term" -> setOf("S", "SUMMER", "TS", "T3", "T0"); else -> return emptyList() }
    return records.filter {
        it.courseCode.equals(paper.courseCode, true) && it.term.take(4).toIntOrNull() == year &&
            it.term.substringAfterLast('-').uppercase() in suffixes &&
            it.section.uppercase() in setOf("LEC", "LECTURE")
    }.map { it.instructorName }.filter { it.isNotBlank() }.distinct().sorted()
}

fun filterPastPapers(papers: List<PastPaper>, records: List<TeachingRecord>, years: Set<String>, instructors: Set<String>, oldestFirst: Boolean): List<PastPaper> =
    papers.filter { (years.isEmpty() || it.academicYear in years) &&
        (instructors.isEmpty() || pastPaperInstructors(it, records).any(instructors::contains)) }
        .sortedWith(compareBy<PastPaper> { PastPapersParser.getTermSortKey(it.academicYear, it.term) == 0 }
            .thenBy { val key = PastPapersParser.getTermSortKey(it.academicYear, it.term); if (oldestFirst) key else -key }
            .thenBy { it.fileName.lowercase() })
