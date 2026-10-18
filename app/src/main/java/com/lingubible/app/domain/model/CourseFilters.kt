package com.lingubible.app.domain.model

import kotlinx.serialization.json.*

data class CourseFilters(
    val subjects: Set<String> = emptySet(),
    val languages: Set<String> = emptySet(),
    val serviceLearning: Set<String> = emptySet(),
    val terms: Set<String> = emptySet(),
    val hasMaterials: Boolean = false,
    val sort: CourseSort = CourseSort.CODE,
    val descending: Boolean = false
)
enum class CourseSort(val label: String) {
    CODE("代碼 Code"), TITLE("名稱 Title"), DEPARTMENT("學系 Department"),
    WORKLOAD("工作量 Workload"), DIFFICULTY("難度 Difficulty"),
    USEFULNESS("實用性 Usefulness"), REVIEWS("評價數 Reviews"), GPA("平均 GPA")
}
data class CourseTerm(val code: String, val label: String)

fun parseCourseStringList(value: Any?): List<String> = when (value) {
    is List<*> -> value.filterIsInstance<String>()
    is String -> runCatching { Json.parseToJsonElement(value).jsonArray.map { it.jsonPrimitive.content } }.getOrDefault(emptyList())
    else -> emptyList()
}

fun filterCourses(courses: List<Course>, filters: CourseFilters, termCodes: Set<String>?, materialCodes: Set<String>?): List<Course> {
    val filtered = courses.filter { course ->
        (filters.subjects.isEmpty() || course.code.takeWhile { !it.isDigit() }.uppercase() in filters.subjects) &&
            (filters.languages.isEmpty() || course.teachingLanguages.any { it in filters.languages }) &&
            (filters.serviceLearning.isEmpty() || filters.serviceLearning.any {
                if (it == "none") course.serviceLearningTypes.isEmpty() else it in course.serviceLearningTypes
            }) &&
            (filters.terms.isEmpty() || course.code.uppercase() in termCodes.orEmpty()) &&
            (!filters.hasMaterials || course.code.uppercase() in materialCodes.orEmpty())
    }
    fun metric(course: Course): Double? = when (filters.sort) {
        CourseSort.WORKLOAD -> course.avgWorkload.takeIf { it > 0 }
        CourseSort.DIFFICULTY -> course.avgDifficulty.takeIf { it > 0 }
        CourseSort.USEFULNESS -> course.avgUsefulness.takeIf { it > 0 }
        CourseSort.GPA -> course.avgGpa.takeIf { it >= 0 }
        CourseSort.REVIEWS -> course.reviewCount.toDouble()
        else -> null
    }
    return filtered.sortedWith { a, b ->
        val numeric = filters.sort in setOf(CourseSort.WORKLOAD, CourseSort.DIFFICULTY, CourseSort.USEFULNESS, CourseSort.GPA, CourseSort.REVIEWS)
        val am = metric(a); val bm = metric(b)
        if (numeric && am == null && bm != null) 1
        else if (numeric && bm == null && am != null) -1
        else {
            val comparison = if (numeric) compareValues(am, bm) else when (filters.sort) {
                CourseSort.TITLE -> a.titleEn.compareTo(b.titleEn, ignoreCase = true)
                CourseSort.DEPARTMENT -> a.department.compareTo(b.department, ignoreCase = true)
                else -> a.code.compareTo(b.code, ignoreCase = true)
            }
            val ordered = if (filters.descending) -comparison else comparison
            if (ordered == 0) a.code.compareTo(b.code, ignoreCase = true) else ordered
        }
    }
}

fun teachingLanguageLabel(code: String): String = when (code) {
    "E" -> "English"
    "C" -> "Cantonese"
    "P" -> "Putonghua"
    "1" -> "English / Cantonese"
    "2" -> "English / Putonghua"
    "3" -> "Cantonese / Putonghua"
    "4" -> "English / Cantonese / Putonghua"
    "5" -> "Other"
    else -> code
}
