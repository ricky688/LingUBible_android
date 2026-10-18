package com.lingubible.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class ReviewSort(val label: String) {
    DATE("日期 Date"), WORKLOAD("工作量 Workload"), DIFFICULTY("難度 Difficulty"),
    USEFULNESS("實用性 Usefulness"), TEACHING("教學 Teaching"), GRADING("給分 Grading"),
    GRADE("成績 Grade"), UPVOTES("推薦 Upvotes"), DOWNVOTES("不推薦 Downvotes")
}
@Serializable
data class ReviewFilters(
    val search: String = "", val courses: Set<String> = emptySet(), val instructors: Set<String> = emptySet(),
    val grades: Set<String> = emptySet(), val terms: Set<String> = emptySet(),
    val languages: Set<String> = emptySet(), val sessions: Set<String> = emptySet(),
    val requirements: Set<String> = emptySet(), val serviceLearning: Set<String> = emptySet(),
    val sort: ReviewSort = ReviewSort.DATE, val descending: Boolean = true
)
fun filterReviews(reviews: List<Review>, filters: ReviewFilters): List<Review> {
    fun Set<String>.accepts(value: String) = isEmpty() || value in this
    fun Set<String>.acceptsAny(values: List<String>) = isEmpty() || values.any { it in this }
    val filtered = reviews.filter { r ->
        val names = r.instructors.map { it.name }.ifEmpty { listOf(r.instructorName) }
        filters.courses.accepts(r.courseCode) && filters.instructors.acceptsAny(names) &&
            filters.grades.accepts(r.grade) && filters.terms.accepts(r.term) && filters.languages.accepts(r.language) &&
            filters.sessions.acceptsAny(r.instructors.map { it.session }) &&
            filters.requirements.acceptsAny(r.instructors.flatMap { it.requirements }) &&
            filters.serviceLearning.acceptsAny(r.instructors.map { it.serviceLearning }) &&
            (filters.search.isBlank() || (listOf(r.courseCode, r.courseTitle, r.courseTitleZh, r.comment, r.instructorName, r.instructorNameZh) +
                r.instructors.flatMap { listOf(it.name, it.comment, it.serviceDescription) }).any { it.contains(filters.search.trim(), true) })
    }
    if (filters.sort == ReviewSort.DATE) return filtered.sortedWith(
        if (filters.descending) compareByDescending<Review> { it.createdAt }.thenBy { it.id }
        else compareBy<Review> { it.createdAt }.thenBy { it.id })
    fun score(r: Review): Double = when (filters.sort) {
        ReviewSort.WORKLOAD -> r.workloadRating
        ReviewSort.DIFFICULTY -> r.difficultyRating
        ReviewSort.USEFULNESS -> r.usefulnessRating
        ReviewSort.TEACHING -> r.teachingRating
        ReviewSort.GRADING -> r.gradingRating
        ReviewSort.GRADE -> REVIEW_GRADES.indexOf(r.grade).let { if (it < 0 || r.grade == "-1") -1.0 else (REVIEW_GRADES.size - it).toDouble() }
        ReviewSort.UPVOTES -> r.upvotes.toDouble()
        ReviewSort.DOWNVOTES -> r.downvotes.toDouble()
        ReviewSort.DATE -> -1.0
    }
    return filtered.sortedWith(compareBy<Review> { score(it) < 0 || !score(it).isFinite() }.thenComparator { a, b ->
        val comparison = score(a).compareTo(score(b))
        if (filters.descending) -comparison else comparison
    }.thenBy { it.id })
}
