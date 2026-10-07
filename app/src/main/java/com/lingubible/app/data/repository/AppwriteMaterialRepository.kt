package com.lingubible.app.data.repository

import com.lingubible.app.data.remote.AppwriteClientProvider
import com.lingubible.app.domain.model.CourseSyllabus
import com.lingubible.app.domain.model.PastPaper
import com.lingubible.app.domain.repository.MaterialRepository
import com.lingubible.app.domain.util.PastPapersParser
import io.appwrite.Query

class AppwriteMaterialRepository(
    private val clientProvider: AppwriteClientProvider
) : MaterialRepository {

    private val pastPapersBucketId = "past_exam_papers"
    private val syllabusBucketId = "course_syllabus"
    private val endpoint = clientProvider.appConfig.endpoint
    private val projectId = clientProvider.appConfig.projectId

    override suspend fun getPastPapers(courseCode: String): Result<List<PastPaper>> {
        val storage = clientProvider.storage
            ?: return Result.success(emptyList())

        val codeUpper = courseCode.trim().uppercase()
        if (codeUpper.isBlank()) return Result.success(emptyList())

        return try {
            val allPapers = mutableListOf<PastPaper>()
            val pageSize = 100
            var offset = 0
            val maxSafetyCap = 5000
            var useSearchParam = true

            while (offset < maxSafetyCap) {
                val queries = listOf(
                    Query.limit(pageSize),
                    Query.offset(offset)
                )

                val fileList = try {
                    if (useSearchParam) {
                        storage.listFiles(
                            bucketId = pastPapersBucketId,
                            queries = queries,
                            search = codeUpper
                        )
                    } else {
                        storage.listFiles(
                            bucketId = pastPapersBucketId,
                            queries = queries
                        )
                    }
                } catch (e: Exception) {
                    if (useSearchParam && offset == 0) {
                        useSearchParam = false
                        storage.listFiles(
                            bucketId = pastPapersBucketId,
                            queries = queries
                        )
                    } else {
                        throw e
                    }
                }

                val files = fileList.files
                for (file in files) {
                    val nameLower = file.name.lowercase()
                    if (!nameLower.endsWith(".pdf")) continue
                    val codeLower = codeUpper.lowercase()
                    val matchesPrefix = nameLower.startsWith("${codeLower}_") ||
                            nameLower.startsWith("${codeLower}-") ||
                            nameLower.startsWith("${codeLower}.") ||
                            nameLower.startsWith("${codeLower} ")
                    val parsed = PastPapersParser.parseFilename(file.name)

                    if (matchesPrefix || (parsed != null && parsed.courseCode.equals(codeUpper, ignoreCase = true))) {
                        allPapers.add(
                            PastPaper(
                                id = file.id,
                                courseCode = parsed?.courseCode ?: codeUpper,
                                academicYear = parsed?.academicYear ?: "Unknown",
                                term = parsed?.term ?: "Unknown",
                                instructorName = parsed?.instructor,
                                fileId = file.id,
                                fileName = file.name,
                                fileSize = file.sizeOriginal,
                                downloadUrl = getDownloadUrl(file.id),
                                viewUrl = getViewUrl(file.id)
                            )
                        )
                    }
                }

                if (files.size < pageSize) break
                offset += pageSize
            }

            val sortedPapers = allPapers.distinctBy { it.id }.sortedWith(
                compareByDescending<PastPaper> { PastPapersParser.getTermSortKey(it.academicYear, it.term) }
                    .thenBy { it.fileName }
            )
            Result.success(sortedPapers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSyllabus(courseCode: String): Result<CourseSyllabus?> {
        val storage = clientProvider.storage
            ?: return Result.success(null)

        val codeTrimmed = courseCode.trim()
        if (codeTrimmed.isBlank()) return Result.success(null)

        return try {
            val prefix = codeTrimmed.lowercase()
            val pageSize = 100
            var offset = 0
            val maxSafetyCap = 5000
            var useSearchParam = true
            var bestFile: io.appwrite.models.File? = null
            var bestSuffix = -1L

            while (offset < maxSafetyCap) {
                val queries = listOf(
                    Query.limit(pageSize),
                    Query.offset(offset)
                )

                val fileList = try {
                    if (useSearchParam) {
                        storage.listFiles(
                            bucketId = syllabusBucketId,
                            queries = queries,
                            search = codeTrimmed
                        )
                    } else {
                        storage.listFiles(
                            bucketId = syllabusBucketId,
                            queries = queries
                        )
                    }
                } catch (e: Exception) {
                    if (useSearchParam && offset == 0) {
                        useSearchParam = false
                        storage.listFiles(
                            bucketId = syllabusBucketId,
                            queries = queries
                        )
                    } else {
                        throw e
                    }
                }

                val files = fileList.files
                for (f in files) {
                    val nameLower = f.name.lowercase()
                    if (!nameLower.endsWith(".pdf")) continue
                    if (!nameLower.startsWith(prefix)) continue
                    val remainder = nameLower.removePrefix(prefix)
                    if (remainder.isNotEmpty() && !remainder.startsWith("-") && !remainder.startsWith("_") && !remainder.startsWith(".") && !remainder.startsWith(" ")) {
                        continue
                    }

                    val stem = f.name.substringBeforeLast(".")
                    val remainderStem = stem.substring(prefix.length).trimStart('-', '_', ' ')
                    val trailingDigitsMatch = "(\\d+)\\s*$".toRegex().find(remainderStem)
                    val suffix = trailingDigitsMatch?.value?.trim()?.toLongOrNull() ?: if (remainderStem.isBlank()) 0L else -1L

                    if (bestFile == null || suffix > bestSuffix) {
                        bestFile = f
                        bestSuffix = suffix
                    }
                }

                if (files.size < pageSize) break
                offset += pageSize
            }

            if (bestFile != null) {
                val syllabus = CourseSyllabus(
                    id = bestFile.id,
                    courseCode = codeTrimmed.uppercase(),
                    fileName = bestFile.name,
                    viewUrl = getSyllabusViewUrl(bestFile.id),
                    downloadUrl = getSyllabusDownloadUrl(bestFile.id),
                    fileSize = bestFile.sizeOriginal
                )
                Result.success(syllabus)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDownloadUrl(fileId: String): String {
        return "$endpoint/storage/buckets/$pastPapersBucketId/files/$fileId/download?project=$projectId"
    }

    override suspend fun getViewUrl(fileId: String): String {
        return "$endpoint/storage/buckets/$pastPapersBucketId/files/$fileId/view?project=$projectId"
    }

    override suspend fun getSyllabusViewUrl(fileId: String): String {
        return "$endpoint/storage/buckets/$syllabusBucketId/files/$fileId/view?project=$projectId"
    }

    override suspend fun getSyllabusDownloadUrl(fileId: String): String {
        return "$endpoint/storage/buckets/$syllabusBucketId/files/$fileId/download?project=$projectId"
    }

    override suspend fun downloadFileBytes(bucketId: String, fileId: String): Result<ByteArray> {
        val storage = clientProvider.storage
            ?: return Result.failure(IllegalStateException("Storage service not available"))
        return try {
            val bytes = storage.getFileDownload(bucketId = bucketId, fileId = fileId)
            Result.success(bytes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

