package com.lingubible.app.e2e.contracts

import kotlin.math.floor
import kotlin.math.round
import kotlin.math.sqrt

/**
 * Authoritative E2E Interface Contracts and Specification Models.
 * Derived strictly from ORIGINAL_REQUEST.md, PROJECT.md, and reference web models in src/.
 */

object AppwriteConfigContract {
    const val ENDPOINT = "https://appwrite.lingubible.com/v1"
    const val PROJECT_ID = "6a1097400037a55f6472"
    const val DATABASE_ID = "lingubible"

    val REQUIRED_COLLECTIONS = listOf(
        "courses",
        "reviews",
        "review_votes",
        "teaching_records",
        "instructors",
        "terms",
        "course_offerings"
    )

    val STORAGE_BUCKETS = listOf(
        "course_syllabus",
        "past_exam_papers",
        "study_materials"
    )

    fun validateEndpoint(endpoint: String): Boolean {
        return endpoint.startsWith("https://") && endpoint.contains("appwrite.lingubible.com")
    }

    fun validateProjectId(projectId: String?): Boolean {
        if (projectId.isNullOrBlank()) return false
        return projectId.trim() == PROJECT_ID
    }

    fun validateDatabaseId(databaseId: String?): Boolean {
        if (databaseId.isNullOrBlank()) return false
        return databaseId.trim() == DATABASE_ID
    }

    fun validateTimeout(seconds: Int): Boolean {
        return seconds in 5..60
    }

    fun buildViewUrl(bucketId: String, fileId: String): String {
        return "$ENDPOINT/storage/buckets/$bucketId/files/$fileId/view?project=$PROJECT_ID"
    }

    fun buildDownloadUrl(bucketId: String, fileId: String): String {
        return "$ENDPOINT/storage/buckets/$bucketId/files/$fileId/download?project=$PROJECT_ID"
    }
}

object EmailValidatorContract {
    private val EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@(?:ln\\.hk|ln\\.edu\\.hk)$".toRegex(RegexOption.IGNORE_CASE)

    fun isValidLingnanEmail(email: String?): Boolean {
        if (email.isNullOrBlank()) return false
        val trimmed = email.trim()
        return EMAIL_REGEX.matches(trimmed)
    }
}

object WordCountValidatorContract {
    fun countWords(text: String?): Int {
        if (text.isNullOrBlank()) return 0
        return text.trim().split("\\s+".toRegex()).size
    }

    data class WordCountResult(
        val isValid: Boolean,
        val wordCount: Int,
        val message: String? = null
    )

    fun validateWordCount(text: String?, minWords: Int = 5, maxWords: Int = 1000): WordCountResult {
        val count = countWords(text)
        return when {
            count < minWords -> WordCountResult(
                isValid = false,
                wordCount = count,
                message = "At least $minWords words required ($count/$minWords)"
            )
            count > maxWords -> WordCountResult(
                isValid = false,
                wordCount = count,
                message = "Too many words ($count/$maxWords)"
            )
            else -> WordCountResult(
                isValid = true,
                wordCount = count
            )
        }
    }
}

object RatingValidatorContract {
    fun isValidRating(rating: Double): Boolean {
        if (rating == -1.0) return true // -1.0 represents N/A or Not Attended
        if (rating < 0.5 || rating > 5.0) return false
        // Must be in increments of 0.5
        val multiplied = (rating * 2).toInt()
        return (rating * 2) == multiplied.toDouble()
    }

    fun clampRating(rating: Double): Double {
        if (rating == -1.0) return -1.0
        if (rating < 0.5) return 0.5
        if (rating > 5.0) return 5.0
        return round(rating * 2) / 2.0
    }
}

object GradeMathContract {
    val GRADE_TO_GPA_MAP: Map<String, Double> = mapOf(
        "A" to 4.00,
        "A-" to 3.67,
        "B+" to 3.33,
        "B" to 3.00,
        "B-" to 2.67,
        "C+" to 2.33,
        "C" to 2.00,
        "C-" to 1.67,
        "D+" to 1.33,
        "D" to 1.00,
        "F" to 0.00
    )

    val GRADE_ORDER = listOf("A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D+", "D", "F", "N/A")

    data class GradeStatistics(
        val mean: Double?,
        val standardDeviation: Double?,
        val totalCount: Int,
        val validGradeCount: Int,
        val gradeDistribution: Map<String, Int>
    )

    data class BoxPlotStatistics(
        val min: Double,
        val q1: Double,
        val median: Double,
        val q3: Double,
        val max: Double,
        val outliers: List<Double>,
        val validCount: Int
    )

    data class ParetoPoint(
        val grade: String,
        val count: Int,
        val percentage: Double,
        val cumulativePercentage: Double
    )

