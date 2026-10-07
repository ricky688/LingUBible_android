package com.lingubible.app

import com.lingubible.app.domain.model.*
import com.lingubible.app.domain.repository.*
import com.lingubible.app.ui.viewmodels.CourseDetailViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CourseDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeCourseRepo : CourseRepository {
        var courseToReturn: Course? = Course(
            id = "c1",
            code = "CDS2004",
            titleEn = "Natural Language Processing",
            titleZh = "自然語言處理",
            reviewCount = 10,
            avgRating = 4.5,
            avgGrade = "3.67"
        )

        override suspend fun getCourses(search: String?, subject: String?, page: Int): Result<List<Course>> =
            Result.success(listOfNotNull(courseToReturn))

        override suspend fun getCourseByCode(courseCode: String): Result<Course> =
            courseToReturn?.let { Result.success(it) } ?: Result.failure(NoSuchElementException())

        override suspend fun getTeachingRecords(courseCode: String): Result<List<TeachingRecord>> =
            Result.success(listOf(TeachingRecord("tr1", "CDS2004", "Dr. Chan", "陳博士", "Term 1", "2024-2025", "1")))

        override suspend fun getGradeDistribution(courseCode: String): Result<Map<String, Int>> =
            Result.success(mapOf("A" to 5, "B" to 3))
    }

    private class FakeReviewRepo : ReviewRepository {
        override suspend fun getReviewsForCourse(courseCode: String): Result<List<Review>> =
            Result.success(listOf(Review("r1", "CDS2004", comment = "Great course!")))

        override suspend fun getReviewsForInstructor(instructorName: String): Result<List<Review>> =
            Result.success(emptyList())

        override suspend fun getLatestReviews(limit: Int): Result<List<Review>> =
            Result.success(emptyList())

        override suspend fun submitReview(submission: ReviewSubmission): Result<Review> =
            Result.success(Review("r2", submission.courseCode, comment = submission.comment))

        override suspend fun voteReview(reviewId: String, voteType: String): Result<VoteState> =
            Result.success(VoteState(upvotes = 3, downvotes = 1, userVote = voteType))

        override suspend fun checkEligibility(userId: String, courseCode: String, termCode: String): EligibilityResult =
            EligibilityResult(true)
    }

    private class FakeMaterialRepo : MaterialRepository {
        var syllabusToReturn: CourseSyllabus? = CourseSyllabus(
            id = "syl_123",
            courseCode = "CDS2004",
            fileName = "CDS2004-202601.pdf",
            viewUrl = "https://appwrite.lingubible.com/v1/storage/buckets/course_syllabus/files/syl_123/view?project=6a1097400037a55f6472",
            downloadUrl = "https://appwrite.lingubible.com/v1/storage/buckets/course_syllabus/files/syl_123/download?project=6a1097400037a55f6472",
            fileSize = 125000L
        )

        var pastPapersToReturn: List<PastPaper> = listOf(
            PastPaper(
                id = "pp1",
                courseCode = "CDS2004",
                academicYear = "2024-2025",
                term = "Term 2",
                fileId = "pp1",
                fileName = "CDS2004_24252.pdf",
                fileSize = 51200L,
                viewUrl = "https://appwrite.lingubible.com/v1/storage/buckets/past_exam_papers/files/pp1/view?project=6a1097400037a55f6472",
                downloadUrl = "https://appwrite.lingubible.com/v1/storage/buckets/past_exam_papers/files/pp1/download?project=6a1097400037a55f6472"
            )
        )

        override suspend fun getPastPapers(courseCode: String): Result<List<PastPaper>> =
            Result.success(pastPapersToReturn)

        override suspend fun getDownloadUrl(fileId: String): String = "download/$fileId"
        override suspend fun getViewUrl(fileId: String): String = "view/$fileId"
        override suspend fun getSyllabus(courseCode: String): Result<CourseSyllabus?> =
            Result.success(syllabusToReturn)

        override suspend fun getSyllabusViewUrl(fileId: String): String = "syllabus/view/$fileId"
        override suspend fun getSyllabusDownloadUrl(fileId: String): String = "syllabus/download/$fileId"
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadCourseDetail loads course, past papers, and syllabus successfully`() = runTest {
        val courseRepo = FakeCourseRepo()
        val reviewRepo = FakeReviewRepo()
        val materialRepo = FakeMaterialRepo()
        val vm = CourseDetailViewModel(courseRepo, reviewRepo, materialRepo)

        vm.loadCourseDetail("CDS2004")
        testScheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isSyllabusLoading)
        assertNotNull(state.course)
        assertEquals("CDS2004", state.course!!.code)

        assertEquals(1, state.pastPapers.size)
        assertEquals("CDS2004", state.pastPapers.first().courseCode)
        assertEquals("2024-2025", state.pastPapers.first().academicYear)
        assertEquals("Term 2", state.pastPapers.first().term)

        assertNotNull(state.syllabus)
        assertEquals("syl_123", state.syllabus!!.id)
        assertEquals("CDS2004-202601.pdf", state.syllabus!!.fileName)
    }

    @Test
    fun `loadCourseDetail handles course with no syllabus as null fallback state`() = runTest {
        val courseRepo = FakeCourseRepo()
        val reviewRepo = FakeReviewRepo()
        val materialRepo = FakeMaterialRepo().apply {
            syllabusToReturn = null
        }
        val vm = CourseDetailViewModel(courseRepo, reviewRepo, materialRepo)

        vm.loadCourseDetail("CDS2004")
        testScheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isSyllabusLoading)
        assertNull(state.syllabus)
    }

    @Test
    fun `resolveAndOpenSyllabus triggers callback immediately when syllabus is cached`() = runTest {
        val courseRepo = FakeCourseRepo()
        val reviewRepo = FakeReviewRepo()
        val materialRepo = FakeMaterialRepo()
        val vm = CourseDetailViewModel(courseRepo, reviewRepo, materialRepo)

        vm.loadCourseDetail("CDS2004")
        testScheduler.advanceUntilIdle()

        var callbackSyllabus: CourseSyllabus? = null
        vm.resolveAndOpenSyllabus("CDS2004") { syl ->
            callbackSyllabus = syl
        }

        assertNotNull(callbackSyllabus)
        assertEquals("syl_123", callbackSyllabus!!.id)
    }

    @Test
    fun `resolveAndOpenSyllabus fetches on-demand when syllabus is not yet loaded`() = runTest {
        val courseRepo = FakeCourseRepo()
        val reviewRepo = FakeReviewRepo()
        val materialRepo = FakeMaterialRepo()
        val vm = CourseDetailViewModel(courseRepo, reviewRepo, materialRepo)

        var callbackSyllabus: CourseSyllabus? = null
        vm.resolveAndOpenSyllabus("CDS2004") { syl ->
            callbackSyllabus = syl
        }
        testScheduler.advanceUntilIdle()

        assertNotNull(callbackSyllabus)
        assertEquals("syl_123", callbackSyllabus!!.id)
        assertNotNull(vm.uiState.value.syllabus)
    }

    @Test
    fun `voteReview updates review in state`() = runTest {
        val courseRepo = FakeCourseRepo()
        val reviewRepo = FakeReviewRepo()
        val materialRepo = FakeMaterialRepo()
        val vm = CourseDetailViewModel(courseRepo, reviewRepo, materialRepo)

        vm.loadCourseDetail("CDS2004")
        testScheduler.advanceUntilIdle()

        vm.voteReview("r1", "up")
        testScheduler.advanceUntilIdle()

        val updatedReview = vm.uiState.value.reviews.first { it.id == "r1" }
        assertEquals("up", updatedReview.userVote)
        assertEquals(3, updatedReview.upvotes)
    }

    @Test
    fun `loadCourseDetail resets loading states and captures error when unexpected exception occurs`() = runTest {
        val courseRepo = object : CourseRepository by FakeCourseRepo() {
            override suspend fun getCourseByCode(courseCode: String): Result<Course> {
                throw IllegalStateException("Unexpected crash")
            }
        }
        val reviewRepo = FakeReviewRepo()
        val materialRepo = FakeMaterialRepo()
        val vm = CourseDetailViewModel(courseRepo, reviewRepo, materialRepo)

        vm.loadCourseDetail("CDS2004")
        testScheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isSyllabusLoading)
        assertEquals("Unexpected crash", state.errorMessage)
    }

    @Test
    fun `CourseDetailViewModel observes currentUser changes from AuthRepository`() = runTest {
        val courseRepo = FakeCourseRepo()
        val reviewRepo = FakeReviewRepo()
        val materialRepo = FakeMaterialRepo()
        val fakeUserFlow = kotlinx.coroutines.flow.MutableStateFlow<User?>(null)
        val authRepo = object : AuthRepository {
            override val currentUser: kotlinx.coroutines.flow.StateFlow<User?> = fakeUserFlow
            override suspend fun login(email: String, password: String): Result<Session> = Result.failure(NotImplementedError())
            override suspend fun register(email: String, password: String, name: String): Result<User> = Result.failure(NotImplementedError())
            override suspend fun loginWithGoogle(activity: androidx.activity.ComponentActivity): Result<User> = Result.failure(NotImplementedError())
            override suspend fun logout(): Result<Unit> = Result.success(Unit)
            override suspend fun checkSession(): Result<User?> = Result.success(fakeUserFlow.value)
            override fun isValidEmail(email: String): Boolean = true
        }

        val vm = CourseDetailViewModel(courseRepo, reviewRepo, materialRepo, authRepo)
        assertNull(vm.uiState.value.currentUser)

        val testUser = User(id = "u1", name = "Test Student", email = "test@ln.hk")
        fakeUserFlow.value = testUser
        testScheduler.advanceUntilIdle()

        assertEquals(testUser, vm.uiState.value.currentUser)
    }
}
