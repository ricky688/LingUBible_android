package com.lingubible.app.data.repository

import android.util.Log
import com.lingubible.app.data.remote.AppwriteClientProvider
import com.lingubible.app.domain.model.Course
import com.lingubible.app.domain.model.TeachingRecord
import com.lingubible.app.domain.repository.CourseRepository
import io.appwrite.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.Locale

class AppwriteCourseRepository(
    private val clientProvider: AppwriteClientProvider
) : CourseRepository {

    private val dbId = clientProvider.databaseId
    private val coursesCol = "courses"
    private val recordsCol = "teaching_records"
    private val reviewsCol = "reviews"
    private val TAG = "CourseRepo"

    private val cacheMutex = Mutex()
    private var allCoursesCache: List<Course>? = null

    private suspend fun getAllCourses(): List<Course> = withContext(Dispatchers.IO) {
        cacheMutex.withLock {
            allCoursesCache?.let { return@withLock it }
            val databases = clientProvider.databases ?: return@withLock emptyList<Course>()

            try {
                val pageSize = 100
                val totalExpected = 1100
                val numPages = (totalExpected + pageSize - 1) / pageSize

                val fetched = coroutineScope {
                    (0 until numPages).map { pageIdx ->
                        async {
                            try {
                                val response = databases.listDocuments(
                                    databaseId = dbId,
                                    collectionId = coursesCol,
                                    queries = listOf(
                                        Query.orderAsc("course_code"),
                                        Query.limit(pageSize),
                                        Query.offset(pageIdx * pageSize)
                                    )
                                )
                                response.documents.map { doc ->
                                    mapDocumentToCourse(doc.id, doc.data)
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error fetching courses page $pageIdx: ${e.message}", e)
                                emptyList()
                            }
                        }
                    }.awaitAll().flatten().distinctBy { it.code }
                }

                if (fetched.isNotEmpty()) {
                    allCoursesCache = fetched
                    Log.d(TAG, "Cached ${fetched.size} courses in memory")
                    return@withLock fetched
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch all courses: ${e.message}", e)
            }
            emptyList<Course>()
        }
    }

    override suspend fun getCourses(
        search: String?,
        subject: String?,
        page: Int
    ): Result<List<Course>> {
        val databases = clientProvider.databases
            ?: return Result.success(emptyList())

        Log.d(TAG, "getCourses called: search=$search, subject=$subject")

        return try {
            val allCourses = getAllCourses()
            if (allCourses.isEmpty()) {
                val queries = mutableListOf<String>()
                queries.add(Query.orderAsc("course_code"))
                queries.add(Query.limit(100))
                if (!search.isNullOrBlank()) {
                    queries.add(Query.startsWith("course_code", search.trim().uppercase()))
                } else if (!subject.isNullOrBlank() && !subject.equals("ALL", ignoreCase = true)) {
                    queries.add(Query.startsWith("course_code", subject.trim().uppercase()))
                }
                val response = databases.listDocuments(dbId, coursesCol, queries)
                val courses = response.documents.map { mapDocumentToCourse(it.id, it.data) }
                return Result.success(courses)
            }

            var filtered = allCourses

            if (!subject.isNullOrBlank() && !subject.equals("ALL", ignoreCase = true)) {
                val s = subject.trim().uppercase()
                filtered = filtered.filter { course ->
                    val codeUpper = course.code.uppercase()
                    val deptUpper = course.department.uppercase()
                    if (s == "CCC") {
                        codeUpper.startsWith("CCC") || deptUpper.contains("COMMON CORE")
                    } else {
                        codeUpper.startsWith(s) || deptUpper == s
                    }
                }
            }

            if (!search.isNullOrBlank()) {
                val q = search.trim()
                filtered = filtered.filter { course ->
                    course.code.contains(q, ignoreCase = true) ||
                    course.titleEn.contains(q, ignoreCase = true) ||
                    course.titleZh.contains(q, ignoreCase = true) ||
                    course.department.contains(q, ignoreCase = true) ||
                    course.description.contains(q, ignoreCase = true)
                }
            }

            Log.d(TAG, "Loaded ${filtered.size} courses (subject: $subject, search: $search)")
            Result.success(filtered)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching courses: ${e.message}", e)
            Result.failure(e)
        }
    }


    override suspend fun getCourseByCode(courseCode: String): Result<Course> {
        val trimmed = courseCode.trim().uppercase()
        allCoursesCache?.find { it.code.trim().uppercase() == trimmed }?.let {
            return Result.success(it)
        }

        val databases = clientProvider.databases
            ?: return Result.failure(IllegalStateException("Databases not initialized"))

        return try {
            val response = databases.listDocuments(
                databaseId = dbId,
                collectionId = coursesCol,
                queries = listOf(Query.equal("course_code", trimmed))
            )

            val doc = response.documents.firstOrNull()
                ?: return Result.failure(NoSuchElementException("Course not found: $courseCode"))

            Result.success(mapDocumentToCourse(doc.id, doc.data))
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching course detail for $courseCode: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun getTeachingRecords(courseCode: String): Result<List<TeachingRecord>> {
        val databases = clientProvider.databases
            ?: return Result.success(emptyList())

        return try {
            val response = databases.listDocuments(
                databaseId = dbId,
                collectionId = recordsCol,
                queries = listOf(
                    Query.equal("course_code", courseCode.trim().uppercase()),
                    Query.limit(100)
                )
            )

            val records = response.documents.map { doc ->
                val data = doc.data
                val termCode = (data["term_code"] as? String) ?: (data["term"] as? String) ?: ""
                TeachingRecord(
                    id = doc.id,
                    courseCode = (data["course_code"] as? String) ?: "",
                    instructorName = (data["instructor_name"] as? String) ?: "",
                    term = termCode,
                    academicYear = if (termCode.length >= 4) termCode.take(4) else "",
                    section = (data["session_type"] as? String) ?: (data["section"] as? String) ?: ""
                )
            }

            Result.success(records)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching teaching records for $courseCode: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun getGradeDistribution(courseCode: String): Result<Map<String, Int>> {
        val databases = clientProvider.databases
            ?: return Result.success(emptyMap())

        return try {
            val response = databases.listDocuments(
                databaseId = dbId,
                collectionId = reviewsCol,
                queries = listOf(
                    Query.equal("course_code", courseCode.trim().uppercase()),
                    Query.limit(500)
                )
            )

            val distribution = mutableMapOf<String, Int>()
            listOf("A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D+", "D", "F").forEach {
                distribution[it] = 0
            }

            response.documents.forEach { doc ->
                val grade = (doc.data["course_final_grade"] as? String ?: doc.data["grade"] as? String)?.trim()?.uppercase()
                if (!grade.isNullOrBlank() && distribution.containsKey(grade)) {
                    distribution[grade] = (distribution[grade] ?: 0) + 1
                }
            }

            Result.success(distribution)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching grade distribution for $courseCode: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun mapDocumentToCourse(docId: String, data: Map<String, Any>): Course {
        val rawRating = (data["stats_avg_rating"] as? Number)?.toDouble() ?: 0.0
        val rawWorkload = (data["stats_avg_workload"] as? Number)?.toDouble() ?: 0.0
        val rawDifficulty = (data["stats_avg_difficulty"] as? Number)?.toDouble() ?: 0.0
        val rawGpa = (data["stats_avg_gpa"] as? Number)?.toDouble() ?: 0.0

        val avgGrade = if (rawGpa > 0) {
            String.format(Locale.US, "%.2f", rawGpa)
        } else {
            (data["average_grade"] as? String) ?: "N/A"
        }

        val credits = when (val c = data["credits"]) {
            is Number -> c.toInt()
            is String -> c.toIntOrNull() ?: 3
            else -> 3
        }

        return Course(
            id = docId,
            code = (data["course_code"] as? String) ?: (data["code"] as? String) ?: docId,
            titleEn = (data["course_title"] as? String) ?: (data["title_en"] as? String) ?: (data["title"] as? String) ?: "",
            titleZh = (data["course_title_tc"] as? String) ?: (data["course_title_sc"] as? String) ?: (data["title_zh"] as? String) ?: "",
            department = (data["department"] as? String) ?: "",
            credits = credits,
            description = (data["course_description"] as? String) ?: (data["description"] as? String) ?: "",
            reviewCount = (data["stats_review_count"] as? Number)?.toInt() ?: (data["review_count"] as? Number)?.toInt() ?: 0,
            avgRating = if (rawRating > 0) rawRating else 0.0,
            avgWorkload = if (rawWorkload > 0) rawWorkload else 0.0,
            avgDifficulty = if (rawDifficulty > 0) rawDifficulty else 0.0,
            avgGrade = avgGrade
        )
    }
}