    fun calculateGradeStatistics(distribution: Map<String, Int>): GradeStatistics {
        val totalCount = distribution.values.sum()
        if (totalCount == 0) {
            return GradeStatistics(null, null, 0, 0, distribution)
        }

        var totalGPA = 0.0
        var validGradeCount = 0

        distribution.forEach { (grade, count) ->
            if (grade != "N/A" && count > 0) {
                val gpa = GRADE_TO_GPA_MAP[grade]
                if (gpa != null) {
                    totalGPA += gpa * count
                    validGradeCount += count
                }
            }
        }

        if (validGradeCount == 0) {
            return GradeStatistics(null, null, totalCount, 0, distribution)
        }

        val mean = totalGPA / validGradeCount

        var varianceSum = 0.0
        distribution.forEach { (grade, count) ->
            if (grade != "N/A" && count > 0) {
                val gpa = GRADE_TO_GPA_MAP[grade]
                if (gpa != null) {
                    val dev = gpa - mean
                    varianceSum += (dev * dev) * count
                }
            }
        }

        val variance = varianceSum / validGradeCount
        val standardDeviation = sqrt(variance)

        return GradeStatistics(
            mean = mean,
            standardDeviation = standardDeviation,
            totalCount = totalCount,
            validGradeCount = validGradeCount,
            gradeDistribution = distribution
        )
    }

    fun calculateBoxPlotStatistics(distribution: Map<String, Int>): BoxPlotStatistics? {
        val gpaValues = mutableListOf<Double>()
        distribution.forEach { (grade, count) ->
            if (grade != "N/A" && count > 0) {
                val gpa = GRADE_TO_GPA_MAP[grade]
                if (gpa != null) {
                    repeat(count) { gpaValues.add(gpa) }
                }
            }
        }

        if (gpaValues.isEmpty()) return null
        val sorted = gpaValues.sorted()
        val n = sorted.size

        val q1Index = floor(n * 0.25).toInt()
        val medianIndex = floor(n * 0.5).toInt()
        val q3Index = floor(n * 0.75).toInt()

        val q1 = sorted[q1Index]
        val median = if (n % 2 == 0 && n > 1) {
            (sorted[medianIndex - 1] + sorted[medianIndex]) / 2.0
        } else {
            sorted[medianIndex]
        }
        val q3 = sorted[q3Index]

        val iqr = q3 - q1
        val lowerBound = q1 - 1.5 * iqr
        val upperBound = q3 + 1.5 * iqr

        val outliers = sorted.filter { it < lowerBound || it > upperBound }
        val nonOutliers = sorted.filter { it in lowerBound..upperBound }

        val min = if (nonOutliers.isNotEmpty()) nonOutliers.first() else sorted.first()
        val max = if (nonOutliers.isNotEmpty()) nonOutliers.last() else sorted.last()

        return BoxPlotStatistics(
            min = min,
            q1 = q1,
            median = median,
            q3 = q3,
            max = max,
            outliers = outliers,
            validCount = n
        )
    }

    fun calculateParetoCurve(distribution: Map<String, Int>): List<ParetoPoint> {
        val validGrades = listOf("A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D+", "D", "F")
        val validTotal = validGrades.sumOf { distribution[it] ?: 0 }
        if (validTotal == 0) return emptyList()

        var runningCount = 0
        return validGrades.map { grade ->
            val count = distribution[grade] ?: 0
            runningCount += count
            val pct = (count.toDouble() / validTotal) * 100.0
            val cumPct = (runningCount.toDouble() / validTotal) * 100.0
            ParetoPoint(
                grade = grade,
                count = count,
                percentage = pct,
                cumulativePercentage = cumPct
            )
        }
    }
}

object AntiSpamRulesContract {
    const val MAX_REVIEWS_PER_TERM = 7
    const val MAX_REVIEWS_PER_COURSE = 2
    val REVIEW_RETRY_FAIL_GRADES = setOf("F")

    data class EligibilityResult(
        val canSubmit: Boolean,
        val reason: String? = null
    )

    fun checkReviewEligibility(
        userId: String,
        courseCode: String,
        termCode: String,
        termReviewCount: Int,
        existingCourseReviews: List<ExistingCourseReview>
    ): EligibilityResult {
        if (termReviewCount >= MAX_REVIEWS_PER_TERM) {
            return EligibilityResult(false, "review.termLimitExceeded")
        }

        if (existingCourseReviews.isEmpty()) {
            return EligibilityResult(true)
        }

        if (existingCourseReviews.size >= MAX_REVIEWS_PER_COURSE) {
            return EligibilityResult(false, "review.limitExceeded")
        }

        if (existingCourseReviews.size == 1) {
            val firstGrade = existingCourseReviews[0].finalGrade?.trim()?.uppercase()
            if (firstGrade in REVIEW_RETRY_FAIL_GRADES) {
                return EligibilityResult(true)
            } else {
                return EligibilityResult(false, "review.limitReachedWithPass")
            }
        }

        return EligibilityResult(false, "review.unknownError")
    }

    data class ExistingCourseReview(
        val reviewId: String,
        val courseCode: String,
        val finalGrade: String?
    )
}

object ReviewVotingContract {
    data class VoteState(
        val upvotes: Int,
        val downvotes: Int,
        val userVote: String? // "up", "down", or null
    )

