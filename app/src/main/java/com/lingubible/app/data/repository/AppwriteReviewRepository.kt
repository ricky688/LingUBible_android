package com.lingubible.app.data.repository

import android.util.Log
import com.lingubible.app.data.remote.AppwriteClientProvider
import com.lingubible.app.domain.model.EligibilityResult
import com.lingubible.app.domain.model.Review
import com.lingubible.app.domain.model.ReviewSubmission
import com.lingubible.app.domain.model.VoteState
import com.lingubible.app.domain.repository.ReviewRepository
import com.lingubible.app.domain.util.RatingValidator
import com.lingubible.app.domain.util.WordCountValidator
import io.appwrite.ID
import io.appwrite.Query
import org.json.JSONArray
import org.json.JSONObject

class AppwriteReviewRepository(
    private val clientProvider: AppwriteClientProvider
) : ReviewRepository {

    private val dbId = clientProvider.databaseId
    private val reviewsCol = "reviews"
    private val votesCol = "review_votes"
    private val TAG = "ReviewRepo"

    override suspend fun getReviewsForCourse(courseCode: String): Result<List<Review>> {
        return getReviews(listOf(
            Query.equal("course_code", courseCode.trim().uppercase()),
            Query.orderDesc("\$createdAt")
        ))
    }

    override suspend fun getReviewsForInstructor(instructorName: String): Result<List<Review>> {
        return getLatestReviews(200).map { reviews ->
            reviews.filter { it.instructorName.contains(instructorName.trim(), ignoreCase = true) }
        }
    }

    override suspend fun getLatestReviews(limit: Int): Result<List<Review>> {
        return getReviews(listOf(
            Query.orderDesc("\$createdAt"),
            Query.limit(limit)
        ))
    }

    private suspend fun getReviews(queries: List<String>): Result<List<Review>> {
        val databases = clientProvider.databases
            ?: return Result.success(emptyList())

        Log.d(TAG, "getReviews called: queries=$queries")

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
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching reviews: ${e.message}", e)
            Result.failure(e)
        }
    }


    override suspend fun submitReview(submission: ReviewSubmission): Result<Review> {
        if (!WordCountValidator.isValid(submission.comment, 5, 1000)) {
            return Result.failure(IllegalArgumentException("Review comment must contain between 5 and 1000 words"))
        }

        if (!RatingValidator.isValidRating(submission.workloadRating) ||
            !RatingValidator.isValidRating(submission.difficultyRating) ||
            !RatingValidator.isValidRating(submission.gradingRating) ||
            !RatingValidator.isValidRating(submission.teachingRating)
        ) {
            return Result.failure(IllegalArgumentException("Ratings must be between 0.5 and 5.0 (in increments of 0.5) or -1 for N/A"))
        }

        val databases = clientProvider.databases
            ?: return Result.failure(IllegalStateException("Databases not initialized"))

        return try {
            val account = clientProvider.account
            val user = account?.get()
            val userId = user?.id ?: ""

            val instructorDetailsJson = JSONArray().apply {
                put(JSONObject().apply {
                    put("instructor_name", submission.instructorName.trim())
                    put("session_type", "Lecture")
                    put("grading", submission.gradingRating)
                    put("teaching", submission.teachingRating)
                    put("comments", submission.comment.trim())
                })
            }.toString()

            val documentData = mapOf(
                "course_code" to submission.courseCode.trim().uppercase(),
                "term_code" to submission.term.trim(),
                "course_final_grade" to submission.grade.trim().uppercase(),
                "course_workload" to submission.workloadRating,
                "course_difficulties" to submission.difficultyRating,
                "course_usefulness" to 3.0,
                "course_comments" to submission.comment.trim(),
                "instructor_details" to instructorDetailsJson,
                "user_id" to userId,
                "username" to (user?.name ?: "Lingnanian"),
                "is_anon" to submission.isAnonymous,
                "submitted_at" to java.time.Instant.now().toString(),
                "review_language" to "en"
            )

            val doc = databases.createDocument(
                databaseId = dbId,
                collectionId = reviewsCol,
                documentId = ID.unique(),
                data = documentData
            )

            val review = mapDocumentToReview(doc.id, doc.data, doc.createdAt)
            Result.success(review)
        } catch (e: Exception) {
            Log.e(TAG, "Error submitting review: ${e.message}", e)
            Result.failure(e)
        }
    }

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

            val allVotes = databases.listDocuments(
                databaseId = dbId,
                collectionId = votesCol,
                queries = listOf(Query.equal("review_id", reviewId))
            )

            val upCount = allVotes.documents.count { it.data["vote_type"] == "up" }
            val downCount = allVotes.documents.count { it.data["vote_type"] == "down" }

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
            ?: return EligibilityResult(true)

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
        } catch (e: Exception) {
            Log.e(TAG, "Error checking eligibility: ${e.message}", e)
            EligibilityResult(true)
        }
    }

    private fun mapDocumentToReview(docId: String, data: Map<String, Any>, docCreatedAt: String): Review {
        val courseCode = (data["course_code"] as? String) ?: ""
        val termCode = (data["term_code"] as? String) ?: (data["term"] as? String) ?: ""
        val grade = (data["course_final_grade"] as? String) ?: (data["grade"] as? String) ?: "N/A"
        val workload = (data["course_workload"] as? Number)?.toDouble() ?: 3.0
        val difficulty = (data["course_difficulties"] as? Number)?.toDouble() ?: 3.0
        val comment = (data["course_comments"] as? String) ?: (data["comment"] as? String) ?: ""
        val isAnon = (data["is_anon"] as? Boolean) ?: (data["is_anonymous"] as? Boolean) ?: true

        val courseTitle = (data["course_title"] as? String) ?: courseCode
        val courseTitleZh = (data["course_title_tc"] as? String)?.trim()
            ?: (data["course_title_sc"] as? String)?.trim()
            ?: (data["course_title_zh"] as? String)?.trim()
            ?: ""

        val instructorDetailsStr = data["instructor_details"] as? String
        var instructorName = (data["instructor_name"] as? String) ?: ""
        var instructorNameZh = (data["instructor_name_tc"] as? String)?.trim()
            ?: (data["instructor_name_zh"] as? String)?.trim()
            ?: ""
        var teachingRating = 3.0
        var gradingRating = 3.0

        if (!instructorDetailsStr.isNullOrBlank()) {
            try {
                val jsonArr = JSONArray(instructorDetailsStr)
                if (jsonArr.length() > 0) {
                    val first = jsonArr.getJSONObject(0)
                    if (instructorName.isBlank()) {
                        instructorName = first.optString("instructor_name", "")
                    }
                    if (instructorNameZh.isBlank()) {
                        instructorNameZh = first.optString("instructor_name_tc", "")
                            .ifBlank { first.optString("name_tc", "") }
                    }
                    val t = first.optDouble("teaching", -1.0)
                    if (t > 0) teachingRating = t
                    val g = first.optDouble("grading", -1.0)
                    if (g > 0) gradingRating = g
                }
            } catch (_: Exception) {}
        }

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
            workloadRating = if (workload > 0) workload else 3.0,
            difficultyRating = if (difficulty > 0) difficulty else 3.0,
            gradingRating = gradingRating,
            teachingRating = teachingRating,
            comment = comment,
            upvotes = (data["upvotes"] as? Number)?.toInt() ?: 0,
            downvotes = (data["downvotes"] as? Number)?.toInt() ?: 0,
            userVote = data["user_vote"] as? String,
            createdAt = (data["submitted_at"] as? String) ?: (data["created_at"] as? String) ?: docCreatedAt,
            userId = (data["user_id"] as? String) ?: "",
            isAnonymous = isAnon
        )
    }
}
