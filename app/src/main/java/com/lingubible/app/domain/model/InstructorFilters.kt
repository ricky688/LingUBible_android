package com.lingubible.app.domain.model

data class InstructorFilters(
    val departments: Set<String> = emptySet(), val terms: Set<String> = emptySet(),
    val languages: Set<String> = emptySet(), val showFormerStaff: Boolean = false,
    val sort: InstructorSort = InstructorSort.NAME, val descending: Boolean = false
)
enum class InstructorSort(val label: String) {
    NAME("姓名 Name"), DEPARTMENT("學系 Department"), TEACHING("教學 Teaching"),
    GRADING("給分 Grading"), REVIEWS("評價數 Reviews"), GPA("平均 GPA")
}
fun filterInstructors(source: List<Instructor>, search: String, filters: InstructorFilters, termNames: Set<String> = emptySet()): List<Instructor> {
    val q = search.trim()
    val filtered = source.filter { instructor ->
        (filters.showFormerStaff || instructor.isCurrentStaff) &&
            (filters.departments.isEmpty() || instructor.department in filters.departments) &&
            (filters.languages.isEmpty() || instructor.teachingLanguages.any { it in filters.languages }) &&
            (filters.terms.isEmpty() || instructor.name in termNames) &&
            (q.isEmpty() || listOf(instructor.name, instructor.nameZh, instructor.nickname, instructor.department).any { it.contains(q, true) })
    }
    val sort = filters.sort
    if (sort == InstructorSort.NAME || sort == InstructorSort.DEPARTMENT) {
        val comparator = compareBy<Instructor> { if (sort == InstructorSort.NAME) it.name.lowercase() else it.department.lowercase() }.thenBy { it.name }
        return filtered.sortedWith(if (filters.descending) comparator.reversed() else comparator)
    }
    fun score(i: Instructor) = when (sort) {
        InstructorSort.TEACHING -> i.avgRating
        InstructorSort.GRADING -> i.avgGrading
        InstructorSort.REVIEWS -> i.reviewCount.toDouble()
        InstructorSort.GPA -> i.avgGpa
        else -> -1.0
    }
    return filtered.sortedWith(compareBy<Instructor> { score(it) < 0 || !score(it).isFinite() }.thenComparator { a, b ->
        val comparison = score(a).compareTo(score(b)); if (filters.descending) -comparison else comparison
    }.thenBy { it.name })
}