    fun applyVote(currentState: VoteState, newVoteType: String): VoteState {
        require(newVoteType == "up" || newVoteType == "down") { "Vote type must be 'up' or 'down'" }

        return when (currentState.userVote) {
            null -> {
                if (newVoteType == "up") {
                    currentState.copy(upvotes = currentState.upvotes + 1, userVote = "up")
                } else {
                    currentState.copy(downvotes = currentState.downvotes + 1, userVote = "down")
                }
            }
            newVoteType -> {
                // Toggle off
                if (newVoteType == "up") {
                    currentState.copy(upvotes = maxOf(0, currentState.upvotes - 1), userVote = null)
                } else {
                    currentState.copy(downvotes = maxOf(0, currentState.downvotes - 1), userVote = null)
                }
            }
            else -> {
                // Switch vote
                if (newVoteType == "up") {
                    currentState.copy(
                        upvotes = currentState.upvotes + 1,
                        downvotes = maxOf(0, currentState.downvotes - 1),
                        userVote = "up"
                    )
                } else {
                    currentState.copy(
                        upvotes = maxOf(0, currentState.upvotes - 1),
                        downvotes = currentState.downvotes + 1,
                        userVote = "down"
                    )
                }
            }
        }
    }
}

object PastPapersParserContract {
    data class ParsedExamPaper(
        val courseCode: String,
        val academicYear: String,
        val term: String,
        val instructor: String? = null
    )

    private val PATTERN_A = "^([A-Z]{3,4}\\d{4})_(\\d{4})(\\d)(?:_(.+))?\\.pdf$".toRegex(RegexOption.IGNORE_CASE)
    private val PATTERN_B = "^([A-Z]{3,4}\\d{4})_(\\d{4}-\\d{4})_Term(\\d)(?:_(.+))?\\.pdf$".toRegex(RegexOption.IGNORE_CASE)

    fun parseFilename(filename: String): ParsedExamPaper? {
        val matchA = PATTERN_A.matchEntire(filename)
        if (matchA != null) {
            val (code, yyRaw, termDigit, instructor) = matchA.destructured
            val year = "20${yyRaw.substring(0, 2)}-20${yyRaw.substring(2, 4)}"
            val term = "Term $termDigit"
            return ParsedExamPaper(code.uppercase(), year, term, instructor.ifBlank { null })
        }

        val matchB = PATTERN_B.matchEntire(filename)
        if (matchB != null) {
            val (code, year, termDigit, instructor) = matchB.destructured
            val term = "Term $termDigit"
            return ParsedExamPaper(code.uppercase(), year, term, instructor.ifBlank { null })
        }

        return null
    }

    fun deduplicate(papers: List<ParsedExamPaper>): List<ParsedExamPaper> {
        return papers.distinctBy { "${it.courseCode}_${it.academicYear}_${it.term}_${it.instructor.orEmpty()}" }
    }
}

object LocalizationContract {
    fun escapeForXml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("'", "\\'")
            .replace("\"", "\\\"")
            .replace("@", "\\@")
            .replace("?", "\\?")
    }

    fun convertParamFormat(text: String): String {
        var result = text
        val regex = "\\{([a-zA-Z0-9_]+)\\}".toRegex()
        var index = 1
        result = regex.replace(result) {
            val token = "%${index}\$s"
            index++
            token
        }
        return result
    }
}

object ProjectScaffoldingContract {
    const val MIN_SDK = 26
    const val TARGET_SDK = 35
    const val COMPILE_SDK = 35
    const val JAVA_VERSION_TARGET = 17

    val REQUIRED_PLUGINS = listOf(
        "com.android.application",
        "org.jetbrains.kotlin.android",
        "org.jetbrains.kotlin.plugin.compose",
        "org.jetbrains.kotlin.plugin.serialization"
    )

    private val PACKAGE_REGEX = "^[a-z][a-z0-9_]*(\\.[a-z][a-z0-9_]*)+$".toRegex()
    private val JAVA_KEYWORDS = setOf("class", "public", "private", "fun", "package", "val", "var")

    fun validateSdkConstraints(minSdk: Int, targetSdk: Int, compileSdk: Int): Boolean {
        if (minSdk < MIN_SDK) return false
        if (targetSdk < 34) return false
        if (compileSdk < targetSdk) return false
        return true
    }

    fun validatePluginSet(plugins: Collection<String>): Boolean {
        return REQUIRED_PLUGINS.all { plugins.contains(it) }
    }

    fun validateJavaVersion(version: Int): Boolean {
        return version >= JAVA_VERSION_TARGET
    }

    fun validatePackageName(packageName: String): Boolean {
        if (!PACKAGE_REGEX.matches(packageName)) return false
        val parts = packageName.split(".")
        return parts.none { it in JAVA_KEYWORDS }
    }
}

object ColorContrastContract {
    fun calculateLuminance(r: Float, g: Float, b: Float): Double {
        fun adjust(c: Float): Double {
            return if (c <= 0.04045f) c.toDouble() / 12.92
            else Math.pow(((c + 0.055) / 1.055).toDouble(), 2.4)
        }
        val rLin = adjust(r)
        val gLin = adjust(g)
        val bLin = adjust(b)
        return 0.2126 * rLin + 0.7152 * gLin + 0.0722 * bLin
    }

