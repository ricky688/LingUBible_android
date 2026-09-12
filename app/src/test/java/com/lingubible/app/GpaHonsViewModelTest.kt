package com.lingubible.app

import android.content.Context
import android.content.SharedPreferences
import com.lingubible.app.domain.calculator.HonoursCalculator
import com.lingubible.app.domain.model.Course
import com.lingubible.app.domain.repository.CourseRepository
import com.lingubible.app.ui.viewmodels.GpaHonsViewModel
import com.lingubible.app.ui.viewmodels.TermPart
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GpaHonsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val context: Context = mockk(relaxed = true)
    private val sharedPreferences: SharedPreferences = mockk(relaxed = true)
    private val editor: SharedPreferences.Editor = mockk(relaxed = true)
    private val courseRepository: CourseRepository = mockk(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { context.getSharedPreferences(any(), any()) } returns sharedPreferences
        every { sharedPreferences.getString(any(), any()) } returns null
        every { sharedPreferences.edit() } returns editor
        every { editor.putString(any(), any()) } returns editor
        coEvery { courseRepository.getCourses(any(), any(), any()) } returns Result.success(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialState() = runTest {
        val viewModel = GpaHonsViewModel(context, courseRepository)
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.document.terms.size)
        assertEquals(1, state.document.terms[0].year)
        assertEquals(TermPart.TERM_1, state.document.terms[0].part)
        assertEquals(1, state.document.terms[0].courses.size)
        assertNull(state.cgpa)
        assertEquals(0.0, state.earnedCredits, 0.001)
    }

    @Test
    fun testCourseGradeAndCgpaCalculation() = runTest {
        val viewModel = GpaHonsViewModel(context, courseRepository)
        testScheduler.advanceUntilIdle()

        val termId = viewModel.uiState.value.document.terms[0].id
        val courseId = viewModel.uiState.value.document.terms[0].courses[0].id

        // Set course to A (4.00), 3 credits
        viewModel.updateCourse(
            termId = termId,
            courseId = courseId,
            code = "BUS1102",
            title = "Statistics for Business",
            credits = "3",
            grade = "A"
        )
        testScheduler.advanceUntilIdle()

        val stateAfter1 = viewModel.uiState.value
        assertEquals(4.00, stateAfter1.cgpa!!, 0.001)
        assertEquals(3.0, stateAfter1.earnedCredits, 0.001)
        assertEquals(12.0, stateAfter1.earnedPoints, 0.001)
        assertEquals(HonoursCalculator.HonoursTier.FIRST, stateAfter1.currentHonoursTier)

        // Add a second course with B (3.00), 3 credits -> average 3.50 (First class honours)
        viewModel.addCourse(termId)
        testScheduler.advanceUntilIdle()
        val courseId2 = viewModel.uiState.value.document.terms[0].courses.last().id

        viewModel.updateCourse(
            termId = termId,
            courseId = courseId2,
            code = "CLE9001",
            credits = "3",
            grade = "B"
        )
        testScheduler.advanceUntilIdle()

        val stateAfter2 = viewModel.uiState.value
        assertEquals(3.50, stateAfter2.cgpa!!, 0.001)
        assertEquals(6.0, stateAfter2.earnedCredits, 0.001)
        assertEquals(21.0, stateAfter2.earnedPoints, 0.001)
        assertEquals(HonoursCalculator.HonoursTier.FIRST, stateAfter2.currentHonoursTier)
    }

    @Test
    fun testUndoAndRedo() = runTest {
        val viewModel = GpaHonsViewModel(context, courseRepository)
        testScheduler.advanceUntilIdle()

        val termId = viewModel.uiState.value.document.terms[0].id
        val courseId = viewModel.uiState.value.document.terms[0].courses[0].id

        viewModel.updateCourse(termId, courseId, grade = "A")
        testScheduler.advanceUntilIdle()
        assertEquals("A", viewModel.uiState.value.document.terms[0].courses[0].grade)
        assertTrue(viewModel.uiState.value.canUndo)

        // Undo
        viewModel.undo()
        testScheduler.advanceUntilIdle()
        assertEquals("", viewModel.uiState.value.document.terms[0].courses[0].grade)
        assertTrue(viewModel.uiState.value.canRedo)

        // Redo
        viewModel.redo()
        testScheduler.advanceUntilIdle()
        assertEquals("A", viewModel.uiState.value.document.terms[0].courses[0].grade)
    }

    @Test
    fun testAddTermAndYear() = runTest {
        val viewModel = GpaHonsViewModel(context, courseRepository)
        testScheduler.advanceUntilIdle()

        // Initially Year 1 Term 1
        viewModel.addTerm(1)
        testScheduler.advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.document.terms.size)
        assertEquals(TermPart.TERM_2, viewModel.uiState.value.document.terms[1].part)

        // Add Year 2
        viewModel.addYear()
        testScheduler.advanceUntilIdle()
        assertEquals(3, viewModel.uiState.value.document.terms.size)
        assertEquals(2, viewModel.uiState.value.document.terms[2].year)
    }
}
