package com.lingubible.app.ui.viewmodels

import android.content.Context
import android.content.ActivityNotFoundException
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lingubible.app.core.util.IcsExporter
import com.lingubible.app.data.repository.TimetableRepository
import com.lingubible.app.domain.model.*
import com.lingubible.app.domain.repository.CourseRepository
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException

data class TimetableUiState(
    val selectedTerm: TimetableTerm = DEFAULT_TERMS.first(),
    val availableTerms: List<TimetableTerm> = DEFAULT_TERMS,
    val allSections: List<TimetableSection> = emptyList(),
    val selectedSectionIds: Set<String> = emptySet(),
    val selectedSections: List<TimetableSection> = emptyList(),
    val creditsByCode: Map<String, Int> = emptyMap(),
    val conflicts: List<TimeConflict> = emptyList(),
    val searchQuery: String = "",
    val selectedDepartment: String = "ALL",
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val importLoading: Boolean = false,
    val importPreview: TimetableImportPreview? = null,
    val importError: String? = null,
    val errorMessage: String? = null,
    val isLoading: Boolean = false,
    val appearance: TimetableAppearance = TimetableAppearance()
) {
    val distinctCourseCodes: List<String>
        get() = selectedSections
            .map { it.courseCode.trim().uppercase(Locale.ROOT) }
            .filter { it.isNotBlank() }
            .distinct()

    val totalCredits: Int
        // shortcut: uncatalogued courses keep the three-credit estimate until credit metadata is available.
        get() = distinctCourseCodes.sumOf { creditsByCode[it] ?: 3 }

    val distinctCourseCount: Int
        get() = distinctCourseCodes.size

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
    private val repository: TimetableRepository,
    private val courseRepository: CourseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimetableUiState())
    val uiState: StateFlow<TimetableUiState> = _uiState.asStateFlow()

    private var termJob: Job? = null
    private var termGeneration = 0
    private var importJob: Job? = null
    private var importGeneration = 0
    private val undoHistory = mutableMapOf<String, MutableList<Set<String>>>()
    private val redoHistory = mutableMapOf<String, MutableList<Set<String>>>()

    init {
        loadTermData(_uiState.value.selectedTerm)
        viewModelScope.launch {
            courseRepository.getCourses().onSuccess { courses ->
                _uiState.update { state ->
                    state.copy(creditsByCode = courses.associate { it.code.trim().uppercase(Locale.ROOT) to it.credits })
                }
            }
        }
    }

    fun selectTerm(term: TimetableTerm) {
        if (_uiState.value.selectedTerm.id == term.id) return
        _uiState.update { it.copy(selectedTerm = term, allSections = emptyList(), selectedSectionIds = emptySet(), selectedSections = emptyList(), conflicts = emptyList(), canUndo = false, canRedo = false) }
        loadTermData(term)
    }

    fun retry() = loadTermData(_uiState.value.selectedTerm)

    private fun loadTermData(term: TimetableTerm) {
        termJob?.cancel()
        val run = ++termGeneration
        termJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val sections = repository.getSectionsForTerm(term)
                val savedIds = repository.getSelectedSectionIds(term.id).toSet()
                val selected = sections.filter { it.id in savedIds }
                if (run == termGeneration) _uiState.update { it.copy(allSections = sections,
                    selectedSectionIds = savedIds, selectedSections = selected, conflicts = repository.findConflicts(selected),
                    appearance = repository.getAppearance(term.id),
                    canUndo = undoHistory[term.id].orEmpty().isNotEmpty(), canRedo = redoHistory[term.id].orEmpty().isNotEmpty(), isLoading = false) }
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) {
                if (run == termGeneration) _uiState.update { it.copy(isLoading = false, errorMessage = e.localizedMessage ?: "課表載入失敗 · Could not load timetable") }
            }
        }
    }

    private fun push(history: MutableMap<String, MutableList<Set<String>>>, term: String, ids: Set<String>) {
        val entries = history.getOrPut(term) { mutableListOf() }
        entries.add(ids.toSet())
        if (entries.size > 10) entries.removeAt(0)
    }

    private fun applySelection(ids: Set<String>, recordHistory: Boolean = true) {
        val state = _uiState.value
        if (state.isLoading || ids == state.selectedSectionIds) return
        repository.saveSelectedSectionIds(state.selectedTerm.id, ids.toSet())
        if (recordHistory) {
            push(undoHistory, state.selectedTerm.id, state.selectedSectionIds)
            redoHistory.remove(state.selectedTerm.id)
        }
        val selected = state.allSections.filter { it.id in ids }
        _uiState.update { it.copy(selectedSectionIds = ids.toSet(), selectedSections = selected, conflicts = repository.findConflicts(selected),
            canUndo = undoHistory[state.selectedTerm.id].orEmpty().isNotEmpty(), canRedo = redoHistory[state.selectedTerm.id].orEmpty().isNotEmpty()) }
    }

    fun toggleSection(section: TimetableSection) {
        val state = _uiState.value
        if (state.allSections.none { it.id == section.id }) return
        applySelection(if (section.id in state.selectedSectionIds) state.selectedSectionIds - section.id else state.selectedSectionIds + section.id)
    }
    fun removeSection(sectionId: String) = applySelection(_uiState.value.selectedSectionIds - sectionId)
    fun clearAll() = applySelection(emptySet())
    fun undo() {
        val state = _uiState.value
        if (state.isLoading) return
        val previous = undoHistory[state.selectedTerm.id]?.removeLastOrNull() ?: return
        push(redoHistory, state.selectedTerm.id, state.selectedSectionIds)
        applySelection(previous, recordHistory = false)
    }
    fun redo() {
        val state = _uiState.value
        if (state.isLoading) return
        val next = redoHistory[state.selectedTerm.id]?.removeLastOrNull() ?: return
        push(undoHistory, state.selectedTerm.id, state.selectedSectionIds)
        applySelection(next, recordHistory = false)
    }

    fun cancelImport() {
        importJob?.cancel(); ++importGeneration
        _uiState.update { it.copy(importLoading = false, importPreview = null, importError = null) }
    }
    fun prepareImport(link: String) {
        cancelImport()
        val share = parseTimetableShare(link).getOrElse { error ->
            _uiState.update { it.copy(importError = error.localizedMessage) }; return
        }
        val run = ++importGeneration
        _uiState.update { it.copy(importLoading = true) }
        importJob = viewModelScope.launch {
            try {
                val sections = repository.getSectionsForTerm(share.term)
                val found = sections.filter { it.id in share.sectionIds }.map { it.id }.toSet()
                check(found.isNotEmpty()) { "找不到連結中的課程 · No matching sections found" }
                val existing = repository.getSelectedSectionIds(share.term.id)
                val preview = TimetableImportPreview(share.term, sections, found, (found - existing).size,
                    (found intersect existing).size, share.sectionIds - found,
                    repository.findConflicts(sections.filter { it.id in existing || it.id in found }).size)
                if (run == importGeneration) _uiState.update { it.copy(importLoading = false, importPreview = preview) }
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) { if (run == importGeneration) _uiState.update { it.copy(importLoading = false, importError = e.localizedMessage ?: "匯入失敗 · Import failed") } }
        }
    }
    fun confirmImport() {
        val preview = _uiState.value.importPreview ?: return
        termJob?.cancel(); ++termGeneration
        val existing = repository.getSelectedSectionIds(preview.term.id).toSet()
        val selected = preview.sections.filter { it.id in existing }
        _uiState.update { it.copy(selectedTerm = preview.term, allSections = preview.sections, selectedSectionIds = existing,
            selectedSections = selected, conflicts = repository.findConflicts(selected), isLoading = false, appearance = repository.getAppearance(preview.term.id),
            canUndo = undoHistory[preview.term.id].orEmpty().isNotEmpty(), canRedo = redoHistory[preview.term.id].orEmpty().isNotEmpty()) }
        applySelection(existing + preview.incomingIds)
        cancelImport()
    }

    fun setSearchQuery(q: String) {
        _uiState.update { it.copy(searchQuery = q) }
    }

    fun setSelectedDepartment(dept: String) {
        _uiState.update { it.copy(selectedDepartment = dept) }
    }

    fun updateAppearance(appearance: TimetableAppearance): Boolean {
        val state = _uiState.value
        if (state.isLoading || timetableAppearanceError(appearance, state.selectedTerm) != null) return false
        repository.saveAppearance(state.selectedTerm.id, appearance)
        _uiState.update { it.copy(appearance = appearance) }
        return true
    }

    fun exportTimetableIcs(context: Context) {
        val state = _uiState.value
        if (state.selectedSections.isEmpty()) return

        val icsText = IcsExporter.buildTimetableIcs(
            sections = state.selectedSections,
            startYmd = state.appearance.icsStart.ifBlank { state.selectedTerm.startDate },
            endYmd = state.appearance.icsEnd.ifBlank { state.selectedTerm.endDate },
            calendarName = state.appearance.title.ifBlank { "LingUBible ${state.selectedTerm.name}" }
        )
        if (!icsText.contains("BEGIN:VEVENT")) {
            Toast.makeText(context, "所選日期內沒有課堂 No meetings in the selected dates", Toast.LENGTH_LONG).show()
            return
        }
        val fileName = "LingUBible_${state.selectedTerm.short}_Timetable.ics"
        IcsExporter.shareIcs(context, icsText, fileName)
    }

    fun exportMeetingIcs(context: Context, section: TimetableSection, meeting: TimetableMeeting) {
        runCatching {
            val state = _uiState.value
            val term = state.selectedTerm.copy(startDate = state.appearance.icsStart.ifBlank { state.selectedTerm.startDate }, endDate = state.appearance.icsEnd.ifBlank { state.selectedTerm.endDate })
            val text = IcsExporter.buildTimetableMeetingIcs(section, meeting, term)
            val code = section.courseCode.replace(Regex("[^A-Za-z0-9_-]"), "_")
            IcsExporter.shareIcs(context, text, "${code}_${term.short}_${meeting.day}_${meeting.startMinutes}.ics").getOrThrow()
        }.onFailure {
            Toast.makeText(context, "無法匯出時段 / Could not export meeting", Toast.LENGTH_LONG).show()
        }
    }

    fun addMeetingToCalendar(context: Context, section: TimetableSection, meeting: TimetableMeeting) {
        try {
            val state = _uiState.value
            val term = state.selectedTerm.copy(startDate = state.appearance.icsStart.ifBlank { state.selectedTerm.startDate }, endDate = state.appearance.icsEnd.ifBlank { state.selectedTerm.endDate })
            context.startActivity(IcsExporter.createTimetableMeetingInsertIntent(section, meeting, term))
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, "未安裝日曆程式，改為匯出 .ics / No calendar app; exporting .ics", Toast.LENGTH_LONG).show()
            exportMeetingIcs(context, section, meeting)
        } catch (_: Exception) {
            Toast.makeText(context, "無法開啟日曆 / Could not open calendar", Toast.LENGTH_LONG).show()
        }
    }
}