    fun calculateContrastRatio(fgR: Float, fgG: Float, fgB: Float, bgR: Float, bgG: Float, bgB: Float): Double {
        val lum1 = calculateLuminance(fgR, fgG, fgB)
        val lum2 = calculateLuminance(bgR, bgG, bgB)
        val lighter = maxOf(lum1, lum2)
        val darker = minOf(lum1, lum2)
        return (lighter + 0.05) / (darker + 0.05)
    }
}

object AuthValidatorContract {
    data class ValidationResult(val isValid: Boolean, val message: String? = null)

    fun validatePassword(password: String?): ValidationResult {
        if (password.isNullOrBlank()) return ValidationResult(false, "Password cannot be empty")
        if (password.length < 8) return ValidationResult(false, "Password must be at least 8 characters")
        if (password.length > 256) return ValidationResult(false, "Password exceeds maximum length")
        return ValidationResult(true)
    }

    fun isValidSessionToken(token: String?): Boolean {
        if (token.isNullOrBlank()) return false
        val tokenRegex = "^[a-zA-Z0-9_-]{16,64}$".toRegex()
        return tokenRegex.matches(token.trim())
    }

    fun validateRegistrationPayload(email: String, password: String, name: String): ValidationResult {
        if (!EmailValidatorContract.isValidLingnanEmail(email)) return ValidationResult(false, "Invalid Lingnan email")
        val pwdResult = validatePassword(password)
        if (!pwdResult.isValid) return pwdResult
        if (name.isBlank()) return ValidationResult(false, "Name cannot be blank")
        return ValidationResult(true)
    }

    fun isSessionActive(expiresAt: Long, now: Long): Boolean {
        return now < expiresAt
    }

    enum class AuthError { None, AccountConflict, InvalidCredentials, RateLimited, Unknown }

    fun mapHttpStatus(status: Int): AuthError = when (status) {
        200 -> AuthError.None
        401 -> AuthError.InvalidCredentials
        409 -> AuthError.AccountConflict
        429 -> AuthError.RateLimited
        else -> AuthError.Unknown
    }

    sealed interface SessionState {
        val isAuthenticated: Boolean

        data object Unauthenticated : SessionState {
            override val isAuthenticated: Boolean = false
        }

        data class Authenticated(val userId: String, val sessionSecret: String) : SessionState {
            override val isAuthenticated: Boolean = true
        }
    }

    class SessionStateMachine(initialState: SessionState = SessionState.Unauthenticated) {
        var currentState: SessionState = initialState
            private set

        fun login(userId: String, secret: String) {
            require(userId.isNotBlank() && secret.isNotBlank())
            currentState = SessionState.Authenticated(userId, secret)
        }

        fun logout() {
            currentState = SessionState.Unauthenticated
        }
    }
}

typealias AuthValidationContract = AuthValidatorContract
typealias SessionStateContract = AuthValidatorContract

object UserProfileContract {
    val ALLOWED_AVATARS = listOf("bear", "fox", "rabbit", "cat", "dog", "koala", "panda", "tiger")
    val ALLOWED_ANIMALS = setOf("bear", "fox", "rabbit", "cat", "dog", "koala", "panda", "tiger")
    const val MIN_BG_INDEX = 0
    const val MAX_BG_INDEX = 11

    data class ProfileResult(val isValid: Boolean, val error: String? = null)

    fun isValidAvatar(avatar: String?): Boolean = avatar != null && avatar in ALLOWED_AVATARS

    fun isValidAnimal(animal: String?): Boolean {
        if (animal.isNullOrBlank()) return false
        return ALLOWED_ANIMALS.contains(animal.trim().lowercase())
    }

    fun isValidBackgroundIndex(index: Int): Boolean = index in MIN_BG_INDEX..MAX_BG_INDEX
    fun isValidBgIndex(index: Int): Boolean = isValidBackgroundIndex(index)

    fun clampBackgroundIndex(index: Int): Int = index.coerceIn(MIN_BG_INDEX, MAX_BG_INDEX)

    fun validateDisplayName(name: String?): Boolean {
        if (name.isNullOrBlank()) return false
        val len = name.trim().length
        return len in 1..100
    }

    fun validateProfile(userId: String, email: String, name: String, avatar: String, bgIndex: Int): ProfileResult {
        if (userId.isBlank()) return ProfileResult(false, "User ID cannot be blank")
        if (!EmailValidatorContract.isValidLingnanEmail(email)) return ProfileResult(false, "Invalid email")
        if (name.isBlank()) return ProfileResult(false, "Name cannot be blank")
        if (!isValidAvatar(avatar)) return ProfileResult(false, "Invalid avatar")
        if (!isValidBackgroundIndex(bgIndex)) return ProfileResult(false, "Invalid background index")
        return ProfileResult(true)
    }

    fun toggleFavorite(current: List<String>, courseCode: String): List<String> {
        return if (courseCode in current) {
            current.filter { it != courseCode }
        } else {
            current + courseCode
        }
    }

