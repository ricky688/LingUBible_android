package com.lingubible.app.data.repository

import android.util.Log
import com.lingubible.app.data.remote.AppwriteClientProvider
import com.lingubible.app.domain.model.Course
import com.lingubible.app.domain.model.TeachingRecord
import com.lingubible.app.domain.repository.CourseRepository
import io.appwrite.Query
import java.util.Locale

class AppwriteCourseRepository(
    private val clientProvider: AppwriteClientProvider
) : CourseRepository {

    private val dbId = clientProvider.databaseId
    private val coursesCol = "courses"
    private val recordsCol = "teaching_records"
    private val reviewsCol = "reviews"
    private val TAG = "CourseRepo"

    override suspend fun getCourses(
        search: String?,
        subject: String?,
        page: Int
    ): Result<List<Course>> {
        val databases = clientProvider.databases
            ?: return Result.success(emptyList())

        Log.d(TAG, "getCourses called: databases=${databases != null}, dbId=$dbId, search=$search, subject=$subject")

        return try {
            val queries = mutableListOf<String>()
            queries.add(Query.orderAsc("course_code"))
            queries.add(Query.limit(50))
            queries.add(Query.offset((page - 1) * 50))

            if (!search.isNullOrBlank()) {
                val q = search.trim().uppercase()
                queries.add(Query.startsWith("course_code", q))
            } else if (!subject.isNullOrBlank()) {
                queries.add(Query.startsWith("course_code", subject.trim().uppercase()))
            }

            val response = databases.listDocuments(
                databaseId = dbId,
                collectionId = coursesCol,
                queries = queries
            )

            val courses = response.documents.map { doc ->
                mapDocumentToCourse(doc.id, doc.data)
            }

            Log.d(TAG, "Loaded ${courses.size} courses (subject: $subject, search: $search)")
            Result.success(courses)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching courses: ${e.message}", e)
            Result.failure(e)
        }
    }


    override suspend fun getCourseByCode(courseCode: String): Result<Course> {
        val databases = clientProvider.databases
            ?: return Result.failure(IllegalStateException("Databases not initialized"))

        return try {
            val response = databases.listDocuments(
                databaseId = dbId,
                collectionId = coursesCol,
                queries = listOf(Query.equal("course_code", courseCode.trim().uppercase()))
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
