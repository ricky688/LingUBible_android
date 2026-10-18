package com.lingubible.app

import androidx.lifecycle.ViewModelStore
import com.lingubible.app.data.repository.ReviewDraftStore
import com.lingubible.app.domain.model.*
import com.lingubible.app.domain.repository.*
import com.lingubible.app.ui.viewmodels.WriteReviewViewModel
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReviewSubmissionViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val user = MutableStateFlow<User?>(User("a", "Student", "a@ln.hk"))
    private val auth = mockk<AuthRepository>()
    private val courses = mockk<CourseRepository>()
    private val reviews = mockk<ReviewRepository>()
    private val drafts = mockk<ReviewDraftStore>()
    private val saved = mutableMapOf<String, ReviewForm>()
    private val store = ViewModelStore()
    private val record = TeachingRecord("record", "BUS1102", "Lecturer", term = "202510", section = "Lecture")
    private val form = ReviewForm("BUS1102", "202510", "A", 0.0, 3.0, -1.0,
        "This course helped me understand the subject", instructors = listOf(InstructorEvaluation("Lecturer", "Lecture", 4.0, 3.0,
            "The lecturer explained the topics clearly", setOf("has_final"))))
    @Before fun setup() {
        Dispatchers.setMain(dispatcher)
        every { auth.currentUser } returns user
        coEvery { courses.getCourses() } returns Result.success(listOf(Course("c", "BUS1102", "Business")))
        coEvery { courses.getTerms() } returns Result.success(listOf(CourseTerm("202510", "2025–26 Term 1")))
        coEvery { courses.getTeachingRecords(any()) } returns Result.success(listOf(record))
        every { drafts.load(any()) } answers { Result.success(saved[firstArg()]) }
        every { drafts.save(any(), any()) } answers { saved[firstArg()] = secondArg(); Result.success(Unit) }
        every { drafts.clear(any()) } answers { saved.remove(firstArg<String>()); Result.success(Unit) }
    }
    @After fun cleanup() { store.clear(); Dispatchers.resetMain() }
    private fun model(key: String = "form") = WriteReviewViewModel(reviews, courses, auth, drafts).also { store.put(key, it); it.start("BUS1102") }

    @Test fun `guest cannot load private draft or submit`() = runTest(dispatcher) {
        user.value = null
        val vm = model(); advanceUntilIdle()
        vm.change { form }; vm.submitReview { fail("Guest submitted") }
        assertNull(vm.uiState.value.userId); assertEquals(ReviewForm(courseCode = "BUS1102"), vm.uiState.value.form)
        verify(exactly = 0) { drafts.load(any()) }; coVerify(exactly = 0) { reviews.submitReview(any()) }
    }
    @Test fun `draft restores after model recreation without overwriting it with route defaults`() = runTest(dispatcher) {
        val vm = model(); advanceUntilIdle(); vm.change { form }
        val restored = model("restored"); advanceUntilIdle()
        restored.start("ACT1100")
        assertTrue(restored.uiState.value.draftRestored); assertEquals(form, restored.uiState.value.form)
    }
    @Test fun `account switch never exposes or overwrites another account draft`() = runTest(dispatcher) {
        saved["b"] = ReviewForm(courseCode = "ACT1100", comment = "Account B draft")
        val vm = model(); advanceUntilIdle(); vm.change { form }
        user.value = User("b", "B", "b@ln.hk"); advanceUntilIdle()
        assertEquals("Account B draft", vm.uiState.value.form.comment)
        assertEquals(form, saved["a"])
        user.value = null; advanceUntilIdle()
        assertEquals("", vm.uiState.value.form.comment)
    }
    @Test fun `validation prevents missing assessments and invalid restored sessions from reaching repository`() = runTest(dispatcher) {
        val vm = model(); advanceUntilIdle()
        vm.change { form.copy(instructors = form.instructors.map { it.copy(requirements = emptySet()) }) }
        vm.submitReview { fail("Invalid form submitted") }; advanceUntilIdle()
        vm.change { form.copy(instructors = form.instructors.map { it.copy(session = "Unknown") }) }
        vm.submitReview { fail("Stale session submitted") }; advanceUntilIdle()
        coVerify(exactly = 0) { reviews.submitReview(any()) }; assertNotNull(vm.uiState.value.errorMessage)
    }
    @Test fun `failed submission retains full draft then retry clears it only on success and blocks duplicate taps`() = runTest(dispatcher) {
        val pending = CompletableDeferred<Result<Review>>()
        coEvery { reviews.submitReview(any()) } coAnswers { pending.await() }
        val vm = model(); advanceUntilIdle(); vm.change { form }
        var successes = 0
        vm.submitReview { successes++ }; vm.submitReview { successes++ }; runCurrent()
        coVerify(exactly = 1) { reviews.submitReview(ReviewSubmission("a", form)) }
        pending.complete(Result.failure(IllegalStateException("offline"))); advanceUntilIdle()
        assertEquals(form, saved["a"]); assertEquals(form, vm.uiState.value.form); assertNotNull(vm.uiState.value.errorMessage)
        coEvery { reviews.submitReview(any()) } returns Result.success(Review("id", "BUS1102"))
        vm.submitReview { successes++ }; advanceUntilIdle()
        assertEquals(1, successes); assertNull(saved["a"])
    }
    @Test fun `logout during submission cannot clear the new account draft or navigate it away`() = runTest(dispatcher) {
        val pending = CompletableDeferred<Result<Review>>()
        coEvery { reviews.submitReview(any()) } coAnswers { pending.await() }
        val vm = model(); advanceUntilIdle(); vm.change { form }; vm.submitReview { fail("Navigated another account") }; runCurrent()
        saved["b"] = ReviewForm(comment = "B private draft")
        user.value = User("b", "B", "b@ln.hk"); runCurrent()
        pending.complete(Result.success(Review("id", "BUS1102"))); advanceUntilIdle()
        assertEquals("B private draft", vm.uiState.value.form.comment); assertNotNull(saved["b"]); assertNull(saved["a"])
    }
    @Test fun `draft save failure is visible and discard leaves another account alone`() = runTest(dispatcher) {
        val vm = model(); advanceUntilIdle(); vm.change { form }; saved["b"] = form
        every { drafts.save("a", any()) } returns Result.failure(IllegalStateException("storage unavailable"))
        vm.change { it.copy(comment = "Changed") }
        assertNotNull(vm.uiState.value.draftError)
        vm.discard(); assertNull(saved["a"]); assertNotNull(saved["b"])
    }
    @Test fun `reselecting the same course or term never erases written answers`() = runTest(dispatcher) {
        val vm = model(); advanceUntilIdle(); vm.change { form }
        vm.selectCourse("BUS1102"); vm.selectTerm("202510")
        assertEquals(form, vm.uiState.value.form); assertEquals(form, saved["a"])
    }

}