    data class UserProfileState(
        val userId: String,
        val animal: String,
        val bgIndex: Int,
        val favorites: List<String>
    ) {
        val isEmptyFavorites: Boolean get() = favorites.isEmpty()
    }
}

object CourseCatalogContract {
    data class CourseItem(
        val courseCode: String = "",
        val title: String = "",
        val credits: Int? = 3,
        val department: String = "",
        val reviewCount: Int = 0,
        val averageRating: Double = 0.0,
        val titleEn: String = title,
        val titleTc: String? = null,
        val term: String = "202409",
        val languageCode: String = "E",
        val isServiceLearning: Boolean = false,
        val averageGpa: Double = averageRating
    ) {
        val code: String get() = courseCode
    }

    enum class SortBy { CODE, REVIEW_COUNT, RATING }

    data class PageResult<T>(
        val items: List<T>,
        val page: Int,
        val pageSize: Int,
        val totalCount: Int,
        val totalPages: Int,
        val hasMore: Boolean
    )

    fun <T> paginate(items: List<T>, page: Int, pageSize: Int): PageResult<T> {
        val clampedPageSize = pageSize.coerceIn(10, 100)
        val safePage = maxOf(1, page)
        val totalCount = items.size
        val totalPages = if (totalCount == 0) 0 else (totalCount + clampedPageSize - 1) / clampedPageSize
        val offset = (safePage - 1) * clampedPageSize
        val pageItems = if (offset >= totalCount) emptyList() else items.subList(offset, minOf(offset + clampedPageSize, totalCount))
        return PageResult(
            items = pageItems,
            page = safePage,
            pageSize = clampedPageSize,
            totalCount = totalCount,
            totalPages = totalPages,
            hasMore = safePage < totalPages
        )
    }

    fun calculatePaginationOffset(page: Int, pageSize: Int): Int {
        val validPage = maxOf(1, page)
        val validSize = pageSize.coerceIn(1, 100)
        return (validPage - 1) * validSize
    }

    fun sortCourses(courses: List<CourseItem>, sortBy: SortBy, ascending: Boolean = true): List<CourseItem> {
        val comparator = when (sortBy) {
            SortBy.CODE -> compareBy<CourseItem> { it.courseCode }
            SortBy.REVIEW_COUNT -> compareBy { it.reviewCount }
            SortBy.RATING -> compareBy { it.averageRating }
        }
        return if (ascending) courses.sortedWith(comparator) else courses.sortedWith(comparator.reversed())
    }

    fun sortCourses(courses: List<CourseItem>, sortBy: String): List<CourseItem> {
        return when (sortBy) {
            "code_asc" -> courses.sortedBy { it.courseCode }
            "gpa_desc" -> courses.sortedByDescending { it.averageGpa }
            "reviews_desc" -> courses.sortedByDescending { it.reviewCount }
            else -> courses
        }
    }

    fun validateCourse(course: CourseItem): Boolean {
        if (course.courseCode.isBlank() || course.courseCode.contains("/") || course.courseCode.contains("..") || course.courseCode.contains(" ")) return false
        return course.title.isNotBlank() && (course.credits ?: 0) in 1..6
    }

    fun normalizeMetrics(rating: Double?, reviewCount: Int): Pair<Double, Int> {
        val validRating = if (rating == null || rating.isNaN() || rating < 0.0) 0.0 else rating.coerceIn(0.0, 5.0)
        val validCount = maxOf(0, reviewCount)
        return Pair(validRating, validCount)
    }

    fun matchesSearch(course: CourseItem, query: String): Boolean {
        if (query.isBlank()) return true
        val q = query.trim().lowercase()
        return course.courseCode.lowercase().contains(q) ||
               course.title.lowercase().contains(q) ||
               course.titleEn.lowercase().contains(q) ||
               (course.titleTc != null && course.titleTc.lowercase().contains(q)) ||
               course.department.lowercase().contains(q)
    }

    fun searchCourses(query: String?, catalog: List<CourseItem>, maxQueryLength: Int = 100): List<CourseItem> {
        if (query.isNullOrBlank()) return catalog
        val sanitized = query.trim().take(maxQueryLength)
        val escapedRegex = Regex(Regex.escape(sanitized), RegexOption.IGNORE_CASE)
        return catalog.filter { course ->
            escapedRegex.containsMatchIn(course.courseCode) ||
            escapedRegex.containsMatchIn(course.title) ||
            escapedRegex.containsMatchIn(course.titleEn) ||
            (course.titleTc != null && escapedRegex.containsMatchIn(course.titleTc)) ||
            escapedRegex.containsMatchIn(course.department)
        }
    }

    fun searchCourses(courses: List<CourseItem>, query: String?): List<CourseItem> = searchCourses(query, courses)

    enum class ServiceLearningType { NONE, OPTIONAL, COMPULSORY }

    data class CourseFilterCriteria(
        val subject: String? = null,
        val term: String? = null,
        val language: String? = null,
        val serviceLearning: ServiceLearningType? = null
    )

    data class FilterCriteria(
        val subjectPrefix: String? = null,
        val term: String? = null,
        val languageCode: String? = null,
        val serviceLearning: Boolean? = null
    ) {
        val isEmpty: Boolean get() = subjectPrefix == null && term == null && languageCode == null && serviceLearning == null
    }

