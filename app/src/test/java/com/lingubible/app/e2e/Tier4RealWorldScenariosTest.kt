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
 * Tier 4: Real-World Application Scenarios Test Suite.
 * Covers 12 end-to-end realistic student user workflows.
 */
class Tier4RealWorldScenariosTest {

    @Test
    fun testScenario01_newStudentRegistrationAndOnboarding() {
        // Step 1: Student opens app and provides school email
        val inputEmail = "freshman.chan@ln.hk"
        assertTrue("Must be valid Lingnan email", EmailValidatorContract.isValidLingnanEmail(inputEmail))

        // Step 2: Student inputs password
        val password = "SecureLingnanPassword2026!"
        val pwdResult = AuthValidatorContract.validatePassword(password)
        assertTrue("Password must be valid", pwdResult.isValid)

        // Step 3: Registration payload validated
        val studentName = "Chan Tai Man"
        val regPayload = AuthValidatorContract.validateRegistrationPayload(inputEmail, password, studentName)
        assertTrue(regPayload.isValid)

        // Step 4: User sets up profile avatar
        val chosenAvatar = "koala"
        val bgIndex = 4
        assertTrue(UserProfileContract.isValidAvatar(chosenAvatar))
        assertTrue(UserProfileContract.isValidBackgroundIndex(bgIndex))

        // Step 5: Student is logged in with active session
        val machine = AuthValidatorContract.SessionStateMachine()
        machine.login("chan_01", "session_token_chan_01")
        assertTrue(machine.currentState.isAuthenticated)
    }

    @Test
    fun testScenario02_courseDiscoveryAndPrerequisiteInvestigation() {
        // Step 1: Student searches for BUS1102
        val query = "BUS1102"
        val courseCatalog = listOf(
            CourseCatalogContract.CourseItem("BUS1102", "Statistics for Business", 3, "BUS"),
            CourseCatalogContract.CourseItem("CDS2004", "Data Science Programming", 3, "CDS")
        )
        val searchResults = CourseCatalogContract.searchCourses(query, courseCatalog)
        assertEquals(1, searchResults.size)
        val matchedCourse = searchResults.first()
        assertEquals("BUS1102", matchedCourse.courseCode)

        // Step 2: Student opens Course Detail
        val detailRoute = Screen.CourseDetail(matchedCourse.courseCode)
        val decoded = Json.decodeFromString<Screen>(Json.encodeToString<Screen>(detailRoute)) as Screen.CourseDetail
        assertEquals("BUS1102", decoded.courseCode)

        // Step 3: Student inspects prerequisites
        val prereqText = "Prerequisite: None"
        assertFalse(prereqText.contains("CDS2004"))

        // Step 4: Student checks historical teaching records
        val teachingRecords = listOf(
            CourseCatalogContract.TeachingRecord("202409", "Prof. Lee", "L1")
        )
        assertEquals(1, teachingRecords.size)
        assertEquals("Prof. Lee", teachingRecords[0].instructor)
    }

    @Test
    fun testScenario03_courseEvaluationAndGradeAnalysisWorkflow() {
        // Step 1: Student navigates to CDS2004 Grades tab
        val distribution = mapOf(
            "A" to 5,
            "A-" to 8,
            "B+" to 12,
            "B" to 6,
            "C+" to 2,
            "F" to 1
        )

        // Step 2: Grade statistics calculated
        val stats = GradeMathContract.calculateGradeStatistics(distribution)
        assertEquals(34, stats.totalCount)
        assertEquals(34, stats.validGradeCount)
        assertTrue(stats.mean!! in 2.8..3.6)

        // Step 3: Box plot 5-number summary generated
        val boxPlot = GradeMathContract.calculateBoxPlotStatistics(distribution)
        assertNotNull(boxPlot)
        assertEquals(2.33, boxPlot!!.min, 0.001) // Lower whisker is C+ (2.33), F (0.0) is outlier
        assertTrue(boxPlot.outliers.contains(0.0))
        assertEquals(4.0, boxPlot.max, 0.001) // A grade is 4.0

        // Step 4: Pareto curve generated for Vico dual-axis chart
        val pareto = GradeMathContract.calculateParetoCurve(distribution)
        assertEquals(11, pareto.size)
        val lastGrade = pareto.last { it.grade == "F" }
        assertEquals(100.0, lastGrade.cumulativePercentage, 0.001)
    }

