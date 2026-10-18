package com.lingubible.app.data.repository

import android.util.Log
import com.lingubible.app.data.remote.AppwriteClientProvider
import com.lingubible.app.domain.model.EligibilityResult
import com.lingubible.app.domain.model.Review
import com.lingubible.app.domain.model.ReviewEdit
import com.lingubible.app.domain.model.ReviewSubmission
import com.lingubible.app.domain.model.VoteState
import com.lingubible.app.domain.model.InstructorEvaluation
import com.lingubible.app.domain.repository.ReviewRepository
import io.appwrite.ID
import io.appwrite.Query
import org.json.JSONObject
import kotlinx.serialization.json.jsonObject

class AppwriteReviewRepository(
    private val clientProvider: AppwriteClientProvider,
    private val authRepository: com.lingubible.app.domain.repository.AuthRepository? = null,
    private val courseRepository: com.lingubible.app.domain.repository.CourseRepository? = null
) : ReviewRepository {

    private val dbId = clientProvider.databaseId
    private val reviewsCol = "reviews"
    private val votesCol = "review_votes"
    private val TAG = "ReviewRepo"

    private suspend fun ownedReview(reviewId: String): io.appwrite.models.Document<Map<String, Any>> {
        require(reviewId.isNotBlank())
        val userId = requireNotNull(clientProvider.account) { "Sign in required" }.get().id
        val document = requireNotNull(clientProvider.databases).getDocument(dbId, reviewsCol, reviewId)
        check(document.data["user_id"] == userId) { "你只能管理自己的評價 · You can only manage your own reviews" }
        return document
    }

    override suspend fun getReviewEdit(reviewId: String): Result<ReviewEdit> = try {
        Result.success(ReviewEdit.from(ownedReview(reviewId).data))
    } catch (e: kotlinx.coroutines.CancellationException) { throw e
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun updateReview(reviewId: String, edit: ReviewEdit): Result<Review> = try {
        val original = ownedReview(reviewId)
        val originalDetails = ReviewEdit.from(original.data).instructors
        // Keep instructor identities, sessions and unknown fields from the saved review.
        require(edit.instructors.size == originalDetails.size) { "Instructor list changed; reopen the review" }
        edit.instructors.forEachIndexed { index, value ->
            val originalDetail = originalDetails[index].jsonObject
            val detail = value.jsonObject
            originalDetail.keys.filterNot { it in setOf("grading", "teaching", "comments") }.forEach {
                require(detail[it] == originalDetail[it]) { "Instructor identity changed" }
            }
        }
        val doc = requireNotNull(clientProvider.databases).updateDocument(dbId, reviewsCol, reviewId, edit.patch())
        recomputeStats(original.data["course_code"] as? String ?: "")
        Result.success(mapDocumentToReview(doc.id, doc.data, doc.createdAt))
    } catch (e: kotlinx.coroutines.CancellationException) { throw e
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun deleteReview(reviewId: String): Result<Unit> = try {
        val original = ownedReview(reviewId)
        val db = requireNotNull(clientProvider.databases)
        // Match web cleanup before deleting the review; never silently ignore a permission failure.
        while (true) {
            val votes = db.listDocuments(dbId, votesCol, listOf(Query.equal("review_id", reviewId), Query.limit(100))).documents
            votes.forEach { db.deleteDocument(dbId, votesCol, it.id) }
            if (votes.size < 100) break
        }
        db.deleteDocument(dbId, reviewsCol, reviewId)
        recomputeStats(original.data["course_code"] as? String ?: "")
        Result.success(Unit)
    } catch (e: kotlinx.coroutines.CancellationException) { throw e
    } catch (e: Exception) { Result.failure(e) }

    private suspend fun recomputeStats(courseCode: String) {
        try {
            clientProvider.functions?.createExecution("recompute-course-stats", JSONObject().put("courseCode", courseCode).toString(), true)
            clientProvider.functions?.createExecution("recompute-course-stats", "{\"instructors\":true}", true)
        } catch (e: kotlinx.coroutines.CancellationException) { throw e
        } catch (e: Exception) { Log.w(TAG, "Statistics refresh deferred", e) }
    }

    override suspend fun getReviewsForCourse(courseCode: String): Result<List<Review>> =
        getAllReviews(listOf(Query.equal("course_code", courseCode.trim().uppercase())))

    override suspend fun getReviewsForInstructor(instructorName: String): Result<List<Review>> =
        getAllReviewsForBrowsing().map { reviews -> reviews.filter { review ->
            review.instructors.any { it.name.equals(instructorName.trim(), true) } ||
                review.instructorName.equals(instructorName.trim(), true)
        } }

    override suspend fun getLatestReviews(limit: Int): Result<List<Review>> =
        attachVotes(getReviews(listOf(Query.orderDesc("\$createdAt"), Query.limit(limit))))

    override suspend fun getAllReviewsForBrowsing(): Result<List<Review>> = getAllReviews(emptyList())

    override suspend fun getReviewsForUser(userId: String): Result<List<Review>> {
        require(userId.isNotBlank()) { "A signed-in user is required" }
        return getAllReviews(listOf(Query.equal("user_id", userId)))
    }

    private suspend fun getAllReviews(filters: List<String>): Result<List<Review>> {
        val reviews = mutableListOf<Review>()
        var cursor: String? = null
        do {
            val result = getReviews(buildList {
                addAll(filters); add(Query.orderDesc("\$createdAt")); add(Query.limit(100))
                cursor?.let { add(Query.cursorAfter(it)) }
            })
            if (result.isFailure) return result
            val page = result.getOrThrow()
            reviews += page
            cursor = page.lastOrNull()?.id
        } while (page.size == 100)
        return attachVotes(Result.success(reviews.distinctBy { it.id }))
    }

    private suspend fun readVotes(reviewId: String? = null): List<io.appwrite.models.Document<Map<String, Any>>> {
        val db = requireNotNull(clientProvider.databases)
        val votes = mutableListOf<io.appwrite.models.Document<Map<String, Any>>>()
        var cursor: String? = null
        do {
            val page = db.listDocuments(dbId, votesCol, buildList {
                reviewId?.let { add(Query.equal("review_id", it)) }
                add(Query.limit(100)); add(Query.orderAsc("\$id"))
                add(Query.select(listOf("\$id", "review_id", "vote_type", "user_id")))
                cursor?.let { add(Query.cursorAfter(it)) }
            }).documents
            votes += page; cursor = page.lastOrNull()?.id
        } while (page.size == 100)
        return votes
    }

    private suspend fun attachVotes(result: Result<List<Review>>): Result<List<Review>> {
        if (result.isFailure || result.getOrThrow().isEmpty()) return result
        return try {
            val catalog = courseRepository?.getCourses()?.getOrThrow()?.associateBy { it.code }.orEmpty()
            val owner = authRepository?.currentUser?.value?.id
            val grouped = readVotes().groupBy { it.data["review_id"] as? String }
            Result.success(result.getOrThrow().map { review ->
                val votes = grouped[review.id].orEmpty()
                val course = catalog[review.courseCode]
                review.copy(courseTitle = course?.titleEn ?: review.courseTitle, courseTitleZh = course?.titleZh ?: review.courseTitleZh,
                    upvotes = votes.count { it.data["vote_type"] == "up" },
                    downvotes = votes.count { it.data["vote_type"] == "down" },
                    userVote = if (owner == null) null else votes.firstOrNull { it.data["user_id"] == owner }?.data?.get("vote_type") as? String)
            })
        } catch (e: kotlinx.coroutines.CancellationException) { throw e
        } catch (e: Exception) { Result.failure(e) }
    }

    private suspend fun getReviews(queries: List<String>): Result<List<Review>> {
        val databases = clientProvider.databases
            ?: return Result.success(emptyList())

        Log.d(TAG, "Fetching reviews")

        return try {
            val response = databases.listDocuments(
                databaseId = dbId,
                collectionId = reviewsCol,
                queries = queries
            )

            val reviews = response.documents.map { doc ->
                mapDocumentToReview(doc.id, doc.data, doc.createdAt)
            }

            Log.d(TAG, "Loaded ${reviews.size} reviews")
            Result.success(reviews)
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching reviews: ${e.message}", e)
            Result.failure(e)
        }
    }


    override suspend fun submitReview(submission: ReviewSubmission): Result<Review> = try {
        val form = submission.form
        form.validate()
        val db = requireNotNull(clientProvider.databases) { "Databases not initialized" }
        val user = requireNotNull(clientProvider.account) { "Sign in required" }.get()
        require(user.id == submission.userId) { "帳戶已變更，請重新開啟表單 · Account changed; reopen the form" }
        // Recheck catalogue membership and eligibility at the write boundary, including restored drafts.
        val records = (courseRepository ?: AppwriteCourseRepository(clientProvider)).getTeachingRecords(form.courseCode).getOrThrow()
        require(form.instructors.all { evaluation -> records.any {
            it.term == form.term && it.instructorName == evaluation.name && it.section == evaluation.session &&
                (it.serviceLearning == null || it.serviceLearning == evaluation.serviceLearning)
        } }) { "課程／講師資料已變更，請重新選擇 · Course or instructor data changed; select again" }
        val eligibility = checkEligibility(user.id, form.courseCode, form.term)
        check(eligibility.canSubmit) { eligibilityMessage(eligibility.reasonKey) }
        require(clientProvider.account?.get()?.id == submission.userId) { "Account changed" }
        val doc = db.createDocument(dbId, reviewsCol, ID.unique(),
            form.documentData(user.id, user.name, java.time.Instant.now().toString()))
        recomputeStats(form.courseCode)
        Result.success(mapDocumentToReview(doc.id, doc.data, doc.createdAt))
    } catch (e: kotlinx.coroutines.CancellationException) { throw e
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun voteReview(reviewId: String, voteType: String): Result<VoteState> {
        val databases = clientProvider.databases
            ?: return Result.failure(IllegalStateException("Databases not initialized"))

        val account = clientProvider.account
        val user = account?.get() ?: return Result.failure(IllegalStateException("Must be logged in to vote"))

        return try {
            val existingVotes = databases.listDocuments(
                databaseId = dbId,
                collectionId = votesCol,
                queries = listOf(
                    Query.equal("review_id", reviewId),
                    Query.equal("user_id", user.id)
                )
            )

            val existingVoteDoc = existingVotes.documents.firstOrNull()
            val currentVote = existingVoteDoc?.data?.get("vote_type") as? String

            val finalVote = if (currentVote == voteType) {
                if (existingVoteDoc != null) {
                    databases.deleteDocument(dbId, votesCol, existingVoteDoc.id)
                }
                null
            } else {
                if (existingVoteDoc != null) {
                    databases.updateDocument(
                        databaseId = dbId,
                        collectionId = votesCol,
                        documentId = existingVoteDoc.id,
                        data = mapOf("vote_type" to voteType)
                    )
                } else {
                    databases.createDocument(
                        databaseId = dbId,
                        collectionId = votesCol,
                        documentId = ID.unique(),
                        data = mapOf(
                            "review_id" to reviewId,
                            "user_id" to user.id,
                            "vote_type" to voteType
                        )
                    )
                }
                voteType
            }

            val allVotes = readVotes(reviewId)

            val upCount = allVotes.count { it.data["vote_type"] == "up" }
            val downCount = allVotes.count { it.data["vote_type"] == "down" }

            Result.success(VoteState(upvotes = upCount, downvotes = downCount, userVote = finalVote))
        } catch (e: Exception) {
            Log.e(TAG, "Error voting on review: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun checkEligibility(
        userId: String,
        courseCode: String,
        termCode: String
    ): EligibilityResult {
        val databases = clientProvider.databases
            ?: return EligibilityResult(false, "review.eligibilityUnavailable")

        return try {
            val userReviews = databases.listDocuments(
                databaseId = dbId,
                collectionId = reviewsCol,
                queries = listOf(
                    Query.equal("user_id", userId),
                    Query.equal("term_code", termCode)
                )
            )

            if (userReviews.total >= 7) {
                return EligibilityResult(false, "review.termLimitExceeded")
            }

            val courseReviews = databases.listDocuments(
                databaseId = dbId,
                collectionId = reviewsCol,
                queries = listOf(
                    Query.equal("user_id", userId),
                    Query.equal("course_code", courseCode.trim().uppercase())
                )
            )

            if (courseReviews.total >= 2) {
                return EligibilityResult(false, "review.limitExceeded")
            }

            if (courseReviews.total == 1L) {
                val grade = (courseReviews.documents[0].data["course_final_grade"] as? String
                    ?: courseReviews.documents[0].data["grade"] as? String)?.trim()?.uppercase()
                if (grade == "F") {
                    return EligibilityResult(true)
                } else {
                    return EligibilityResult(false, "review.limitReachedWithPass")
                }
            }

            EligibilityResult(true)
        } catch (e: kotlinx.coroutines.CancellationException) { throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error checking eligibility: ${e.message}", e)
            EligibilityResult(false, "review.eligibilityUnavailable")
        }
    }

    private fun mapDocumentToReview(docId: String, data: Map<String, Any>, docCreatedAt: String): Review {
        val courseCode = (data["course_code"] as? String) ?: ""
        val termCode = (data["term_code"] as? String) ?: (data["term"] as? String) ?: ""
        val grade = (data["course_final_grade"] as? String) ?: (data["grade"] as? String) ?: "N/A"
        val workload = (data["course_workload"] as? Number)?.toDouble() ?: -1.0
        val difficulty = (data["course_difficulties"] as? Number)?.toDouble() ?: -1.0
        val comment = (data["course_comments"] as? String) ?: (data["comment"] as? String) ?: ""
        val isAnon = (data["is_anon"] as? Boolean) ?: (data["is_anonymous"] as? Boolean) ?: true

        val courseTitle = (data["course_title"] as? String) ?: courseCode
        val courseTitleZh = (data["course_title_tc"] as? String)?.trim()
            ?: (data["course_title_sc"] as? String)?.trim()
            ?: (data["course_title_zh"] as? String)?.trim()
            ?: ""

        val details = InstructorEvaluation.fromWire(data["instructor_details"] as? String)
        val instructorName = details.joinToString { it.name }.ifBlank { data["instructor_name"] as? String ?: "" }
        val instructorNameZh = data["instructor_name_tc"] as? String ?: ""
        fun average(values: List<Double?>) = values.filterNotNull().filter { it >= 0 }.let { if (it.isEmpty()) -1.0 else it.average() }
        val teachingRating = average(details.map { if (it.notAttended) -1.0 else it.teaching })
        val gradingRating = average(details.map { it.grading })

        return Review(
            id = docId,
            courseCode = courseCode,
            courseTitle = courseTitle,
            courseTitleZh = courseTitleZh,
            instructorName = instructorName,
            instructorNameZh = instructorNameZh,
            term = termCode,
            academicYear = if (termCode.length >= 4) termCode.take(4) else "",
            grade = grade,
            workloadRating = workload,
            difficultyRating = difficulty,
            gradingRating = gradingRating,
            teachingRating = teachingRating,
            comment = comment,
            upvotes = (data["upvotes"] as? Number)?.toInt() ?: 0,
            downvotes = (data["downvotes"] as? Number)?.toInt() ?: 0,
            userVote = data["user_vote"] as? String,
            createdAt = (data["submitted_at"] as? String) ?: (data["created_at"] as? String) ?: docCreatedAt,
            userId = (data["user_id"] as? String) ?: "",
            isAnonymous = isAnon,
            usefulnessRating = (data["course_usefulness"] as? Number)?.toDouble() ?: -1.0,
            language = data["review_language"] as? String ?: "",
            instructors = details
        )
    }
}

internal fun eligibilityMessage(key: String?): String = when (key) {
    "review.termLimitExceeded" -> "本學期最多可提交 7 則評價 · Maximum 7 reviews per term"
    "review.limitExceeded" -> "此課程最多可提交 2 則評價 · Maximum 2 reviews per course"
    "review.limitReachedWithPass" -> "你已評價此課程；僅不及格後重修可再次提交 · Only a failed course can be reviewed again"
    else -> "無法確認提交資格，請重試 · Could not verify eligibility; retry"
}
