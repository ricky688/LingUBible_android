package com.lingubible.app.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lingubible.app.core.util.IcsExporter
import com.lingubible.app.data.repository.AcademicCalendarRepository
import com.lingubible.app.domain.model.AcademicEvent
import com.lingubible.app.domain.model.CalendarCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AcademicCalendarUiState(
    val allEvents: List<AcademicEvent> = emptyList(),
    val hiddenCategories: Set<CalendarCategory> = emptySet(),
    val selectedCategory: CalendarCategory? = null,
    val selectedMonthKey: String? = null, // "2026-08", "2026-09", etc.
    val selectedDate: String? = null, // "2026-08-05"
    val searchQuery: String = "",
    val isLoading: Boolean = false
) {
    val months: List<String>
        get() = allEvents.map { it.start.take(7) }.distinct().sorted()

    val hasHiddenCategories: Boolean
        get() = hiddenCategories.isNotEmpty()

    val visibleEvents: List<AcademicEvent>
        get() {
            return allEvents.filter { event ->
                !hiddenCategories.contains(event.category) ||
                    (event.category2 != null && !hiddenCategories.contains(event.category2))
            }
        }

    val selectedDayEvents: List<AcademicEvent>
        get() {
            val date = selectedDate ?: "2026-09-10"
            return visibleEvents.filter { event ->
                val end = event.end ?: event.start
                event.start <= date && end >= date
            }.sortedWith(
                compareBy<AcademicEvent> { it.category.ordinal }
                    .thenBy { it.start }
            )
        }

    val filteredEvents: List<AcademicEvent>
        get() {
            var list = visibleEvents
            if (selectedCategory != null) {
                list = list.filter { it.category == selectedCategory || it.category2 == selectedCategory }
            }
            if (selectedDate != null) {
                list = list.filter { event ->
                    val end = event.end ?: event.start
                    event.start <= selectedDate && end >= selectedDate
                }
            } else if (selectedMonthKey != null) {
                list = list.filter { it.start.startsWith(selectedMonthKey) || (it.end?.startsWith(selectedMonthKey) == true) }
            }
            if (searchQuery.isNotBlank()) {
                val q = searchQuery.trim().lowercase()
                list = list.filter {
                    it.title.lowercase().contains(q) ||
                    it.titleTc.lowercase().contains(q) ||
                    it.titleSc.lowercase().contains(q) ||
                    it.category.labelZh.contains(q)
                }
            }
            return list
        }
}

class AcademicCalendarViewModel(
    private val repository: AcademicCalendarRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AcademicCalendarUiState())
    val uiState: StateFlow<AcademicCalendarUiState> = _uiState.asStateFlow()

    init {
        loadEvents()
    }

    private fun loadEvents() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val events = repository.getAcademicEvents()
            _uiState.update {
                it.copy(
                    allEvents = events,
                    isLoading = false
                )
            }
        }
    }

    fun toggleCategory(category: CalendarCategory) {
        _uiState.update { current ->
            val next = current.hiddenCategories.toMutableSet()
            if (next.contains(category)) {
                next.remove(category)
            } else {
                next.add(category)
            }
            current.copy(hiddenCategories = next)
        }
    }

    fun showAllCategories() {
        _uiState.update { it.copy(hiddenCategories = emptySet()) }
    }

    fun setCategory(category: CalendarCategory?) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun setMonth(monthKey: String?) {
        _uiState.update { current ->
            val shouldClearDate = current.selectedDate != null && monthKey != null && !current.selectedDate.startsWith(monthKey)
            current.copy(
                selectedMonthKey = monthKey,
                selectedDate = if (shouldClearDate) null else current.selectedDate
            )
        }
    }

    fun setDate(date: String?) {
        _uiState.update { current ->
            val newMonth = date?.take(7) ?: current.selectedMonthKey
            current.copy(
                selectedDate = date,
                selectedMonthKey = newMonth
            )
        }
    }

    fun jumpToToday() {
        val todayStr = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
        setDate(todayStr)
    }

    fun jumpToTermStart() {
        val today = java.time.LocalDate.now()
        val term2Start = java.time.LocalDate.parse("2027-01-08")
        val targetDate = if (today.isBefore(term2Start)) "2026-09-01" else "2027-01-08"
        setDate(targetDate)
    }

    fun clearDateFilter() {
        _uiState.update { it.copy(selectedDate = null) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    /**
     * Add event to Android system Calendar app directly via Intent.
     */
    fun addToSystemCalendar(context: Context, event: AcademicEvent) {
        val intent = IcsExporter.createCalendarInsertIntent(event)
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to sharing .ics
            exportSingleEventIcs(context, event)
        }
    }

    /**
     * Export a single event as an .ics file and trigger Android share sheet.
     */
    fun exportSingleEventIcs(context: Context, event: AcademicEvent) {
        val icsText = IcsExporter.buildSingleEventIcs(event)
        val safeTitle = event.displayTitle.replace(Regex("[^a-zA-Z0-9\\u4e00-\\u9fa5]"), "_")
        val fileName = "${safeTitle}_${event.start}.ics"
        IcsExporter.shareIcs(context, icsText, fileName)
    }

    /**
     * Export all academic events as a single batch .ics file and trigger Android share sheet.
     */
    fun exportAllEventsToIcs(context: Context) {
        val events = _uiState.value.allEvents
        if (events.isEmpty()) return
        val icsText = IcsExporter.buildMultipleEventsIcs(events)
        val fileName = "Lingnan_Academic_Calendar.ics"
        IcsExporter.shareIcs(context, icsText, fileName)
    }
}
