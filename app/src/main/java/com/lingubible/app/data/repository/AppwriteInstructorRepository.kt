package com.lingubible.app.data.repository

import android.util.Log
import com.lingubible.app.data.remote.AppwriteClientProvider
import com.lingubible.app.domain.model.parseCourseStringList
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

    override suspend fun getAllInstructors(): Result<List<Instructor>> = try {
        val db = requireNotNull(clientProvider.databases)
        val result = mutableListOf<Instructor>()
        var cursor: String? = null
        do {
            val page = db.listDocuments(dbId, instructorsCol, buildList {
                add(Query.limit(100)); add(Query.orderAsc("\$id")); cursor?.let { add(Query.cursorAfter(it)) }
            }).documents
            result += page.map { mapDocumentToInstructor(it.id, it.data) }
            cursor = page.lastOrNull()?.id
        } while (page.size == 100)
        Result.success(result.distinctBy { it.id })
    } catch (e: kotlinx.coroutines.CancellationException) { throw e
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun getInstructorNamesForTerms(terms: Set<String>): Result<Set<String>> = try {
        require(terms.isNotEmpty())
        val db = requireNotNull(clientProvider.databases)
        val names = mutableSetOf<String>()
        var cursor: String? = null
        do {
            val page = db.listDocuments(dbId, recordsCol, buildList {
                add(Query.equal("term_code", terms.toList())); add(Query.select(listOf("instructor_name")))
                add(Query.limit(100)); add(Query.orderAsc("\$id")); cursor?.let { add(Query.cursorAfter(it)) }
            }).documents
            names += page.mapNotNull { it.data["instructor_name"] as? String }
            cursor = page.lastOrNull()?.id
        } while (page.size == 100)
        Result.success(names)
    } catch (e: kotlinx.coroutines.CancellationException) { throw e
    } catch (e: Exception) { Result.failure(e) }

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

    private var teachingContainsSupported = true
    private var fullHistoryCache: Pair<Long, List<TeachingRecord>>? = null

    override suspend fun getInstructorTeachingRecords(name: String): Result<List<TeachingRecord>> = try {
        require(name.isNotBlank())
        val records = if (teachingContainsSupported) {
            try { loadTeachingRecords(name) }
            catch (e: io.appwrite.exceptions.AppwriteException) {
                if (e.code != 400) throw e
                teachingContainsSupported = false
                cachedFullHistory()
            }
        } else cachedFullHistory()
        Result.success(records.filter { record -> record.instructorName.split('/').any { it.trim().equals(name.trim(), true) } }.map { it.copy(instructorName = name.trim()) })
    } catch (e: kotlinx.coroutines.CancellationException) { throw e
    } catch (e: Exception) { Result.failure(e) }

    private suspend fun cachedFullHistory(): List<TeachingRecord> {
        fullHistoryCache?.takeIf { System.currentTimeMillis() - it.first < 10 * 60 * 1000 }?.let { return it.second }
        return loadTeachingRecords(null).also { fullHistoryCache = System.currentTimeMillis() to it }
    }

    private suspend fun loadTeachingRecords(name: String?): List<TeachingRecord> {
        val databases = requireNotNull(clientProvider.databases)
            val documents = mutableListOf<io.appwrite.models.Document<Map<String, Any>>>()
            var cursor: String? = null
            do {
                val page = databases.listDocuments(dbId, recordsCol, buildList {
                    if (name != null) add(Query.contains("instructor_name", name.trim()))
                    add(Query.limit(100)); add(Query.orderAsc("\$id"))
                    cursor?.let { add(Query.cursorAfter(it)) }
                }).documents
                documents += page
                cursor = page.lastOrNull()?.id
            } while (page.size == 100)
            val records = documents.distinctBy { it.id }.map { doc ->
                val data = doc.data
                val termCode = (data["term_code"] as? String) ?: (data["term"] as? String) ?: ""
                val instructorNameZh = (data["instructor_name_tc"] as? String)?.trim()
                    ?: (data["instructor_name_zh"] as? String)?.trim()
                    ?: ""
                TeachingRecord(
                    id = doc.id,
                    courseCode = (data["course_code"] as? String) ?: "",
                    instructorName = (data["instructor_name"] as? String) ?: "",
                    instructorNameZh = instructorNameZh,
                    term = termCode,
                    academicYear = if (termCode.length >= 4) termCode.take(4) else "",
                    section = (data["session_type"] as? String) ?: (data["section"] as? String) ?: "",
                    teachingLanguage = (data["teaching_language"] as? String).orEmpty().trim()
                )
            }

        return records
    }

    private fun mapDocumentToInstructor(docId: String, data: Map<String, Any>): Instructor {
        val rawScore = (data["stats_teaching_score"] as? Number)?.toDouble()
            ?: (data["average_rating"] as? Number)?.toDouble() ?: -1.0

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
            avgRating = if (rawScore >= 0) rawScore else -1.0,
            teachingSession = "Lecture",
            avgGrading = (data["stats_grading_fairness"] as? Number)?.toDouble() ?: -1.0,
            avgGpa = if ((data["stats_avg_gpa_count"] as? Number)?.toInt() == 0) -1.0 else (data["stats_avg_gpa"] as? Number)?.toDouble() ?: -1.0,
            teachingLanguages = parseCourseStringList(data["teaching_languages"]),
            isCurrentStaff = data["is_current_staff"] as? Boolean ?: true,
            nickname = data["nickname"] as? String ?: ""
        )
    }
}
