package com.lingubible.app

import com.lingubible.app.domain.model.*
import org.junit.Assert.*
import org.junit.Test

class ReviewBrowsingTest {
    private val lecture = InstructorEvaluation("First", "Lecture", requirements = setOf("has_final"))
    private val tutorial = InstructorEvaluation("Second", "Tutorial", comment = "Helpful feedback", serviceLearning = "optional")
    private val reviews = listOf(
        Review("a", "BUS1102", term = "202510", grade = "A", language = "zh-TW", workloadRating = 0.0, createdAt = "2026-10-16", instructors = listOf(lecture, tutorial)),
        Review("b", "BUS1102", term = "202410", grade = "F", language = "en", workloadRating = 5.0, createdAt = "2025-01-01", instructors = listOf(lecture)),
        Review("c", "ACT1100", workloadRating = -1.0, createdAt = "2024-01-01")
    )
    @Test fun `filters combine across categories and include every instructor session`() {
        val filters = ReviewFilters(courses = setOf("BUS1102"), instructors = setOf("Second"), grades = setOf("A"),
            terms = setOf("202510"), languages = setOf("zh-TW"), sessions = setOf("Tutorial"),
            requirements = setOf("has_final"), serviceLearning = setOf("optional"))
        assertEquals(listOf("a"), filterReviews(reviews, filters).map { it.id })
        assertTrue(filterReviews(reviews, filters.copy(languages = setOf("en"))).isEmpty())
    }
    @Test fun `search includes instructor comments and multiple alternatives work within a category`() {
        assertEquals(listOf("a"), filterReviews(reviews, ReviewFilters(search = "HELPFUL")).map { it.id })
        assertEquals(3, filterReviews(reviews, ReviewFilters(courses = setOf("BUS1102", "ACT1100"))).size)
    }
    @Test fun `NA is last in both directions while explicit zero remains sortable`() {
        assertEquals(listOf("a", "b", "c"), filterReviews(reviews, ReviewFilters(sort = ReviewSort.WORKLOAD, descending = false)).map { it.id })
        assertEquals(listOf("b", "a", "c"), filterReviews(reviews, ReviewFilters(sort = ReviewSort.WORKLOAD)).map { it.id })
    }
    @Test fun `grade date and votes sort consistently`() {
        assertEquals(listOf("a", "b", "c"), filterReviews(reviews, ReviewFilters(sort = ReviewSort.GRADE)).map { it.id })
        assertEquals(listOf("c", "b", "a"), filterReviews(reviews, ReviewFilters(descending = false)).map { it.id })
        val voted = reviews.map { it.copy(upvotes = if (it.id == "b") 10 else 0) }
        assertEquals("b", filterReviews(voted, ReviewFilters(sort = ReviewSort.UPVOTES)).first().id)
    }
}
