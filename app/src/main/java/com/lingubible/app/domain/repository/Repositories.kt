package com.lingubible.app.domain.repository

import com.lingubible.app.domain.model.*
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val currentUser: StateFlow<User?>
    suspend fun login(email: String, password: String): Result<Session>
    suspend fun register(email: String, password: String, name: String): Result<User>
    suspend fun logout(): Result<Unit>
    suspend fun checkSession(): Result<User?>
    fun isValidEmail(email: String): Boolean
}

interface CourseRepository {
    suspend fun getCourses(search: String? = null, subject: String? = null, page: Int = 1): Result<List<Course>>
    suspend fun getCourseByCode(courseCode: String): Result<Course>
    suspend fun getTeachingRecords(courseCode: String): Result<List<TeachingRecord>>
    suspend fun getGradeDistribution(courseCode: String): Result<Map<String, Int>>
}

interface InstructorRepository {
    suspend fun getInstructors(search: String? = null, department: String? = null, page: Int = 1): Result<List<Instructor>>
    suspend fun getInstructorByName(name: String): Result<Instructor>
    suspend fun getInstructorTeachingRecords(name: String): Result<List<TeachingRecord>>
}

interface ReviewRepository {
    suspend fun getReviewsForCourse(courseCode: String): Result<List<Review>>
    suspend fun getReviewsForInstructor(instructorName: String): Result<List<Review>>
    suspend fun getLatestReviews(limit: Int = 20): Result<List<Review>>
    suspend fun submitReview(submission: ReviewSubmission): Result<Review>
    suspend fun voteReview(reviewId: String, voteType: String): Result<VoteState>
    suspend fun checkEligibility(userId: String, courseCode: String, termCode: String): EligibilityResult
}

interface MaterialRepository {
    suspend fun getPastPapers(courseCode: String): Result<List<PastPaper>>
    suspend fun getDownloadUrl(fileId: String): String
    suspend fun getViewUrl(fileId: String): String
}
