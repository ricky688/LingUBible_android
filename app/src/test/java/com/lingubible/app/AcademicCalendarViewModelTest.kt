package com.lingubible.app

import com.lingubible.app.data.repository.AcademicCalendarRepository
import com.lingubible.app.domain.model.AcademicEvent
import com.lingubible.app.domain.model.CalendarCategory
import com.lingubible.app.ui.viewmodels.AcademicCalendarViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AcademicCalendarViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val repository: AcademicCalendarRepository = mockk(relaxed = true)

    private val sampleEvents = listOf(
        AcademicEvent(
            id = "event-1",
            start = "2026-08-05",
            end = null,
            category = CalendarCategory.EVENT,
            title = "JUPAS Main Round Offer",
            titleTc = "公布 JUPAS 大學聯招正取結果"
        ),
        AcademicEvent(
            id = "event-2",
            start = "2026-08-20",
            end = "2026-08-25",
            category = CalendarCategory.REGISTRATION,
            title = "Course Registration Period",
            titleTc = "課程註冊期"
        ),
        AcademicEvent(
            id = "event-3",
            start = "2026-09-01",
            end = null,
            category = CalendarCategory.TERM,
            title = "First Term begins",
            titleTc = "第一學期開始"
        ),
        AcademicEvent(
            id = "event-4",
            start = "2026-10-01",
            end = null,
            category = CalendarCategory.HOLIDAY,
            title = "National Day",
            titleTc = "國慶日"
        )
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { repository.getAcademicEvents() } returns sampleEvents
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialLoadingAndMonths() = runTest {
        val viewModel = AcademicCalendarViewModel(repository)
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(4, state.allEvents.size)
        assertEquals(4, state.filteredEvents.size)
        assertEquals(listOf("2026-08", "2026-09", "2026-10"), state.months)
        assertNull(state.selectedMonthKey)
        assertNull(state.selectedDate)
    }

    @Test
    fun testMonthFilter() = runTest {
        val viewModel = AcademicCalendarViewModel(repository)
        testScheduler.advanceUntilIdle()

        viewModel.setMonth("2026-08")
        val state = viewModel.uiState.value
        assertEquals("2026-08", state.selectedMonthKey)
        assertEquals(2, state.filteredEvents.size)
        assertEquals("event-1", state.filteredEvents[0].id)
        assertEquals("event-2", state.filteredEvents[1].id)
    }

    @Test
    fun testDateFilterSingleDayAndMultiDay() = runTest {
        val viewModel = AcademicCalendarViewModel(repository)
        testScheduler.advanceUntilIdle()

        // Single-day event on 2026-08-05
        viewModel.setDate("2026-08-05")
        var state = viewModel.uiState.value
        assertEquals("2026-08-05", state.selectedDate)
        assertEquals("2026-08", state.selectedMonthKey)
        assertEquals(1, state.filteredEvents.size)
        assertEquals("event-1", state.filteredEvents[0].id)

        // Date in the middle of multi-day event (2026-08-20 to 2026-08-25)
        viewModel.setDate("2026-08-22")
        state = viewModel.uiState.value
        assertEquals(1, state.filteredEvents.size)
        assertEquals("event-2", state.filteredEvents[0].id)

        // Date with no events
        viewModel.setDate("2026-08-15")
        state = viewModel.uiState.value
        assertEquals(0, state.filteredEvents.size)

        // Clear date filter
        viewModel.clearDateFilter()
        state = viewModel.uiState.value
        assertNull(state.selectedDate)
        // Reverts to month filter
        assertEquals(2, state.filteredEvents.size)
    }

    @Test
    fun testCategoryFilter() = runTest {
        val viewModel = AcademicCalendarViewModel(repository)
        testScheduler.advanceUntilIdle()

        viewModel.setCategory(CalendarCategory.HOLIDAY)
        val state = viewModel.uiState.value
        assertEquals(1, state.filteredEvents.size)
        assertEquals("event-4", state.filteredEvents[0].id)
    }

    @Test
    fun testMonthChangeClearsMismatchedDate() = runTest {
        val viewModel = AcademicCalendarViewModel(repository)
        testScheduler.advanceUntilIdle()

        viewModel.setDate("2026-08-05")
        assertEquals("2026-08-05", viewModel.uiState.value.selectedDate)

        // Switch to different month
        viewModel.setMonth("2026-09")
        assertNull(viewModel.uiState.value.selectedDate)
        assertEquals("2026-09", viewModel.uiState.value.selectedMonthKey)
        assertEquals(1, viewModel.uiState.value.filteredEvents.size)
        assertEquals("event-3", viewModel.uiState.value.filteredEvents[0].id)
    }

    @Test
    fun testHiddenCategoriesToggleAndShowAll() = runTest {
        val viewModel = AcademicCalendarViewModel(repository)
        testScheduler.advanceUntilIdle()

        viewModel.toggleCategory(CalendarCategory.HOLIDAY)
        var state = viewModel.uiState.value
        assertEquals(true, state.hasHiddenCategories)
        assertEquals(setOf(CalendarCategory.HOLIDAY), state.hiddenCategories)
        assertEquals(3, state.visibleEvents.size)

        // Toggle again to unhide
        viewModel.toggleCategory(CalendarCategory.HOLIDAY)
        state = viewModel.uiState.value
        assertEquals(false, state.hasHiddenCategories)
        assertEquals(4, state.visibleEvents.size)

        // Toggle and show all
        viewModel.toggleCategory(CalendarCategory.TERM)
        assertEquals(1, viewModel.uiState.value.hiddenCategories.size)
        viewModel.showAllCategories()
        assertEquals(0, viewModel.uiState.value.hiddenCategories.size)
        assertEquals(4, viewModel.uiState.value.visibleEvents.size)
    }

    @Test
    fun testSelectedDayEvents() = runTest {
        val viewModel = AcademicCalendarViewModel(repository)
        testScheduler.advanceUntilIdle()

        viewModel.setDate("2026-08-22")
        val dayEvents = viewModel.uiState.value.selectedDayEvents
        assertEquals(1, dayEvents.size)
        assertEquals("event-2", dayEvents[0].id)
    }

    @Test
    fun testJumpToTermStart() = runTest {
        val viewModel = AcademicCalendarViewModel(repository)
        testScheduler.advanceUntilIdle()

        viewModel.jumpToTermStart()
        assertEquals("2026-09-01", viewModel.uiState.value.selectedDate)
        assertEquals("2026-09", viewModel.uiState.value.selectedMonthKey)
    }
}
