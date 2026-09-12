package com.lingubible.app.data.repository

import android.util.Log
import com.lingubible.app.data.remote.AppwriteClientProvider
import com.lingubible.app.domain.model.Instructor
import com.lingubible.app.domain.model.TeachingRecord
import com.lingubible.app.domain.repository.InstructorRepository
import io.appwrite.Query

class AppwriteInstructorRepository(
    private val clientProvider: AppwriteClientProvider
) : InstructorRepository {

    private val dbId = clientProvider.databaseId
    private val instructorsCol = "instructors"
    private val recordsCol = "teaching_records"
    private val TAG = "InstructorRepo"

    override suspend fun getInstructors(
        search: String?,
        department: String?,
        page: Int
    ): Result<List<Instructor>> {
        val databases = clientProvider.databases
            ?: return Result.success(emptyList())

        return try {
            val queries = mutableListOf<String>()
            queries.add(Query.orderAsc("name"))
            queries.add(Query.limit(50))
            queries.add(Query.offset((page - 1) * 50))

            if (!search.isNullOrBlank()) {
                val trimmed = search.trim()
                val hasChinese = trimmed.any { it in '\u4e00'..'\u9fa5' }
                if (hasChinese) {
                    queries.add(Query.startsWith("name_tc", trimmed))
                } else {
                    queries.add(Query.startsWith("name", trimmed))
                }
            }
            if (!department.isNullOrBlank()) {
                queries.add(Query.startsWith("department", department.trim()))
            }

            val response = databases.listDocuments(
                databaseId = dbId,
                collectionId = instructorsCol,
                queries = queries
            )

            val instructors = response.documents.map { doc ->
                mapDocumentToInstructor(doc.id, doc.data)
            }

            Log.d(TAG, "Loaded ${instructors.size} instructors (department: $department, search: $search)")
            Result.success(instructors)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching instructors: ${e.message}", e)
            Result.failure(e)
        }
    }


    override suspend fun getInstructorByName(name: String): Result<Instructor> {
        val databases = clientProvider.databases
            ?: return Result.failure(IllegalStateException("Databases not initialized"))

        return try {
            val response = databases.listDocuments(
                databaseId = dbId,
                collectionId = instructorsCol,
                queries = listOf(Query.equal("name", name.trim()))
            )

            val doc = response.documents.firstOrNull()
                ?: return Result.failure(NoSuchElementException("Instructor not found: $name"))

            Result.success(mapDocumentToInstructor(doc.id, doc.data))
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching instructor detail for $name: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun getInstructorTeachingRecords(name: String): Result<List<TeachingRecord>> {
        val databases = clientProvider.databases
            ?: return Result.success(emptyList())

        return try {
            val response = databases.listDocuments(
                databaseId = dbId,
                collectionId = recordsCol,
                queries = listOf(
                    Query.equal("instructor_name", name.trim()),
                    Query.limit(100)
                )
            )

            val records = response.documents.map { doc ->
                val data = doc.data
                val termCode = (data["term_code"] as? String) ?: (data["term"] as? String) ?: ""
                val instructorNameZh = (data["instructor_name_tc"] as? String)?.trim()
                    ?: (data["instructor_name_zh"] as? String)?.trim()
                    ?: ""
                TeachingRecord(
                    id = doc.id,
                    courseCode = (data["course_code"] as? String) ?: "",
                    instructorName = (data["instructor_name"] as? String) ?: name,
                    instructorNameZh = instructorNameZh,
                    term = termCode,
                    academicYear = if (termCode.length >= 4) termCode.take(4) else "",
                    section = (data["session_type"] as? String) ?: (data["section"] as? String) ?: ""
                )
            }

            Result.success(records)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching instructor teaching records for $name: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun mapDocumentToInstructor(docId: String, data: Map<String, Any>): Instructor {
        val rawScore = (data["stats_teaching_score"] as? Number)?.toDouble()
            ?: (data["average_rating"] as? Number)?.toDouble() ?: 0.0

        val name = (data["name"] as? String)?.trim() ?: docId
        val nameZh = (data["name_tc"] as? String)?.trim()
            ?: (data["name_sc"] as? String)?.trim()
            ?: (data["name_zh"] as? String)?.trim()
            ?: ""
        val title = (data["title"] as? String)?.trim() ?: ""

        return Instructor(
            id = docId,
            name = name,
            nameZh = nameZh,
            department = (data["department"] as? String) ?: "",
            title = title,
            reviewCount = (data["stats_review_count"] as? Number)?.toInt()
                ?: (data["review_count"] as? Number)?.toInt() ?: 0,
            avgRating = if (rawScore > 0) rawScore else 0.0,
            teachingSession = "Lecture"
        )
    }
}
