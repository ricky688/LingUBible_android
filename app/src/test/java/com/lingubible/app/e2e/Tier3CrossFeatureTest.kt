package com.lingubible.app.e2e

import com.lingubible.app.core.navigation.Screen
import com.lingubible.app.core.theme.DarkColorScheme
import com.lingubible.app.core.theme.LightColorScheme
import com.lingubible.app.core.theme.LingnanRed
import com.lingubible.app.core.util.I18n
import com.lingubible.app.e2e.contracts.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

/**
 * Tier 3: Cross-Feature Combinations Test Suite.
 * Covers 24 interaction pairs across the platform's core functional modules.
 */
class Tier3CrossFeatureTest {

    @Test
    fun testCross01_emailAuthToLoginToProfile() {
        val studentEmail = "student2024@ln.hk"
        assertTrue(EmailValidatorContract.isValidLingnanEmail(studentEmail))

        val authScreen = Screen.Auth(mode = "login")
        val jsonAuth = Json.encodeToString<Screen>(authScreen)
        val decodedAuth = Json.decodeFromString<Screen>(jsonAuth) as Screen.Auth
        assertEquals("login", decodedAuth.mode)

        val profileState = UserProfileContract.UserProfileState(
            userId = "usr_99",
            animal = "bear",
            bgIndex = 3,
            favorites = emptyList()
        )
        assertTrue(UserProfileContract.isValidAnimal(profileState.animal))
        assertEquals(3, UserProfileContract.clampBackgroundIndex(profileState.bgIndex))

        val profileScreen = Screen.Profile
        val decodedProfile = Json.decodeFromString<Screen>(Json.encodeToString<Screen>(profileScreen))
        assertEquals(Screen.Profile, decodedProfile)
    }

    @Test
    fun testCross02_authSessionToReviewSubmission() {
        val isValidSession = AuthValidatorContract.isValidSessionToken("64f1a2b3c4d5e6f7a1b2c3d4e5f6g7h8")
        assertTrue(isValidSession)

        val draft = ReviewSubmissionContract.WizardDraft(
            step = 4,
            courseCode = "CDS2004",
            comment = "Solid introduction to analytics with practical exercises."
        )
        val validation = ReviewSubmissionContract.validateStep(draft)
        assertTrue(validation.canProceed)
        assertTrue(WordCountValidatorContract.countWords(draft.comment) >= 5)
    }

    @Test
    fun testCross03_authSessionToReviewVotingGuard() {
        val machine = AuthValidatorContract.SessionStateMachine()
        assertFalse(machine.currentState.isAuthenticated)

        machine.login("user_100", "sess_active_123")
        assertTrue(machine.currentState.isAuthenticated)

        val initial = ReviewVotingContract.VoteState(5, 1, null)
        val voted = ReviewVotingContract.applyVote(initial, "up")
        assertEquals(6, voted.upvotes)
        assertEquals("up", voted.userVote)

        val switched = ReviewVotingContract.applyVote(voted, "down")
        assertEquals(5, switched.upvotes)
        assertEquals(2, switched.downvotes)
        assertEquals("down", switched.userVote)
    }

    @Test
    fun testCross04_authSessionToPastPapersBucketAccess() {
        val actionGuest = StorageBucketContract.resolveDocumentAction(
            fileId = "paper_1",
            bucketId = "past_exam_papers",
            isPdf = true,
            hasNativeViewer = true,
            isSessionActive = false
        )
        assertTrue(actionGuest is StorageBucketContract.DocumentAction.Error)

        val parsed = PastPapersParserContract.parseFilename("CDS2004_24252.pdf")
        assertNotNull(parsed)
        assertEquals("2024-2025", parsed!!.academicYear)

        val actionAuth = StorageBucketContract.resolveDocumentAction(
            fileId = "CDS2004_24252.pdf",
            bucketId = "past_exam_papers",
            isPdf = true,
            hasNativeViewer = true,
            isSessionActive = true
        )
        assertTrue(actionAuth is StorageBucketContract.DocumentAction.OpenInApp)
        assertEquals("past_exam_papers", AppwriteConfigContract.STORAGE_BUCKETS[1])
    }

