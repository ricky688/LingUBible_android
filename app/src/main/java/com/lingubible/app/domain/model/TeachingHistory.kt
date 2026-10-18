package com.lingubible.app.domain.model

import kotlinx.serialization.Serializable
import com.lingubible.app.domain.util.GradeCalculator

@Serializable
data class TeachingHistoryFilters(val terms: Set<String> = emptySet(), val languages: Set<String> = emptySet(),
    val sessions: Set<String> = emptySet(), val courses: Set<String> = emptySet())

fun filterTeachingHistory(records: List<TeachingRecord>, filters: TeachingHistoryFilters): List<TeachingRecord> =
    records.distinctBy { it.id }.filter {
        (filters.terms.isEmpty() || it.term in filters.terms) &&
        (filters.languages.isEmpty() || it.teachingLanguage in filters.languages) &&
        (filters.sessions.isEmpty() || teachingSessionLabel(it.section) in filters.sessions) &&
        (filters.courses.isEmpty() || it.courseCode in filters.courses)
    }.sortedWith(compareByDescending<TeachingRecord> { it.term }.thenBy { it.courseCode }.thenBy { it.id })

fun instructorGradeDistribution(reviews: List<Review>, instructor: String, records: List<TeachingRecord>, filters: TeachingHistoryFilters): Map<String, Int> {
    val history = filterTeachingHistory(records, filters)
    return reviews.distinctBy { it.id }.filter { review ->
        val evaluations = review.instructors.filter { it.name.equals(instructor, true) }
        val belongs = evaluations.isNotEmpty() || (review.instructors.isEmpty() && review.instructorName.split('/').any { it.trim().equals(instructor, true) })
        belongs && (filters.courses.isEmpty() || review.courseCode in filters.courses) &&
            (filters.terms.isEmpty() || review.term in filters.terms) &&
            (filters.sessions.isEmpty() || evaluations.any { teachingSessionLabel(it.session) in filters.sessions } ||
                (evaluations.isEmpty() && history.any { it.courseCode == review.courseCode && it.term == review.term })) &&
            (filters.languages.isEmpty() || history.any { it.courseCode == review.courseCode && it.term == review.term &&
                (evaluations.isEmpty() || evaluations.any { e -> teachingSessionLabel(e.session) == teachingSessionLabel(it.section) }) })
    }.groupingBy { it.grade.takeIf { grade -> grade in GradeCalculator.GRADE_TO_GPA_MAP } ?: "N/A" }.eachCount()
}
