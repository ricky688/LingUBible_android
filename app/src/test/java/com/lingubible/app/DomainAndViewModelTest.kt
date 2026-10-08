package com.lingubible.app

import com.lingubible.app.domain.model.*
import com.lingubible.app.domain.repository.*
import androidx.activity.ComponentActivity
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
    // Past Paper Filename Parser & FileUtils Tests
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

    @Test
    fun `past paper parses summer terms, instructor suffixes, and expanded formats`() {
        val summerS = PastPapersParser.parseFilename("CDS2004_2425S.pdf")
        assertNotNull(summerS)
        assertEquals("2024-2025", summerS!!.academicYear)
        assertEquals("Summer Term", summerS.term)

        val summer0 = PastPapersParser.parseFilename("CDS2004_24250.pdf")
        assertNotNull(summer0)
        assertEquals("Summer Term", summer0!!.term)

        val summer3 = PastPapersParser.parseFilename("CDS2004_24253.pdf")
        assertNotNull(summer3)
        assertEquals("Summer Term", summer3!!.term)

        val instructorShort = PastPapersParser.parseFilename("CDS2004_24251_Simmons.pdf")
        assertNotNull(instructorShort)
        assertEquals("Simmons", instructorShort!!.instructor)
        assertEquals("Term 1", instructorShort.term)

        val instructorExpanded = PastPapersParser.parseFilename("BUS1102_2023-2024_Term1_ProfChan.pdf")
        assertNotNull(instructorExpanded)
        assertEquals("ProfChan", instructorExpanded!!.instructor)
        assertEquals("2023-2024", instructorExpanded.academicYear)
        assertEquals("Term 1", instructorExpanded.term)

        val shortEndYear = PastPapersParser.parseFilename("BUS1102_2023-24_Term1.pdf")
        assertNotNull(shortEndYear)
        assertEquals("2023-2024", shortEndYear!!.academicYear)

        val fourLetterCode = PastPapersParser.parseFilename("MGSL4001_24252.pdf")
        assertNotNull(fourLetterCode)
        assertEquals("MGSL4001", fourLetterCode!!.courseCode)

        val letterSuffix = PastPapersParser.parseFilename("HST3366e_24251.pdf")
        assertNotNull(letterSuffix)
        assertEquals("HST3366E", letterSuffix!!.courseCode)

        val semFormat = PastPapersParser.parseFilename("BUS1102_2023-2024_Sem1.pdf")
        assertNotNull(semFormat)
        assertEquals("Term 1", semFormat!!.term)

        val semesterFormat = PastPapersParser.parseFilename("BUS1102_2023-2024_Semester2.pdf")
        assertNotNull(semesterFormat)
        assertEquals("Term 2", semesterFormat!!.term)

        val hyphenInstructor = PastPapersParser.parseFilename("CDS2004_24251-Simmons.pdf")
        assertNotNull(hyphenInstructor)
        assertEquals("Simmons", hyphenInstructor!!.instructor)

        val twoDigitYearRange = PastPapersParser.parseFilename("BUS1102_23-24_Term1.pdf")
        assertNotNull(twoDigitYearRange)
        assertEquals("2023-2024", twoDigitYearRange!!.academicYear)

        val trailingWhitespace = PastPapersParser.parseFilename("CDS2004_24252 .pdf")
        assertNotNull(trailingWhitespace)
        assertEquals("Term 2", trailingWhitespace!!.term)

        // Compact year with separated term
        val compactYearTerm = PastPapersParser.parseFilename("CDS2004_2425_Term1.pdf")
        assertNotNull(compactYearTerm)
        assertEquals("2024-2025", compactYearTerm!!.academicYear)
        assertEquals("Term 1", compactYearTerm.term)

        val compactYearSem = PastPapersParser.parseFilename("CDS2004_2425_Sem2.pdf")
        assertNotNull(compactYearSem)
        assertEquals("Term 2", compactYearSem!!.term)

        val compactYearT = PastPapersParser.parseFilename("CDS2004_2425_T1.pdf")
        assertNotNull(compactYearT)
        assertEquals("Term 1", compactYearT!!.term)

        val compactYearSummer = PastPapersParser.parseFilename("CDS2004_2425_Summer.pdf")
        assertNotNull(compactYearSummer)
        assertEquals("Summer Term", compactYearSummer!!.term)

        val compactYearDigit = PastPapersParser.parseFilename("CDS2004_2425_1.pdf")
        assertNotNull(compactYearDigit)
        assertEquals("Term 1", compactYearDigit!!.term)

        // Hyphen and spaced instructor separators
        val spacedHyphenInstructor = PastPapersParser.parseFilename("CDS2004_24251 - Simmons.pdf")
        assertNotNull(spacedHyphenInstructor)
        assertEquals("Simmons", spacedHyphenInstructor!!.instructor)

        val expandedSpacedInstructor = PastPapersParser.parseFilename("BUS1102_2023-2024_Term1 - Chan.pdf")
        assertNotNull(expandedSpacedInstructor)
        assertEquals("Chan", expandedSpacedInstructor!!.instructor)

        // Parenthesized instructor names
        val parenInstructor = PastPapersParser.parseFilename("CDS2004_24251(Simmons).pdf")
        assertNotNull(parenInstructor)
        assertEquals("Simmons", parenInstructor!!.instructor)

        val expandedParenInstructor = PastPapersParser.parseFilename("BUS1102_2023-2024_Term1 (Chan).pdf")
        assertNotNull(expandedParenInstructor)
        assertEquals("Chan", expandedParenInstructor!!.instructor)

        // 8-digit years and underscore year separators
        val eightDigitYear = PastPapersParser.parseFilename("BUS1102_20242025_Term1.pdf")
        assertNotNull(eightDigitYear)
        assertEquals("2024-2025", eightDigitYear!!.academicYear)

        val underscoreYear = PastPapersParser.parseFilename("BUS1102_2023_2024_Term1.pdf")
        assertNotNull(underscoreYear)
        assertEquals("2023-2024", underscoreYear!!.academicYear)

        val hyphenT = PastPapersParser.parseFilename("BUS1102_2023-2024_T-1.pdf")
        assertNotNull(hyphenT)
        assertEquals("Term 1", hyphenT!!.term)

        // Space-separated course code and year
        val spaceHyphenCode = PastPapersParser.parseFilename("CDS2004 - 24251.pdf")
        assertNotNull(spaceHyphenCode)
        assertEquals("CDS2004", spaceHyphenCode!!.courseCode)
        assertEquals("2024-2025", spaceHyphenCode.academicYear)
        assertEquals("Term 1", spaceHyphenCode.term)

        val spaceCode = PastPapersParser.parseFilename("CDS2004 24251.pdf")
        assertNotNull(spaceCode)
        assertEquals("CDS2004", spaceCode!!.courseCode)
        assertEquals("2024-2025", spaceCode.academicYear)
        assertEquals("Term 1", spaceCode.term)

        // Exam type keywords are not mistaken for instructor names
        val finalExamPaper = PastPapersParser.parseFilename("CDS2004_24252_Final.pdf")
        assertNotNull(finalExamPaper)
        assertEquals("Term 2", finalExamPaper!!.term)
        assertNull(finalExamPaper.instructor)

        val midtermPaper = PastPapersParser.parseFilename("CDS2004_24251_Midterm.pdf")
        assertNotNull(midtermPaper)
        assertEquals("Term 1", midtermPaper!!.term)
        assertNull(midtermPaper.instructor)

        val solutionPaper = PastPapersParser.parseFilename("BUS1102_2023-2024_Term1_Solution.pdf")
        assertNotNull(solutionPaper)
        assertNull(solutionPaper!!.instructor)

        val finalWithInstructor = PastPapersParser.parseFilename("CDS2004_24251_Final_Simmons.pdf")
        assertNotNull(finalWithInstructor)
        assertEquals("Simmons", finalWithInstructor!!.instructor)

        // Single 4-digit year format (e.g. 2024_Term1 -> 2024-2025, not 2020-2024)
        val singleYearTerm1 = PastPapersParser.parseFilename("CDS2004_2024_Term1.pdf")
        assertNotNull(singleYearTerm1)
        assertEquals("2024-2025", singleYearTerm1!!.academicYear)
        assertEquals("Term 1", singleYearTerm1.term)

        val singleYearTerm2 = PastPapersParser.parseFilename("BUS1102_2024_Term2.pdf")
        assertNotNull(singleYearTerm2)
        assertEquals("2023-2024", singleYearTerm2!!.academicYear)
        assertEquals("Term 2", singleYearTerm2.term)
    }

    @Test
    fun `past paper term sort key orders newest first with unknown last`() {
        val key2024T2 = PastPapersParser.getTermSortKey("2024-2025", "Term 2")
        val key2024T1 = PastPapersParser.getTermSortKey("2024-2025", "Term 1")
        val key2023T2 = PastPapersParser.getTermSortKey("2023-2024", "Term 2")
        val keyUnknown = PastPapersParser.getTermSortKey("Unknown", "Unknown")

        assertTrue(key2024T2 > key2024T1)
        assertTrue(key2024T1 > key2023T2)
        assertTrue(key2023T2 > keyUnknown)
        assertEquals(0, keyUnknown)
    }

    @Test
    fun `past paper parser rejects malformed filenames`() {
        assertNull(PastPapersParser.parseFilename("CDS200424252.pdf"))
        assertNull(PastPapersParser.parseFilename("old_syllabus_doc.pdf"))
        assertNull(PastPapersParser.parseFilename("random.txt"))
        assertNull(PastPapersParser.parseFilename(""))
    }

    @Test
    fun `file utils formats file size accurately`() {
        assertEquals("0 B", FileUtils.formatFileSize(0L))
        assertEquals("1 KB", FileUtils.formatFileSize(1024L))
        assertEquals("200 KB", FileUtils.formatFileSize(204800L))
        assertEquals("1.5 MB", FileUtils.formatFileSize(1572864L))
        assertEquals("15.0 MB", FileUtils.formatFileSize(15728640L))
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

        override suspend fun loginWithGoogle(activity: ComponentActivity): Result<User> =
            Result.failure(UnsupportedOperationException("Not used by this fake"))

        override suspend fun logout(): Result<Unit> {
            _currentUser.value = null
            return Result.success(Unit)
        }

        override suspend fun checkSession(): Result<User?> = Result.success(_currentUser.value)

        override suspend fun updateName(name: String): Result<User> {
            val cur = _currentUser.value ?: return Result.failure(IllegalStateException("No user"))
            val updated = cur.copy(name = name)
            _currentUser.value = updated
            return Result.success(updated)
        }

        override suspend fun updatePassword(newPassword: String, oldPassword: String): Result<Unit> =
            Result.success(Unit)

        override suspend fun isGoogleLinked(): Result<Boolean> = Result.success(true)

        override suspend fun unlinkGoogle(): Result<Unit> = Result.success(Unit)

        override fun isValidEmail(email: String): Boolean = EmailValidator.isValidLingnanEmail(email)
    }

    private class FakeAvatarRepository : AvatarRepository {
        private val _currentAvatar = MutableStateFlow<CustomAvatar?>(null)
        override val currentAvatar: StateFlow<CustomAvatar?> = _currentAvatar

        override suspend fun getUserAvatar(userId: String): Result<CustomAvatar?> {
            val av = _currentAvatar.value ?: AvatarPresets.getDefaultAvatar(userId)
            return Result.success(av)
        }

        override suspend fun saveUserAvatar(userId: String, animal: String, backgroundIndex: Int): Result<CustomAvatar> {
            val av = CustomAvatar(animal, backgroundIndex)
            _currentAvatar.value = av
            return Result.success(av)
        }

        override suspend fun deleteUserAvatar(userId: String): Result<Unit> {
            _currentAvatar.value = null
            return Result.success(Unit)
        }

        override fun getCachedAvatar(userId: String): CustomAvatar? = _currentAvatar.value
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
        val authRepo = FakeAuthRepository()
        val avatarRepo = FakeAvatarRepository()
        val vm = AuthViewModel(authRepo, avatarRepo)

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
    fun `avatar presets and custom avatar workflow`() = runTest {
        assertEquals(60, AvatarPresets.CUTE_AVATARS.size)
        assertEquals(60, AvatarPresets.BACKGROUND_COLORS.size)
        assertTrue(AvatarPresets.CUTE_AVATARS.contains("🐢"))

        val defaultAvatar = AvatarPresets.getDefaultAvatar("user123")
        assertNotNull(defaultAvatar.animal)
        assertTrue(defaultAvatar.backgroundIndex in 0..59)

        val authRepo = FakeAuthRepository()
        val avatarRepo = FakeAvatarRepository()
        val vm = AuthViewModel(authRepo, avatarRepo)

        // Login first
        vm.login("student@ln.hk", "pass1234") {}
        testScheduler.advanceUntilIdle()

        // Update custom avatar to turtle + background 57
        var avatarSaved = false
        vm.saveCustomAvatar("🐢", 57) { avatarSaved = it }
        testScheduler.advanceUntilIdle()
        assertTrue(avatarSaved)
        assertEquals("🐢", vm.uiState.value.currentAvatar?.animal)
        assertEquals(57, vm.uiState.value.currentAvatar?.backgroundIndex)

        // Update username
        var usernameUpdated = false
        vm.updateUsername("ricky", onSuccess = { usernameUpdated = true }, onError = {})
        testScheduler.advanceUntilIdle()
        assertTrue(usernameUpdated)
        assertEquals("ricky", vm.uiState.value.currentUser?.name)
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