    @Test
    fun testCross05_authSessionToDocumentDownloadUrl() {
        val fileId = "paper_2024_01"
        val downloadUrl = AppwriteConfigContract.buildDownloadUrl("past_exam_papers", fileId)
        assertTrue(downloadUrl.contains(fileId))
        assertTrue(downloadUrl.contains(AppwriteConfigContract.PROJECT_ID))
    }

    @Test
    fun testCross06_courseBrowsingToKeywordSearch() {
        val catalog = listOf(
            CourseCatalogContract.CourseItem("CDS2004", "Data Science Programming"),
            CourseCatalogContract.CourseItem("BUS1102", "Financial Accounting"),
            CourseCatalogContract.CourseItem("CDS3001", "Artificial Intelligence")
        )
        val results = CourseCatalogContract.searchCourses("Data Science", catalog)
        assertEquals(1, results.size)
        assertEquals("CDS2004", results.first().courseCode)

        val detailScreen = Screen.CourseDetail(courseCode = results.first().courseCode)
        val decoded = Json.decodeFromString<Screen>(Json.encodeToString<Screen>(detailScreen)) as Screen.CourseDetail
        assertEquals("CDS2004", decoded.courseCode)
    }

    @Test
    fun testCross07_courseBrowsingToMultiCriteriaFilter() {
        val catalog = listOf(
            CourseCatalogContract.CourseItem("CDS2004", "Data Science", term = "202409"),
            CourseCatalogContract.CourseItem("BUS1102", "Statistics", term = "202409"),
            CourseCatalogContract.CourseItem("CDS3001", "AI", term = "202501")
        )
        val filtered = CourseCatalogContract.filterCourses(
            catalog,
            CourseCatalogContract.FilterCriteria(subjectPrefix = "CDS", term = "202409")
        )
        assertEquals(1, filtered.size)
        assertEquals("CDS2004", filtered[0].courseCode)

        val paged = CourseCatalogContract.paginate(filtered, page = 1, pageSize = 10)
        assertEquals(1, paged.items.size)
    }

    @Test
    fun testCross08_searchAndFilterAndSortingPipeline() {
        val courses = listOf(
            CourseCatalogContract.CourseItem("ECO2101", "Microeconomics", languageCode = "E", averageRating = 3.2),
            CourseCatalogContract.CourseItem("ECO2102", "Macroeconomics", languageCode = "E", averageRating = 3.7),
            CourseCatalogContract.CourseItem("ECO3101", "Managerial Economics", languageCode = "C", averageRating = 3.9)
        )
        val searched = CourseCatalogContract.searchCourses("Economics", courses)
        val filtered = CourseCatalogContract.filterCourses(searched, CourseCatalogContract.FilterCriteria(languageCode = "E"))
        val sorted = CourseCatalogContract.sortCourses(filtered, CourseCatalogContract.SortBy.RATING, ascending = false)

        assertEquals(2, sorted.size)
        assertEquals("ECO2102", sorted[0].courseCode)
    }

    @Test
    fun testCross09_courseCatalogToCourseDetailTransition() {
        val course = CourseCatalogContract.CourseItem("CDS2004", "Data Science Programming")
        val detailRoute = Screen.CourseDetail(course.courseCode)
        val decoded = Json.decodeFromString<Screen>(Json.encodeToString<Screen>(detailRoute)) as Screen.CourseDetail
        assertEquals("CDS2004", decoded.courseCode)

        val tabs = CourseCatalogContract.TABS
        assertEquals(7, tabs.size)
        assertTrue(tabs.contains("overview"))
    }

    @Test
    fun testCross10_instructorCatalogToInstructorDetailTransition() {
        val instructor = InstructorCatalogContract.InstructorItem("Richard SIMMONS", "English")
        val route = Screen.InstructorDetail(instructor.name)
        val decoded = Json.decodeFromString<Screen>(Json.encodeToString<Screen>(route)) as Screen.InstructorDetail
        assertEquals("Richard SIMMONS", decoded.name)

        val teachingRecords = listOf("ENG1001", "ENG2002")
        assertEquals(2, teachingRecords.size)
    }

