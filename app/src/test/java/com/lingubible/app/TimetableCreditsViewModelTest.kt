package com.lingubible.app

import com.lingubible.app.data.repository.TimetableRepository
import com.lingubible.app.domain.model.TimetableAppearance
import com.lingubible.app.domain.model.Course
import com.lingubible.app.domain.model.DEFAULT_TERMS
import com.lingubible.app.domain.model.TimetableSection
import com.lingubible.app.domain.repository.CourseRepository
import com.lingubible.app.ui.viewmodels.TimetableViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TimetableCreditsViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val timetable: TimetableRepository = mockk()
    private val catalog: CourseRepository = mockk()
    private val sections = listOf(
        TimetableSection("1", "1", " abc1001 ", "Two-credit course"),
        TimetableSection("2", "2", "ABC1002", "Six-credit course"),
        TimetableSection("3", "3", "ABC1003", "Zero-credit course")
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        coEvery { timetable.getSectionsForTerm(any()) } returns sections
        every { timetable.getAppearance(any()) } returns TimetableAppearance()
        every { timetable.getSelectedSectionIds(any()) } returns setOf("1", "2", "3")
        every { timetable.findConflicts(any()) } returns emptyList()
        coEvery { catalog.getCourses(any(), any(), any()) } returns Result.success(listOf(
            Course("1", "ABC1001", "Two-credit course", credits = 2),
            Course("2", "abc1002", "Six-credit course", credits = 6),
            Course("3", "ABC1003", "Zero-credit course", credits = 0)
        ))
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun catalogCreditsMatchNormalizedCodesAndPreserveZero() = runTest {
        val viewModel = TimetableViewModel(timetable, catalog)
        testScheduler.advanceUntilIdle()
        val state = viewModel.uiState.value
        assertEquals(mapOf("ABC1001" to 2, "ABC1002" to 6, "ABC1003" to 0), state.creditsByCode)
        assertEquals(8, state.totalCredits)
        assertEquals(sections, state.selectedSections)
        assertFalse(state.isLoading)
    }

    @Test
    fun catalogFailureDoesNotBlockSavedTimetable() = runTest {
        coEvery { catalog.getCourses(any(), any(), any()) } returns Result.failure(IllegalStateException("Offline"))
        val viewModel = TimetableViewModel(timetable, catalog)
        testScheduler.advanceUntilIdle()
        val state = viewModel.uiState.value
        assertTrue(state.creditsByCode.isEmpty())
        assertEquals(setOf("1", "2", "3"), state.selectedSectionIds)
        assertEquals(sections, state.selectedSections)
        assertEquals(9, state.totalCredits)
        assertFalse(state.isLoading)
    }

    @Test
    fun termChangeRetainsCreditsWithoutReloadingCatalog() = runTest {
        val viewModel = TimetableViewModel(timetable, catalog)
        testScheduler.advanceUntilIdle()
        viewModel.selectTerm(DEFAULT_TERMS[1])
        testScheduler.advanceUntilIdle()
        assertEquals(DEFAULT_TERMS[1], viewModel.uiState.value.selectedTerm)
        assertEquals(8, viewModel.uiState.value.totalCredits)
        coVerify(exactly = 1) { catalog.getCourses(any(), any(), any()) }
    }

    @Test
    fun multiSectionCourseDoesNotDoubleCountCreditsOrCourses() = runTest {
        val multiSections = listOf(
            TimetableSection("10", "405", "CLE9008", "Course LEC", section = "1"),
            TimetableSection("11", "406", "CLE9008", "Course TUT", section = "2"),
            TimetableSection("12", "234", "CDS3004", "Another Course", section = "1")
        )
        coEvery { timetable.getSectionsForTerm(any()) } returns multiSections
        every { timetable.getSelectedSectionIds(any()) } returns setOf("10", "11", "12")
        coEvery { catalog.getCourses(any(), any(), any()) } returns Result.success(listOf(
            Course("10", "CLE9008", "Course", credits = 3),
            Course("12", "CDS3004", "Another Course", credits = 3)
        ))
        val viewModel = TimetableViewModel(timetable, catalog)
        testScheduler.advanceUntilIdle()
        val state = viewModel.uiState.value
        assertEquals(2, state.distinctCourseCount)
        assertEquals(6, state.totalCredits)
        assertEquals(3, state.selectedSections.size)
    }
}
