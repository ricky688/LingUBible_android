package com.lingubible.app.domain.repository

import androidx.activity.ComponentActivity
import com.lingubible.app.domain.model.*
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val currentUser: StateFlow<User?>
    suspend fun login(email: String, password: String): Result<Session>
    suspend fun register(email: String, password: String, name: String): Result<User>
    suspend fun loginWithGoogle(activity: ComponentActivity): Result<User>
    suspend fun logout(): Result<Unit>
    suspend fun checkSession(): Result<User?>
    suspend fun updateName(name: String): Result<User>
    suspend fun updatePassword(newPassword: String, oldPassword: String): Result<Unit>
    suspend fun isGoogleLinked(): Result<Boolean>
    suspend fun unlinkGoogle(): Result<Unit>
    fun isValidEmail(email: String): Boolean
}

interface CourseRepository {
    suspend fun getTerms(): Result<List<CourseTerm>> = Result.failure(UnsupportedOperationException())
    suspend fun getCourseCodesForTerms(terms: Set<String>): Result<Set<String>> = Result.failure(UnsupportedOperationException())
    suspend fun getCourses(search: String? = null, subject: String? = null, page: Int = 1): Result<List<Course>>
    suspend fun getCourseByCode(courseCode: String): Result<Course>
    suspend fun getTeachingRecords(courseCode: String): Result<List<TeachingRecord>>
    suspend fun getGradeDistribution(courseCode: String): Result<Map<String, Int>>
}

interface InstructorRepository {
    suspend fun getAllInstructors(): Result<List<Instructor>> = getInstructors()
    suspend fun getInstructorNamesForTerms(terms: Set<String>): Result<Set<String>> = Result.failure(UnsupportedOperationException())
    suspend fun getInstructors(search: String? = null, department: String? = null, page: Int = 1): Result<List<Instructor>>
    suspend fun getInstructorByName(name: String): Result<Instructor>
    suspend fun getInstructorTeachingRecords(name: String): Result<List<TeachingRecord>>
}

interface ReviewRepository {
    suspend fun getAllReviewsForBrowsing(): Result<List<Review>> = getLatestReviews(100)
    suspend fun getReviewEdit(reviewId: String): Result<ReviewEdit> = Result.failure(UnsupportedOperationException())
    suspend fun updateReview(reviewId: String, edit: ReviewEdit): Result<Review> = Result.failure(UnsupportedOperationException())
    suspend fun deleteReview(reviewId: String): Result<Unit> = Result.failure(UnsupportedOperationException())
    suspend fun getReviewsForCourse(courseCode: String): Result<List<Review>>
    suspend fun getReviewsForInstructor(instructorName: String): Result<List<Review>>
    suspend fun getLatestReviews(limit: Int = 20): Result<List<Review>>
    suspend fun getReviewsForUser(userId: String): Result<List<Review>>
    suspend fun submitReview(submission: ReviewSubmission): Result<Review>
    suspend fun voteReview(reviewId: String, voteType: String): Result<VoteState>
    suspend fun checkEligibility(userId: String, courseCode: String, termCode: String): EligibilityResult
}

interface MaterialRepository {
    suspend fun getStudyMaterials(courseCode: String): Result<List<StudyMaterial>> = Result.failure(UnsupportedOperationException())
    suspend fun getStudyMaterialCourseCodes(): Result<Set<String>> = Result.failure(UnsupportedOperationException())
    suspend fun getPastPapers(courseCode: String): Result<List<PastPaper>>
    suspend fun getDownloadUrl(fileId: String): String
    suspend fun getViewUrl(fileId: String): String
    suspend fun getSyllabus(courseCode: String): Result<CourseSyllabus?>
    suspend fun getSyllabusViewUrl(fileId: String): String
    suspend fun getSyllabusDownloadUrl(fileId: String): String
    suspend fun downloadFileBytes(bucketId: String, fileId: String): Result<ByteArray> = Result.failure(UnsupportedOperationException("downloadFileBytes not supported"))
}