    data class FilterableCourse(
        val courseCode: String,
        val termOffering: List<String>,
        val teachingLanguages: List<String>,
        val serviceLearning: ServiceLearningType
    )

    val SUPPORTED_LANGUAGES = listOf("E", "C", "P", "1", "2", "3", "4", "5")
    val VALID_LANGUAGE_CODES = setOf("E", "C", "P", "1", "2", "3", "4", "5")

    fun isValidLanguageCode(code: String?): Boolean = code != null && VALID_LANGUAGE_CODES.contains(code.trim().uppercase())

    fun matchesFilter(course: FilterableCourse, criteria: CourseFilterCriteria): Boolean {
        if (criteria.subject != null && !course.courseCode.startsWith(criteria.subject, ignoreCase = true)) return false
        if (criteria.term != null && criteria.term !in course.termOffering) return false
        if (criteria.language != null && criteria.language !in course.teachingLanguages) return false
        if (criteria.serviceLearning != null && course.serviceLearning != criteria.serviceLearning) return false
        return true
    }

    fun filterCourses(catalog: List<CourseItem>, criteria: FilterCriteria): List<CourseItem> {
        if (criteria.isEmpty) return catalog
        return catalog.filter { course ->
            (criteria.subjectPrefix == null || course.courseCode.startsWith(criteria.subjectPrefix, ignoreCase = true)) &&
            (criteria.term == null || course.term == criteria.term) &&
            (criteria.languageCode == null || course.languageCode.equals(criteria.languageCode, ignoreCase = true)) &&
            (criteria.serviceLearning == null || course.isServiceLearning == criteria.serviceLearning)
        }
    }

    fun filterCourses(courses: List<CourseItem>, subject: String?, term: String?, language: String?): List<CourseItem> {
        return courses.filter { item ->
            (subject == null || item.courseCode.startsWith(subject, ignoreCase = true)) &&
            (term == null || item.term == term) &&
            (language == null || item.languageCode.equals(language, ignoreCase = true))
        }
    }

    val TABS = listOf("overview", "teaching", "reviews", "grades", "readings", "materials", "exams")

    data class TeachingRecord(val term: String, val instructor: String, val session: String)

    data class CourseDetailTabs(
        val hasOverview: Boolean = true,
        val hasTeaching: Boolean = true,
        val hasReviews: Boolean = true,
        val hasGrades: Boolean = true,
        val hasReadings: Boolean = true,
        val hasMaterials: Boolean = true,
        val hasExams: Boolean
    )

    fun evaluateTabs(pastPapersCount: Int): CourseDetailTabs {
        return CourseDetailTabs(hasExams = pastPapersCount > 0)
    }

    fun formatCredits(credits: Int?): String = credits?.toString() ?: "N/A"

    fun validateOverview(description: String, credits: Int): Boolean {
        return description.isNotBlank() && credits > 0
    }
}

typealias CourseSearchContract = CourseCatalogContract
typealias CourseFilterContract = CourseCatalogContract
typealias CourseDetailContract = CourseCatalogContract

object InstructorCatalogContract {
    data class InstructorItem(
        val name: String,
        val department: String,
        val title: String = "Professor",
        val isActive: Boolean = true,
        val teachingScore: Double = 0.0,
        val email: String = "",
        val averageRating: Double = teachingScore
    )

    data class InstructorRecord(
        val id: String,
        val nameEn: String,
        val nameZh: String?,
        val department: String,
        val isStaff: Boolean,
        val coursesTaught: List<String>,
        val email: String? = null,
        val biography: String? = null,
        val averageRating: Double? = null
    )

    data class InstructorProfile(val name: String, val department: String, val email: String)

    fun matchesSearch(instructor: InstructorItem, query: String): Boolean {
        if (query.isBlank()) return true
        return instructor.name.contains(query.trim(), ignoreCase = true)
    }

    fun filterActive(instructors: List<InstructorItem>): List<InstructorItem> {
        return instructors.filter { it.isActive }
    }

    fun filterByDepartment(instructors: List<InstructorItem>, dept: String): List<InstructorItem> {
        return instructors.filter { it.department.equals(dept, ignoreCase = true) }
    }

    fun sortByScore(instructors: List<InstructorItem>, descending: Boolean = true): List<InstructorItem> {
        val comparator = compareBy<InstructorItem> { it.teachingScore }
        return if (descending) instructors.sortedWith(comparator.reversed()) else instructors.sortedWith(comparator)
    }

    fun calculateOverallRating(ratings: List<Double>): Double? {
        if (ratings.isEmpty()) return null
        return (ratings.average() * 100.0).toInt() / 100.0
    }

    fun parseSlashDelimitedInstructors(raw: String): List<String> {
        if (raw.isBlank()) return emptyList()
        return raw.split("\\s*/\\s*".toRegex()).filter { it.isNotBlank() }
    }

