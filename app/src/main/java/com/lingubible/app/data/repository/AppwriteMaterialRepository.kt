package com.lingubible.app.data.repository

import com.lingubible.app.data.remote.AppwriteClientProvider
import com.lingubible.app.domain.model.PastPaper
import com.lingubible.app.domain.repository.MaterialRepository
import com.lingubible.app.domain.util.PastPapersParser

class AppwriteMaterialRepository(
    private val clientProvider: AppwriteClientProvider
) : MaterialRepository {

    private val bucketId = "past_exam_papers"
    private val endpoint = clientProvider.appConfig.endpoint
    private val projectId = clientProvider.appConfig.projectId

    override suspend fun getPastPapers(courseCode: String): Result<List<PastPaper>> {
        val storage = clientProvider.storage
            ?: return Result.success(emptyList())

        return try {
            val fileList = storage.listFiles(bucketId = bucketId)
            val codeUpper = courseCode.trim().uppercase()

            val papers = fileList.files
                .filter { it.name.startsWith(codeUpper, ignoreCase = true) }
                .map { file ->
                    val parsed = PastPapersParser.parseFilename(file.name)
                    PastPaper(
                        id = file.id,
                        courseCode = parsed?.courseCode ?: codeUpper,
                        academicYear = parsed?.academicYear ?: "Unknown",
                        term = parsed?.term ?: "Term 1",
                        instructorName = parsed?.instructor,
                        fileId = file.id,
                        fileName = file.name,
                        fileSize = file.sizeOriginal,
                        downloadUrl = getDownloadUrl(file.id),
                        viewUrl = getViewUrl(file.id)
                    )
                }

            Result.success(papers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDownloadUrl(fileId: String): String {
        return "$endpoint/storage/buckets/$bucketId/files/$fileId/download?project=$projectId"
    }

    override suspend fun getViewUrl(fileId: String): String {
        return "$endpoint/storage/buckets/$bucketId/files/$fileId/view?project=$projectId"
    }
}
