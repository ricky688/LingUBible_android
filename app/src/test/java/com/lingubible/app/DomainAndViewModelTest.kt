package com.lingubible.app

import com.lingubible.app.domain.model.*
import com.lingubible.app.domain.repository.*
import com.lingubible.app.domain.util.*
import com.lingubible.app.ui.viewmodels.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DomainAndViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ====================================================
    // Email Validation Tests
    // ====================================================
    @Test
    fun `valid lingnan emails are accepted`() {
        assertTrue(EmailValidator.isValidLingnanEmail("student@ln.hk"))
        assertTrue(EmailValidator.isValidLingnanEmail("prof.smith@ln.edu.hk"))
        assertTrue(EmailValidator.isValidLingnanEmail("USER.123@LN.HK"))
    }

    @Test
    fun `non-lingnan emails are rejected`() {
        assertFalse(EmailValidator.isValidLingnanEmail("user@gmail.com"))
        assertFalse(EmailValidator.isValidLingnanEmail("user@hku.hk"))
        assertFalse(EmailValidator.isValidLingnanEmail("user@ln.com"))
        assertFalse(EmailValidator.isValidLingnanEmail(""))
        assertFalse(EmailValidator.isValidLingnanEmail(null))
    }

    // ====================================================
    // Word Count Validation Tests
    // ====================================================
    @Test
    fun `word count counts whitespace correctly`() {
        assertEquals(0, WordCountValidator.countWords(""))
        assertEquals(0, WordCountValidator.countWords("   "))
        assertEquals(5, WordCountValidator.countWords("This is a simple test."))
        assertEquals(3, WordCountValidator.countWords("One\nTwo\tThree"))
    }

    @Test
    fun `word count validator requires between 5 and 1000 words`() {
        assertFalse(WordCountValidator.isValid("Too short", 5, 1000))
        assertTrue(WordCountValidator.isValid("This is five words now", 5, 1000))
        val bigText = (1..1005).joinToString(" ") { "word" }
        assertFalse(WordCountValidator.isValid(bigText, 5, 1000))
    }

    // ====================================================
    // Rating Validator Tests
    // ====================================================
    @Test
    fun `rating validator allows half star increments and NA`() {
        assertTrue(RatingValidator.isValidRating(-1.0))
        assertTrue(RatingValidator.isValidRating(0.5))
        assertTrue(RatingValidator.isValidRating(1.0))
        assertTrue(RatingValidator.isValidRating(2.5))
        assertTrue(RatingValidator.isValidRating(5.0))

        assertFalse(RatingValidator.isValidRating(0.0))
        assertFalse(RatingValidator.isValidRating(0.3))
        assertFalse(RatingValidator.isValidRating(5.5))
    }

    // ====================================================
    // Grade Calculator Math Tests
    // ====================================================
    @Test
    fun `grade statistics calculates mean and standard deviation accurately`() {
        val distribution = mapOf(
            "A" to 2,  // 4.0 * 2 = 8.0
            "B" to 2,  // 3.0 * 2 = 6.0
            "C" to 1   // 2.0 * 1 = 2.0
            // Sum = 16.0 / 5 = 3.20
        )

        val stats = GradeCalculator.calculateGradeStatistics(distribution)
        assertEquals(5, stats.validGradeCount)
        assertEquals(3.20, stats.mean!!, 0.001)
        assertTrue(stats.standardDeviation!! > 0)
    }

    @Test
    fun `grade statistics handles empty and NA distribution`() {
        val distribution = mapOf("N/A" to 3)
        val stats = GradeCalculator.calculateGradeStatistics(distribution)
        assertNull(stats.mean)
        assertNull(stats.standardDeviation)
        assertEquals(3, stats.totalCount)
        assertEquals(0, stats.validGradeCount)
    }

    @Test
    fun `pareto curve produces cumulative 100 percent`() {
        val distribution = mapOf("A" to 5, "B" to 5)
        val curve = GradeCalculator.calculateParetoCurve(distribution)
        assertFalse(curve.isEmpty())
        assertEquals(100.0, curve.last().cumulativePercentage, 0.001)
    }

    // ====================================================
    // Past Paper Filename Parser Tests
    // ====================================================
    @Test
    fun `past paper parses filename correctly`() {
        val parsedA = PastPapersParser.parseFilename("CLC9001_23241_ProfChan.pdf")
        assertNotNull(parsedA)
        assertEquals("CLC9001", parsedA!!.courseCode)
        assertEquals("2023-2024", parsedA.academicYear)
        assertEquals("Term 1", parsedA.term)
        assertEquals("ProfChan", parsedA.instructor)

        val parsedB = PastPapersParser.parseFilename("BUS1102_2022-2023_Term2.pdf")
        assertNotNull(parsedB)
        assertEquals("BUS1102", parsedB!!.courseCode)
        assertEquals("2022-2023", parsedB.academicYear)
        assertEquals("Term 2", parsedB.term)
        assertNull(parsedB.instructor)
    }

    // ====================================================
    // Fake Repositories & ViewModel Tests
    // ====================================================
    private class FakeAuthRepository : AuthRepository {
        private val _currentUser = MutableStateFlow<User?>(null)
        override val currentUser: StateFlow<User?> = _currentUser

        override suspend fun login(email: String, password: String): Result<Session> {
            if (!isValidEmail(email)) return Result.failure(IllegalArgumentException("Invalid email"))
            val user = User(id = "user123", name = "Test User", email = email)
            _currentUser.value = user
            return Result.success(Session(id = "sess123", userId = user.id))
        }

        override suspend fun register(email: String, password: String, name: String): Result<User> {
            val user = User(id = "user123", name = name, email = email)
            return Result.success(user)
        }

        override suspend fun logout(): Result<Unit> {
            _currentUser.value = null
            return Result.success(Unit)
        }

        override suspend fun checkSession(): Result<User?> = Result.success(_currentUser.value)
        override fun isValidEmail(email: String): Boolean = EmailValidator.isValidLingnanEmail(email)
    }

    private class FakeCourseRepository : CourseRepository {
        val courses = listOf(
            Course(id = "1", code = "CLC9001", titleEn = "Chinese Language"),
            Course(id = "2", code = "BUS1102", titleEn = "Statistics for Business")
        )

        override suspend fun getCourses(search: String?, subject: String?, page: Int): Result<List<Course>> {
            var filtered = courses
            if (!search.isNullOrBlank()) {
                filtered = filtered.filter { it.code.contains(search, ignoreCase = true) }
            }
            if (!subject.isNullOrBlank()) {
                filtered = filtered.filter { it.code.startsWith(subject, ignoreCase = true) }
            }
            return Result.success(filtered)
        }

        override suspend fun getCourseByCode(courseCode: String): Result<Course> {
            val c = courses.firstOrNull { it.code == courseCode }
            return if (c != null) Result.success(c) else Result.failure(NoSuchElementException())
        }

        override suspend fun getTeachingRecords(courseCode: String): Result<List<TeachingRecord>> =
            Result.success(emptyList())

        override suspend fun getGradeDistribution(courseCode: String): Result<Map<String, Int>> =
            Result.success(mapOf("A" to 5, "B" to 3))
    }

    private class FakeReviewRepository : ReviewRepository {
        val reviews = mutableListOf(
            Review(id = "rev1", courseCode = "CLC9001", comment = "Great course! Learned a lot from this class.")
        )

        override suspend fun getReviewsForCourse(courseCode: String): Result<List<Review>> =
            Result.success(reviews.filter { it.courseCode == courseCode })

        override suspend fun getReviewsForInstructor(instructorName: String): Result<List<Review>> =
            Result.success(reviews)

        override suspend fun getLatestReviews(limit: Int): Result<List<Review>> =
            Result.success(reviews)

        override suspend fun submitReview(submission: ReviewSubmission): Result<Review> {
            val r = Review(
                id = "rev_${reviews.size + 1}",
                courseCode = submission.courseCode,
                comment = submission.comment
            )
            reviews.add(r)
            return Result.success(r)
        }

        override suspend fun voteReview(reviewId: String, voteType: String): Result<VoteState> =
            Result.success(VoteState(upvotes = 1, downvotes = 0, userVote = voteType))

        override suspend fun checkEligibility(userId: String, courseCode: String, termCode: String): EligibilityResult =
            EligibilityResult(true)
    }

    @Test
    fun `auth viewmodel validates email and performs login`() = runTest {
        val repo = FakeAuthRepository()
        val vm = AuthViewModel(repo)

        var successCalled = false
        vm.login("invalid@gmail.com", "pass1234") { successCalled = true }
        assertFalse(successCalled)
        assertNotNull(vm.uiState.value.errorMessage)

        vm.login("student@ln.hk", "pass1234") { successCalled = true }
        testScheduler.advanceUntilIdle()
        assertTrue(successCalled)
        assertNull(vm.uiState.value.errorMessage)
        assertEquals("student@ln.hk", vm.uiState.value.currentUser?.email)
    }

    @Test
    fun `courses viewmodel filters by search and subject`() = runTest {
        val repo = FakeCourseRepository()
        val vm = CoursesViewModel(repo)
        testScheduler.advanceUntilIdle()

        assertEquals(2, vm.uiState.value.courses.size)

        vm.search("CLC")
        testScheduler.advanceUntilIdle()
        assertEquals(1, vm.uiState.value.courses.size)
        assertEquals("CLC9001", vm.uiState.value.courses[0].code)

        vm.filterSubject("BUS")
        testScheduler.advanceUntilIdle()
        assertEquals(0, vm.uiState.value.courses.size) // No course matches CLC search AND BUS subject

        vm.search("")
        testScheduler.advanceUntilIdle()
        assertEquals(1, vm.uiState.value.courses.size)
        assertEquals("BUS1102", vm.uiState.value.courses[0].code)
    }

    @Test
    fun `write review viewmodel validates word count before submission`() = runTest {
        val repo = FakeReviewRepository()
        val vm = WriteReviewViewModel(repo)

        vm.setInitialCourse("CLC9001")
        vm.updateComment("Too short")
        assertFalse(vm.uiState.value.isWordCountValid)

        var submitted = false
        vm.submitReview { submitted = true }
        assertFalse(submitted)
        assertNotNull(vm.uiState.value.errorMessage)

        vm.updateComment("This is a valid review with more than five words.")
        assertTrue(vm.uiState.value.isWordCountValid)

        vm.submitReview { submitted = true }
        testScheduler.advanceUntilIdle()
        assertTrue(submitted)
        assertEquals(2, repo.reviews.size)
    }
}
