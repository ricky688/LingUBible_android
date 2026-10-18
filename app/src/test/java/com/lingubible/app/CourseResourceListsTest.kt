package com.lingubible.app

import com.lingubible.app.domain.model.*
import org.junit.Assert.*
import org.junit.Test

class CourseResourceListsTest {
    @Test fun courseTermAndSessionLabelsAvoidDuplicateYearsAndIncorrectSectionBadges() {
        assertEquals("2025–2026 · 第二學期 Term 2", teachingTermLabel(TeachingRecord("a", "BUS1102", "Teacher", term = "2025-T2", academicYear = "2025")))
        assertEquals("2024-2025 · 第一學期 Term 1", teachingTermLabel(TeachingRecord("a", "BUS1102", "Teacher", term = "Term 1", academicYear = "2024-2025")))
        assertEquals("2025–2026 · 夏季學期 Summer", teachingTermLabel(TeachingRecord("a", "BUS1102", "Teacher", term = "2025-Summer")))
        assertEquals("講課 Lecture", teachingSessionLabel("LEC"))
        assertEquals("導修 Tutorial", teachingSessionLabel("Tutorial"))
        assertEquals("組別 Section 1", teachingSessionLabel("1"))
        assertEquals("", teachingSessionLabel(""))
        assertEquals("學期未知 Unknown term", courseTermLabel("Unknown Term"))
    }
    @Test fun historyKeepsRepeatedCoursesAndCombinesFilters() {
        val records = listOf(
            TeachingRecord("a", "BUS1102", "Teacher", term = "2025-T1", section = "LEC", teachingLanguage = "E"),
            TeachingRecord("b", "BUS1102", "Teacher", term = "2025-T2", section = "TUT", teachingLanguage = "C"),
            TeachingRecord("c", "ACT1100", "Teacher", term = "2025-T2", section = "LEC", teachingLanguage = "E"))
        assertEquals(3, filterTeachingHistory(records, TeachingHistoryFilters()).size)
        assertEquals(listOf("c"), filterTeachingHistory(records, TeachingHistoryFilters(terms = setOf("2025-T2"), languages = setOf("E"), sessions = setOf(teachingSessionLabel("LEC")))).map { it.id })
        assertEquals(listOf("b", "a"), filterTeachingHistory(records, TeachingHistoryFilters(courses = setOf("BUS1102"))).map { it.id })
    }
    @Test fun instructorGradesCountEachReviewOnceAndRespectHistoryLanguageAndSession() {
        val records = listOf(TeachingRecord("a", "BUS1102", "Teacher", term = "2025-T1", section = "LEC", teachingLanguage = "E"))
        val valid = Review("1", "BUS1102", term = "2025-T1", grade = "F", instructorName = "Teacher")
        val other = valid.copy(id = "2", instructorName = "Other", grade = "A")
        assertEquals(mapOf("F" to 1), instructorGradeDistribution(listOf(valid, valid, other), "Teacher", records, TeachingHistoryFilters(languages = setOf("E"))))
        assertTrue(instructorGradeDistribution(listOf(valid), "Teacher", records, TeachingHistoryFilters(languages = setOf("C"))).isEmpty())
        assertEquals(mapOf("N/A" to 1), instructorGradeDistribution(listOf(valid.copy(grade = "-1")), "Teacher", records, TeachingHistoryFilters()))
    }
}