    fun searchInstructors(query: String?, instructors: List<InstructorRecord>, includeInactive: Boolean): List<InstructorRecord> {
        val filtered = if (includeInactive) instructors else instructors.filter { it.isStaff }
        if (query.isNullOrBlank()) return filtered
        val sanitized = query.trim()
        val regex = Regex(Regex.escape(sanitized), RegexOption.IGNORE_CASE)
        return filtered.filter { inst ->
            regex.containsMatchIn(inst.nameEn) ||
            (inst.nameZh != null && regex.containsMatchIn(inst.nameZh)) ||
            regex.containsMatchIn(inst.department)
        }
    }

    fun searchInstructors(instructors: List<InstructorItem>, query: String?, department: String?): List<InstructorItem> {
        return instructors.filter { inst ->
            (query == null || inst.name.contains(query.trim(), ignoreCase = true)) &&
            (department == null || inst.department.equals(department.trim(), ignoreCase = true))
        }
    }

    fun truncateBio(bio: String?, maxChars: Int = 150): String {
        if (bio.isNullOrBlank()) return ""
        val trimmed = bio.trim()
        return if (trimmed.length <= maxChars) trimmed else trimmed.take(maxChars) + "..."
    }
}

typealias InstructorContract = InstructorCatalogContract
typealias InstructorDetailContract = InstructorCatalogContract

object ReviewSubmissionContract {
    val DIMENSIONS = listOf("workload", "difficulty", "grading", "teaching")

    data class ReviewSummary(
        val courseCode: String,
        val grade: String,
        val ratings: Map<String, Double>,
        val upvotes: Int,
        val downvotes: Int
    )

    fun validateReview(review: ReviewSummary): Boolean {
        if (review.courseCode.isBlank()) return false
        if (review.grade !in GradeMathContract.GRADE_TO_GPA_MAP.keys) return false
        return review.ratings.values.all { RatingValidatorContract.isValidRating(it) }
    }

    fun computeNetScore(upvotes: Int, downvotes: Int): Int = upvotes - downvotes

    enum class Step(val order: Int) {
        COURSE_INFO(1), COURSE_RATINGS(2), INSTRUCTOR_RATINGS(3), SETTINGS(4), PREVIEW(5)
    }

    fun validateStep1(courseCode: String?, termCode: String?): Boolean {
        return !courseCode.isNullOrBlank() && !termCode.isNullOrBlank()
    }

    data class WizardDraft(
        val step: Int,
        val courseCode: String?,
        val instructorName: String? = null,
        val workloadRating: Double? = null,
        val difficultyRating: Double? = null,
        val teachingRating: Double? = null,
        val comment: String? = null,
        val finalGrade: String? = null
    )

    data class StepValidation(val canProceed: Boolean, val errorMessageRes: String? = null)

    fun validateStep(draft: WizardDraft): StepValidation {
        return when (draft.step) {
            1 -> {
                if (draft.courseCode.isNullOrBlank()) StepValidation(false, "review.selectCourseRequired")
                else StepValidation(true)
            }
            2 -> {
                if (draft.workloadRating == null || draft.difficultyRating == null) {
                    StepValidation(false, "review.ratingRequired")
                } else {
                    val valid = RatingValidatorContract.isValidRating(draft.workloadRating) && RatingValidatorContract.isValidRating(draft.difficultyRating)
                    if (!valid) StepValidation(false, "review.ratingRequired") else StepValidation(true)
                }
            }
            3 -> {
                if (draft.teachingRating == null) {
                    StepValidation(false, "review.instructorRatingRequired")
                } else {
                    if (!RatingValidatorContract.isValidRating(draft.teachingRating)) StepValidation(false, "review.instructorRatingRequired")
                    else StepValidation(true)
                }
            }
            4 -> {
                if (draft.comment == null) {
                    StepValidation(false, "review.commentRequired")
                } else {
                    val res = WordCountValidatorContract.validateWordCount(draft.comment, 5, 1000)
                    if (!res.isValid) StepValidation(false, res.message) else StepValidation(true)
                }
            }
            else -> StepValidation(true)
        }
    }

    data class WizardStepPayload(
        val courseCode: String? = null,
        val termCode: String? = null,
        val instructorName: String? = null,
        val ratingWorkload: Double? = null,
        val ratingDifficulty: Double? = null,
        val ratingTeaching: Double? = null,
        val gradeReceived: String? = null,
        val comment: String? = null
    )

    fun validateStep1(payload: WizardStepPayload): Boolean =
        !payload.courseCode.isNullOrBlank() && !payload.termCode.isNullOrBlank()

    fun validateStep2(payload: WizardStepPayload): Boolean =
        payload.ratingWorkload != null && RatingValidatorContract.isValidRating(payload.ratingWorkload) &&
        payload.ratingDifficulty != null && RatingValidatorContract.isValidRating(payload.ratingDifficulty)

    fun validateStep3(payload: WizardStepPayload): Boolean =
        payload.ratingTeaching != null && RatingValidatorContract.isValidRating(payload.ratingTeaching)

    fun validateStep4(payload: WizardStepPayload): Boolean =
        payload.gradeReceived != null && GradeMathContract.GRADE_TO_GPA_MAP.containsKey(payload.gradeReceived.trim())

