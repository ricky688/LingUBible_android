package com.lingubible.app

import com.lingubible.app.domain.model.*
import org.junit.Test
import org.junit.Assert.*

class InstructorFiltersTest {
    private val all = listOf(
        Instructor("a", "CHAN", "陳", department = "Chinese", avgRating = 0.0, avgGpa = 0.0, teachingLanguages = listOf("C"), nickname = "Writing teacher"),
        Instructor("b", "LEE", department = "Chinese", avgRating = 5.0, avgGpa = 4.0, teachingLanguages = listOf("E")),
        Instructor("c", "WONG", department = "English", avgRating = -1.0, avgGpa = -1.0, isCurrentStaff = false)
    )
    @Test fun combinationsAndFormerStaff() {
        val filters = InstructorFilters(departments = setOf("Chinese"), terms = setOf("2025-T2"), languages = setOf("C"))
        assertEquals(listOf("a"), filterInstructors(all, "陳", filters, setOf("CHAN")).map { it.id })
        assertEquals(2, filterInstructors(all, "", InstructorFilters()).size)
        assertEquals(3, filterInstructors(all, "", InstructorFilters(showFormerStaff = true)).size)
        assertEquals("a", filterInstructors(all, "writing", InstructorFilters()).single().id)
    }
    @Test fun zeroIsValidAndUnknownAlwaysLast() {
        assertEquals(listOf("a", "b", "c"), filterInstructors(all, "", InstructorFilters(showFormerStaff = true, sort = InstructorSort.GPA)).map { it.id })
        assertEquals(listOf("b", "a", "c"), filterInstructors(all, "", InstructorFilters(showFormerStaff = true, sort = InstructorSort.TEACHING, descending = true)).map { it.id })
    }
}
