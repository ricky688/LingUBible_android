package com.lingubible.app

import com.lingubible.app.domain.model.*
import org.junit.Assert.*
import org.junit.Test

class PastPaperFiltersTest {
    private fun paper(id: String, year: String, term: String, name: String? = null) = PastPaper(id, "BUS1102", year, term, name, id, "$id.pdf")
    private val records = listOf(
        TeachingRecord("1", "BUS1102", "Lecturer", term = "2025-T2", section = "Lecture"),
        TeachingRecord("2", "BUS1102", "Tutor", term = "2025-T2", section = "Tutorial"),
        TeachingRecord("3", "BUS1102", "Other term", term = "2025-T1", section = "LEC"),
        TeachingRecord("4", "OTHER", "Other course", term = "2025-T2", section = "LEC")
    )
    @Test fun filenameInstructorWinsAndFallbackUsesMatchingLectureOnly() {
        assertEquals(listOf("Named"), pastPaperInstructors(paper("a", "2025-2026", "Term 2", "Named"), records))
        assertEquals(listOf("Lecturer"), pastPaperInstructors(paper("a", "2025-2026", "Term 2"), records))
        assertTrue(pastPaperInstructors(paper("a", "Unknown", ""), records).isEmpty())
    }
    @Test fun summerUsesTheWebDatabaseAliases() {
        val paper = paper("summer", "2025-2026", "Summer Term")
        listOf("S", "Summer", "T3").forEach { suffix ->
            assertEquals(listOf("Lecturer"), pastPaperInstructors(paper, listOf(TeachingRecord("s", "BUS1102", "Lecturer", term = "2025-$suffix", section = "Lecture"))))
        }
    }
    @Test fun combinedFiltersAndUnknownDatesLastInBothDirections() {
        val papers = listOf(paper("unknown", "Unknown", ""), paper("old", "2024-2025", "Term 1"), paper("new", "2025-2026", "Term 2"))
        assertEquals(listOf("new", "old", "unknown"), filterPastPapers(papers, records, emptySet(), emptySet(), false).map { it.id })
        assertEquals(listOf("old", "new", "unknown"), filterPastPapers(papers, records, emptySet(), emptySet(), true).map { it.id })
        assertEquals(listOf("new"), filterPastPapers(papers, records, setOf("2025-2026"), setOf("Lecturer"), false).map { it.id })
        assertTrue(filterPastPapers(papers, records, setOf("2024-2025"), setOf("Lecturer"), false).isEmpty())
    }
}