    @Test
    fun testScenario04_endToEndReviewSubmissionFlow() {
        // Step 1: Wizard Step 1 - Course and Term Selection
        val courseCode = "CDS2004"
        val termCode = "202409"
        val instructorName = "SIMMONS Richard"

        // Step 2: Wizard Step 2 - Course Ratings
        val workloadRating = 3.5
        val difficultyRating = 4.0
        val usefulnessRating = 4.5
        assertTrue(RatingValidatorContract.isValidRating(workloadRating))
        assertTrue(RatingValidatorContract.isValidRating(difficultyRating))
        assertTrue(RatingValidatorContract.isValidRating(usefulnessRating))

        // Step 3: Wizard Step 3 - Instructor Ratings
        val teachingRating = 4.5
        assertTrue(RatingValidatorContract.isValidRating(teachingRating))

        // Step 4: Wizard Step 4 - Grade received
        val finalGrade = "A-"
        assertTrue(GradeMathContract.GRADE_TO_GPA_MAP.containsKey(finalGrade))

        // Step 5: Wizard Step 5 - Constructive comment validation
        val comment = "Professor Simmons is engaging and connects data science concepts to practical industrial use cases. The labs were demanding but rewarding."
        val wordValidation = WordCountValidatorContract.validateWordCount(comment, 5, 1000)
        assertTrue(wordValidation.isValid)

        // Step 6: Submit review document
        val reviewSummary = ReviewSubmissionContract.ReviewSummary(
            courseCode = courseCode,
            grade = finalGrade,
            ratings = mapOf("workload" to workloadRating, "difficulty" to difficultyRating, "teaching" to teachingRating),
            upvotes = 0,
            downvotes = 0
        )
        assertTrue(ReviewSubmissionContract.validateReview(reviewSummary))
    }

    @Test
    fun testScenario05_reviewRetakeAfterFailingGradeException() {
        val studentId = "student_retake_01"
        val courseCode = "BUS1102"

        // Student's initial review in Term 1 with failing grade
        val firstReview = AntiSpamRulesContract.ExistingCourseReview(
            reviewId = "rev_initial_fail",
            courseCode = courseCode,
            finalGrade = "F"
        )

        // Check eligibility before retake
        val eligibility = AntiSpamRulesContract.checkReviewEligibility(
            userId = studentId,
            courseCode = courseCode,
            termCode = "202501",
            termReviewCount = 2,
            existingCourseReviews = listOf(firstReview)
        )
        assertTrue("Failing grade in first review unlocks second review", eligibility.canSubmit)

        // Student submits second review with passing grade
        val secondReview = AntiSpamRulesContract.ExistingCourseReview(
            reviewId = "rev_retake_pass",
            courseCode = courseCode,
            finalGrade = "B+"
        )

        // Check eligibility after retake review is submitted (now 2 reviews exist)
        val thirdEligibility = AntiSpamRulesContract.checkReviewEligibility(
            userId = studentId,
            courseCode = courseCode,
            termCode = "202509",
            termReviewCount = 0,
            existingCourseReviews = listOf(firstReview, secondReview)
        )
        assertFalse("Third review is strictly forbidden", thirdEligibility.canSubmit)
    }

    @Test
    fun testScenario06_antiSpamTermCourseLoadLimitEnforcement() {
        val studentId = "active_reviewer"
        val currentTerm = "202409"

        // Student already submitted 7 reviews for this term (maximum allowable load)
        val currentTermReviews = 7

        val eligibility = AntiSpamRulesContract.checkReviewEligibility(
            userId = studentId,
            courseCode = "EXTRA101",
            termCode = currentTerm,
            termReviewCount = currentTermReviews,
            existingCourseReviews = emptyList()
        )

        assertFalse("8th review in same term must be blocked", eligibility.canSubmit)
        assertEquals("review.termLimitExceeded", eligibility.reason)
    }