    @Test
    fun testCross11_courseDetailTeachingHistoryToInstructorDetail() {
        val rawInstructors = "Dr. Richard SIMMONS / Prof. MAO Ying"
        val parsedList = InstructorCatalogContract.parseSlashDelimitedInstructors(rawInstructors)
        assertEquals(2, parsedList.size)

        val targetInstructor = parsedList[1]
        val route = Screen.InstructorDetail(targetInstructor)
        val decoded = Json.decodeFromString<Screen>(Json.encodeToString<Screen>(route)) as Screen.InstructorDetail
        assertEquals("Prof. MAO Ying", decoded.name)
    }

    @Test
    fun testCross12_reviewFeedDisplayToVotingReaction() {
        val initialVoteState = ReviewVotingContract.VoteState(
            upvotes = 10,
            downvotes = 1,
            userVote = null
        )
        val afterUpvote = ReviewVotingContract.applyVote(initialVoteState, "up")
        assertEquals(11, afterUpvote.upvotes)
        assertEquals("up", afterUpvote.userVote)
    }

    @Test
    fun testCross13_reviewFeedToGradeStatsAggregation() {
        val reviews = listOf(
            ReviewSubmissionContract.ReviewSummary("CDS2004", "A", emptyMap(), 0, 0),
            ReviewSubmissionContract.ReviewSummary("CDS2004", "A-", emptyMap(), 0, 0),
            ReviewSubmissionContract.ReviewSummary("CDS2004", "B+", emptyMap(), 0, 0)
        )
        val dist = mutableMapOf<String, Int>()
        reviews.forEach { r ->
            dist[r.grade] = (dist[r.grade] ?: 0) + 1
        }
        val stats = GradeMathContract.calculateGradeStatistics(dist)
        assertEquals(3, stats.validGradeCount)
        assertTrue(stats.mean!! > 3.5)
    }

    @Test
    fun testCross14_gradeStatsToParetoCurveChartSeries() {
        val distribution = mapOf("A" to 5, "B" to 5, "C" to 5)
        val stats = GradeMathContract.calculateGradeStatistics(distribution)
        val curve = GradeMathContract.calculateParetoCurve(distribution)
        assertEquals(15, stats.validGradeCount)
        assertEquals(11, curve.size)
        val lastPoint = curve.first { it.grade == "F" }
        assertEquals(100.0, lastPoint.cumulativePercentage, 0.001)
    }

    @Test
    fun testCross15_gradeChartBarClickToReviewFeedFilter() {
        val allReviews = listOf(
            ReviewSubmissionContract.ReviewSummary("CDS2004", "A", mapOf("workload" to 3.0), 5, 0),
            ReviewSubmissionContract.ReviewSummary("CDS2004", "B", mapOf("workload" to 3.0), 2, 0),
            ReviewSubmissionContract.ReviewSummary("CDS2004", "A", mapOf("workload" to 4.0), 3, 0)
        )
        val clickedBarGrade = "A"
        val filtered = allReviews.filter { it.grade == clickedBarGrade }
        assertEquals(2, filtered.size)

        val stats = GradeMathContract.calculateGradeStatistics(mapOf("A" to filtered.size))
        assertEquals(4.00, stats.mean!!, 0.001)
    }

    @Test
    fun testCross16_reviewWizardToHalfStarRatingsValidation() {
        val wizardRatings = mapOf(
            "workload" to 3.5,
            "difficulty" to 4.0,
            "teaching" to 4.5
        )
        val allValid = wizardRatings.values.all { RatingValidatorContract.isValidRating(it) }
        assertTrue(allValid)
    }

    @Test
    fun testCross17_reviewWizardToAntiSpamWordCountValidation() {
        var comment = "Bad"
        var validation = WordCountValidatorContract.validateWordCount(comment, 5, 1000)
        assertFalse(validation.isValid)

        comment = "This course provides excellent practical assignments and hands-on laboratory experience."
        validation = WordCountValidatorContract.validateWordCount(comment, 5, 1000)
        assertTrue(validation.isValid)
    }