    fun validateStep5(payload: WizardStepPayload): Boolean =
        payload.comment != null && WordCountValidatorContract.validateWordCount(payload.comment).isValid

    data class DisplayReview(
        val id: String,
        val authorDisplay: String,
        val isAnonymous: Boolean,
        val comment: String,
        val overallRating: Double,
        val canOpenProfile: Boolean
    )

    fun formatReview(
        id: String,
        authorName: String?,
        isAnonymous: Boolean,
        isAuthorDeleted: Boolean,
        comment: String,
        workload: Double,
        difficulty: Double,
        grading: Double,
        teaching: Double
    ): DisplayReview {
        val author = when {
            isAnonymous -> "Anonymous Student"
            isAuthorDeleted -> "[Deleted User]"
            authorName.isNullOrBlank() -> "Lingnan Student"
            else -> authorName
        }
        val ratings = listOf(workload, difficulty, grading, teaching).filter { it >= 0.5 }
        val overall = if (ratings.isEmpty()) 0.0 else ratings.average()
        val truncatedComment = comment.trim().take(2000)
        return DisplayReview(
            id = id,
            authorDisplay = author,
            isAnonymous = isAnonymous,
            comment = truncatedComment,
            overallRating = overall,
            canOpenProfile = !isAnonymous && !isAuthorDeleted
        )
    }
}

typealias ReviewFeedContract = ReviewSubmissionContract
typealias ReviewWizardContract = ReviewSubmissionContract

object StorageBucketContract {
    fun isValidBucket(bucketId: String): Boolean = bucketId in AppwriteConfigContract.STORAGE_BUCKETS

    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0L) return "0 B"
        val kb = 1024L
        val mb = kb * 1024L
        val gb = mb * 1024L
        return when {
            bytes >= gb -> String.format(java.util.Locale.US, "%.1f GB", bytes.toDouble() / gb)
            bytes >= mb -> String.format(java.util.Locale.US, "%.1f MB", bytes.toDouble() / mb)
            bytes >= kb -> {
                if (bytes % kb == 0L) {
                    "${bytes / kb} KB"
                } else {
                    String.format(java.util.Locale.US, "%.1f KB", bytes.toDouble() / kb)
                }
            }
            else -> "$bytes B"
        }
    }

    fun getMimeType(fileName: String): String {
        return when {
            fileName.endsWith(".pdf", ignoreCase = true) -> "application/pdf"
            fileName.endsWith(".jpg", ignoreCase = true) || fileName.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg"
            fileName.endsWith(".png", ignoreCase = true) -> "image/png"
            fileName.endsWith(".doc", ignoreCase = true) || fileName.endsWith(".docx", ignoreCase = true) -> "application/msword"
            else -> "application/octet-stream"
        }
    }

    fun resolveMimeType(filename: String): String = getMimeType(filename)

    fun isAllowedDocumentType(fileName: String): Boolean {
        val safeExtensions = listOf(".pdf", ".doc", ".docx", ".ppt", ".pptx", ".txt")
        return safeExtensions.any { fileName.endsWith(it, ignoreCase = true) }
    }

    data class CacheControlInfo(val isPublic: Boolean, val maxAgeSeconds: Long)

    fun parseCacheControl(header: String): CacheControlInfo {
        val isPublic = header.contains("public", ignoreCase = true)
        val maxAgeMatch = "max-age=(\\d+)".toRegex(RegexOption.IGNORE_CASE).find(header)
        val maxAge = maxAgeMatch?.groupValues?.get(1)?.toLongOrNull() ?: 0L
        return CacheControlInfo(isPublic, maxAge)
    }

    fun isPdf(filename: String?, mimeType: String? = null): Boolean {
        if (mimeType == "application/pdf") return true
        return filename?.endsWith(".pdf", ignoreCase = true) == true
    }

    sealed class DocumentAction {
        data class OpenInApp(val viewUrl: String) : DocumentAction()
        data class OpenBrowser(val downloadUrl: String) : DocumentAction()
        data class Error(val message: String) : DocumentAction()
    }

    fun resolveDocumentAction(fileId: String, bucketId: String, isPdf: Boolean, hasNativeViewer: Boolean, isSessionActive: Boolean): DocumentAction {
        if (!isSessionActive) return DocumentAction.Error("auth.sessionExpired")
        val viewUrl = AppwriteConfigContract.buildViewUrl(bucketId, fileId)
        val downloadUrl = AppwriteConfigContract.buildDownloadUrl(bucketId, fileId)
        return if (isPdf && hasNativeViewer) DocumentAction.OpenInApp(viewUrl) else DocumentAction.OpenBrowser(downloadUrl)
    }

    enum class StorageError { None, PermissionDenied, FileNotFound, Unknown }

    fun mapHttpStatus(status: Int): StorageError = when (status) {
        403 -> StorageError.PermissionDenied
        404 -> StorageError.FileNotFound
        else -> StorageError.Unknown
    }
}

typealias FileUtilityContract = StorageBucketContract
typealias DocumentViewerContract = StorageBucketContract
typealias DocumentStorageContract = StorageBucketContract