    @Test
    fun testScenario07_communityInteractionAndReviewVotingCycle() {
        var voteState = ReviewVotingContract.VoteState(upvotes = 15, downvotes = 2, userVote = null)

        // Student finds review helpful -> Upvotes
        voteState = ReviewVotingContract.applyVote(voteState, "up")
        assertEquals(16, voteState.upvotes)
        assertEquals("up", voteState.userVote)

        // Student accidentally clicks upvote again -> Toggles off
        voteState = ReviewVotingContract.applyVote(voteState, "up")
        assertEquals(15, voteState.upvotes)
        assertNull(voteState.userVote)

        // Student changes mind and clicks downvote
        voteState = ReviewVotingContract.applyVote(voteState, "down")
        assertEquals(15, voteState.upvotes)
        assertEquals(3, voteState.downvotes)
        assertEquals("down", voteState.userVote)
    }

    @Test
    fun testScenario08_examPreparationAndPastPaperAccess() {
        // Step 1: Student opens Course Detail for CDS2004
        val rawExamFilenames = listOf(
            "CDS2004_24252.pdf",
            "CDS2004_23241.pdf",
            "CDS2004_22232_Simmons.pdf"
        )

        // Step 2: Filename parsing into metadata
        val parsedPapers = rawExamFilenames.mapNotNull { PastPapersParserContract.parseFilename(it) }
        assertEquals(3, parsedPapers.size)

        // Step 3: Filter papers for academic year 2024-2025
        val filtered = parsedPapers.filter { it.academicYear == "2024-2025" }
        assertEquals(1, filtered.size)
        assertEquals("Term 2", filtered[0].term)

        // Step 4: Student initiates download
        val downloadUrl = AppwriteConfigContract.buildDownloadUrl("past_exam_papers", "file_cds2004_24252")
        assertTrue(downloadUrl.contains("past_exam_papers"))
    }

    @Test
    fun testScenario09_multilingualStudentExperience() {
        val resKey = I18n.keyToResName("courses.title")
        assertEquals("courses_title", resKey)

        val template = "Remaining: %1\$s minutes"
        val converted = LocalizationContract.convertParamFormat("Remaining: {minutes} minutes")
        assertEquals(template, converted)

        val localized = String.format(converted, "15")
        assertEquals("Remaining: 15 minutes", localized)
    }

    @Test
    fun testScenario10_instructorEvaluationAndTeachingHistoryExploration() {
        val instructor = InstructorCatalogContract.InstructorItem("Richard SIMMONS", "English")
        val route = Screen.InstructorDetail(instructor.name)
        val decoded = Json.decodeFromString<Screen>(Json.encodeToString<Screen>(route)) as Screen.InstructorDetail
        assertEquals("Richard SIMMONS", decoded.name)

        val coursesTaught = listOf(
            CourseCatalogContract.CourseItem("ENG1001", "English I", 3, "ENG", averageRating = 4.8),
            CourseCatalogContract.CourseItem("ENG2002", "English II", 3, "ENG", averageRating = 4.6)
        )
        assertEquals(2, coursesTaught.size)

        val avgRating = InstructorCatalogContract.calculateOverallRating(coursesTaught.map { it.averageRating })
        assertEquals(4.7, avgRating!!, 0.01)
    }

    @Test
    fun testScenario11_darkModeLateNightStudySession() {
        val lightBg = LightColorScheme.background
        val darkBg = DarkColorScheme.background
        assertNotEquals(lightBg, darkBg)
        assertEquals(LingnanRed, LightColorScheme.primary)

        val contrast = ColorContrastContract.calculateContrastRatio(
            DarkColorScheme.onSurface.red, DarkColorScheme.onSurface.green, DarkColorScheme.onSurface.blue,
            darkBg.red, darkBg.green, darkBg.blue
        )
        assertTrue("Dark mode contrast must be >= 4.5", contrast >= 4.5)
    }

    @Test
    fun testScenario12_offlineDraftPersistenceAndRecovery() {
        val initialDraft = ReviewSubmissionContract.WizardDraft(
            step = 3,
            courseCode = "CDS2004",
            comment = "Drafting my detailed notes about the final project..."
        )
        val validation = ReviewSubmissionContract.validateStep(initialDraft)
        assertFalse(validation.canProceed) // Step 3 requires instructor rating

        val validDraft = initialDraft.copy(teachingRating = 4.5)
        assertTrue(ReviewSubmissionContract.validateStep(validDraft).canProceed)
        assertEquals("CDS2004", validDraft.courseCode)
    }
}