    @Test
    fun testCross18_antiSpamRetakeRuleToReviewFeedDisplay() {
        val userReviews = mutableListOf(
            AntiSpamRulesContract.ExistingCourseReview("rev_1", "BUS1102", "F")
        )
        val eligibility = AntiSpamRulesContract.checkReviewEligibility(
            userId = "user_fail",
            courseCode = "BUS1102",
            termCode = "202501",
            termReviewCount = 1,
            existingCourseReviews = userReviews
        )
        assertTrue(eligibility.canSubmit)

        userReviews.add(AntiSpamRulesContract.ExistingCourseReview("rev_2", "BUS1102", "A-"))
        assertEquals(2, userReviews.size)
    }

    @Test
    fun testCross19_antiSpamTermLimitToWizardStepBlock() {
        val eligibility = AntiSpamRulesContract.checkReviewEligibility(
            userId = "prolific_reviewer",
            courseCode = "NEW202",
            termCode = "202409",
            termReviewCount = 7,
            existingCourseReviews = emptyList()
        )
        assertFalse(eligibility.canSubmit)
        assertEquals("review.termLimitExceeded", eligibility.reason)
    }

    @Test
    fun testCross20_pastPapersListToDocumentViewUrl() {
        val parsed = PastPapersParserContract.parseFilename("CDS2004_24252.pdf")
        assertNotNull(parsed)
        val viewUrl = AppwriteConfigContract.buildViewUrl("past_exam_papers", "file_cds_01")
        assertTrue(viewUrl.contains("past_exam_papers"))
        assertTrue(viewUrl.contains("file_cds_01"))
    }

    @Test
    fun testCross21_studyMaterialsListToDocumentDownloadUrl() {
        val downloadUrl = AppwriteConfigContract.buildDownloadUrl("study_materials", "notes_week1")
        assertTrue(downloadUrl.contains("study_materials"))
        assertTrue(downloadUrl.contains("download"))
    }

    @Test
    fun testCross22_multilingualLocalizationToMaterialThemeSwitch() {
        val resName = I18n.keyToResName("courses.title")
        assertEquals("courses_title", resName)
        assertEquals(LingnanRed, LightColorScheme.primary)
        assertNotNull(DarkColorScheme.primary)
        val contrastRatio = ColorContrastContract.calculateContrastRatio(
            DarkColorScheme.onSurface.red, DarkColorScheme.onSurface.green, DarkColorScheme.onSurface.blue,
            DarkColorScheme.surface.red, DarkColorScheme.surface.green, DarkColorScheme.surface.blue
        )
        assertTrue("Dark theme contrast ratio >= 4.5", contrastRatio >= 4.5)
    }

    @Test
    fun testCross23_navigationToUserProfileAvatarUpdate() {
        val route = Screen.Profile
        val decoded = Json.decodeFromString<Screen>(Json.encodeToString<Screen>(route))
        assertEquals(Screen.Profile, decoded)

        assertTrue(UserProfileContract.isValidAnimal("panda"))
        val clamped = UserProfileContract.clampBackgroundIndex(15)
        assertEquals(11, clamped)

        val state = UserProfileContract.UserProfileState("user_1", "panda", clamped, emptyList())
        assertEquals("panda", state.animal)
    }

    @Test
    fun testCross24_courseDetailTabSwitching() {
        val tabs = CourseCatalogContract.TABS
        assertTrue(tabs.contains("overview"))
        assertTrue(tabs.contains("materials"))
        assertTrue(tabs.contains("exams"))

        val evalWithExams = CourseCatalogContract.evaluateTabs(pastPapersCount = 2)
        assertTrue(evalWithExams.hasExams)
        val evalNoExams = CourseCatalogContract.evaluateTabs(pastPapersCount = 0)
        assertFalse(evalNoExams.hasExams)
    }
}
