package com.lingubible.app.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lingubible.app.core.util.IcsExporter
import com.lingubible.app.data.repository.TimetableRepository
import com.lingubible.app.domain.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TimetableUiState(
    val selectedTerm: TimetableTerm = DEFAULT_TERMS.first(),
    val availableTerms: List<TimetableTerm> = DEFAULT_TERMS,
    val allSections: List<TimetableSection> = emptyList(),
    val selectedSectionIds: Set<String> = emptySet(),
    val selectedSections: List<TimetableSection> = emptyList(),
    val conflicts: List<TimeConflict> = emptyList(),
    val searchQuery: String = "",
    val selectedDepartment: String = "ALL",
    val isLoading: Boolean = false
) {
    val totalCredits: Int
        get() = selectedSections.size * 3 // Lingnan courses standard 3 credits

    val filteredSections: List<TimetableSection>
        get() {
            var list = allSections
            if (selectedDepartment != "ALL") {
                list = list.filter { it.department.equals(selectedDepartment, ignoreCase = true) }
            }
            if (searchQuery.isNotBlank()) {
                val q = searchQuery.trim().lowercase()
                list = list.filter {
                    it.courseCode.lowercase().contains(q) ||
                    it.courseTitle.lowercase().contains(q) ||
                    it.crn.contains(q) ||
                    it.instructors.any { inst -> inst.lowercase().contains(q) }
                }
            }
            return list
        }
}

class TimetableViewModel(
    private val repository: TimetableRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimetableUiState())
    val uiState: StateFlow<TimetableUiState> = _uiState.asStateFlow()

    init {
        loadTermData(_uiState.value.selectedTerm)
    }

    fun selectTerm(term: TimetableTerm) {
        if (_uiState.value.selectedTerm.id == term.id) return
        _uiState.update { it.copy(selectedTerm = term) }
        loadTermData(term)
    }

    private fun loadTermData(term: TimetableTerm) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val sections = repository.getSectionsForTerm(term)
            val savedIds = repository.getSelectedSectionIds(term.id)
            val selected = sections.filter { savedIds.contains(it.id) }
            val conflicts = repository.findConflicts(selected)

            _uiState.update {
                it.copy(
                    allSections = sections,
                    selectedSectionIds = savedIds,
                    selectedSections = selected,
                    conflicts = conflicts,
                    isLoading = false
                )
            }
        }
    }

    fun toggleSection(section: TimetableSection) {
        val currentIds = _uiState.value.selectedSectionIds.toMutableSet()
        if (currentIds.contains(section.id)) {
            currentIds.remove(section.id)
        } else {
            currentIds.add(section.id)
        }
        val termId = _uiState.value.selectedTerm.id
        repository.saveSelectedSectionIds(termId, currentIds)

        val selected = _uiState.value.allSections.filter { currentIds.contains(it.id) }
        val conflicts = repository.findConflicts(selected)

        _uiState.update {
            it.copy(
                selectedSectionIds = currentIds,
                selectedSections = selected,
                conflicts = conflicts
            )
        }
    }

    fun removeSection(sectionId: String) {
        val currentIds = _uiState.value.selectedSectionIds.toMutableSet()
        currentIds.remove(sectionId)
        val termId = _uiState.value.selectedTerm.id
        repository.saveSelectedSectionIds(termId, currentIds)

        val selected = _uiState.value.allSections.filter { currentIds.contains(it.id) }
        val conflicts = repository.findConflicts(selected)

        _uiState.update {
            it.copy(
                selectedSectionIds = currentIds,
                selectedSections = selected,
                conflicts = conflicts
            )
        }
    }

    fun clearAll() {
        val termId = _uiState.value.selectedTerm.id
        repository.saveSelectedSectionIds(termId, emptySet())
        _uiState.update {
            it.copy(
                selectedSectionIds = emptySet(),
                selectedSections = emptyList(),
                conflicts = emptyList()
            )
        }
    }

    fun setSearchQuery(q: String) {
        _uiState.update { it.copy(searchQuery = q) }
    }

    fun setSelectedDepartment(dept: String) {
        _uiState.update { it.copy(selectedDepartment = dept) }
    }

    fun exportTimetableIcs(context: Context) {
        val state = _uiState.value
        if (state.selectedSections.isEmpty()) return

        val icsText = IcsExporter.buildTimetableIcs(
            sections = state.selectedSections,
            startYmd = state.selectedTerm.startDate,
            endYmd = state.selectedTerm.endDate,
            calendarName = "LingUBible ${state.selectedTerm.name}"
        )
        val fileName = "LingUBible_${state.selectedTerm.short}_Timetable.ics"
        IcsExporter.shareIcs(context, icsText, fileName)
    }
}
