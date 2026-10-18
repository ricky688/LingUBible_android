package com.lingubible.app

import android.util.Log
import com.lingubible.app.data.remote.AppwriteClientProvider
import com.lingubible.app.data.repository.AppwriteReviewRepository
import com.lingubible.app.domain.model.*
import io.appwrite.Query
import io.appwrite.models.Document
import io.appwrite.models.DocumentList
import io.appwrite.services.Account
import io.appwrite.services.Databases
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Assert.*
import org.junit.Test

class ReviewRepositoryParityTest {
    private val provider = mockk<AppwriteClientProvider>()
    private val db = mockk<Databases>()
    private val account = mockk<Account>()
    private lateinit var repository: AppwriteReviewRepository
    private val evaluation = InstructorEvaluation("Lecturer", "Lecture", 0.0, -1.0,
        "The lecturer explained the topics clearly", setOf("has_final"))
    private val form = ReviewForm("BUS1102", "202510", "A", 0.0, -1.0, 4.5,
        "This course helped me understand the subject", instructors = listOf(evaluation))
    @Before fun setup() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0; every { Log.e(any(), any(), any()) } returns 0; every { Log.w(any(), any(), any()) } returns 0
        every { provider.databaseId } returns "lingubible"; every { provider.databases } returns db
        every { provider.account } returns account; every { provider.functions } returns null
        coEvery { account.get() } returns mockk { every { id } returns "owner"; every { name } returns "Student" }
        coEvery { db.listDocuments("lingubible", "review_votes", any()) } returns DocumentList(0, emptyList())
        repository = AppwriteReviewRepository(provider)
    }
    @After fun cleanup() { unmockkStatic(Log::class) }
    private fun document(id: String, fields: Map<String, Any> = emptyMap()): Document<Map<String, Any>> = mockk {
        every { this@mockk.id } returns id; every { data } returns fields; every { createdAt } returns "2026-10-16T00:00:00Z"
    }
    @Test fun `course browsing pages past SDK default while preserving the course constraint`() = runTest {
        val calls = mutableListOf<List<String>?>()
        coEvery { db.listDocuments("lingubible", "reviews", captureNullable(calls)) } returnsMany listOf(
            DocumentList(101, (1..100).map { document("r$it") }), DocumentList(101, listOf(document("older"))))
        assertEquals(101, repository.getReviewsForCourse(" bus1102 ").getOrThrow().size)
        calls.forEach { assertTrue(it!!.contains(Query.equal("course_code", "BUS1102"))) }
        assertTrue(calls.last()!!.contains(Query.cursorAfter("r100")))
    }
    @Test fun `instructor browsing includes secondary instructors beyond old 200 review cutoff`() = runTest {
        val old = form.copy(instructors = listOf(evaluation, evaluation.copy(name = "Second", session = "Tutorial")))
            .documentData("owner", "Student", "now")
        coEvery { db.listDocuments("lingubible", "reviews", any()) } returnsMany listOf(
            DocumentList(201, (1..100).map { document("a$it") }),
            DocumentList(201, (1..100).map { document("b$it") }), DocumentList(201, listOf(document("older", old))))
        val result = repository.getReviewsForInstructor("Second").getOrThrow()
        assertEquals(listOf("older"), result.map { it.id }); assertEquals(2, result.single().instructors.size)
        assertEquals(0.0, result.single().workloadRating, 0.0); assertEquals(-1.0, result.single().difficultyRating, 0.0)
    }
    @Test fun `eligibility cannot fail open on timeout`() = runTest {
        coEvery { db.listDocuments(any(), any(), any()) } throws IllegalStateException("timeout")
        assertFalse(repository.checkEligibility("owner", "BUS1102", "202510").canSubmit)
    }
    @Test fun `stale account cannot create a review`() = runTest {
        assertTrue(repository.submitReview(ReviewSubmission("other", form)).isFailure)
        coVerify(exactly = 0) { db.createDocument(any(), any(), any(), any()) }
        coVerify(exactly = 0) { db.listDocuments(any(), any(), any()) }
    }
    private fun records() = listOf(document("record", mapOf("course_code" to "BUS1102", "term_code" to "202510", "instructor_name" to "Lecturer", "session_type" to "Lecture")))
    @Test fun `submission rechecks membership and limits before writing web compatible payload`() = runTest {
        coEvery { db.listDocuments("lingubible", "teaching_records", any()) } returns DocumentList(1, records())
        coEvery { db.listDocuments("lingubible", "reviews", any()) } returns DocumentList(0, emptyList())
        val data = slot<Map<String, Any>>()
        coEvery { db.createDocument("lingubible", "reviews", any(), capture(data)) } answers { document("created", data.captured) }
        val result = repository.submitReview(ReviewSubmission("owner", form)).getOrThrow()
        assertEquals("created", result.id); assertEquals("owner", data.captured["user_id"])
        assertEquals(4.5, data.captured["course_usefulness"]); assertEquals(-1.0, result.gradingRating, 0.0)
        coVerify(exactly = 2) { db.listDocuments("lingubible", "reviews", any()) }
    }
    @Test fun `unknown session or term limit refuses write`() = runTest {
        coEvery { db.listDocuments("lingubible", "teaching_records", any()) } returns DocumentList(1, records())
        assertTrue(repository.submitReview(ReviewSubmission("owner", form.copy(term = "wrong"))).isFailure)
        coEvery { db.listDocuments("lingubible", "reviews", any()) } returns DocumentList(7, emptyList())
        assertTrue(repository.submitReview(ReviewSubmission("owner", form)).isFailure)
        coVerify(exactly = 0) { db.createDocument(any(), any(), any(), any()) }
    }
    @Test fun `vote totals include every page and selected account vote`() = runTest {
        val auth = mockk<com.lingubible.app.domain.repository.AuthRepository>()
        every { auth.currentUser } returns kotlinx.coroutines.flow.MutableStateFlow(User("owner", "Student", "a@ln.hk"))
        coEvery { db.listDocuments("lingubible", "reviews", any()) } returns DocumentList(1, listOf(document("review")))
        val voteCalls = mutableListOf<List<String>?>()
        coEvery { db.listDocuments("lingubible", "review_votes", captureNullable(voteCalls)) } returnsMany listOf(
            DocumentList(101, (1..100).map { document("v$it", mapOf("review_id" to "review", "vote_type" to "up", "user_id" to "other$it")) }),
            DocumentList(101, listOf(document("mine", mapOf("review_id" to "review", "vote_type" to "down", "user_id" to "owner")))))
        val review = AppwriteReviewRepository(provider, auth).getAllReviewsForBrowsing().getOrThrow().single()
        assertEquals(100, review.upvotes); assertEquals(1, review.downvotes); assertEquals("down", review.userVote)
        assertTrue(voteCalls.last()!!.contains(Query.cursorAfter("v100")))
    }

}
