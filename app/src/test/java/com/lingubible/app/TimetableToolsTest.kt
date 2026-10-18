package com.lingubible.app

import androidx.lifecycle.ViewModelStore
import com.lingubible.app.core.util.*
import com.lingubible.app.data.repository.TimetableRepository
import com.lingubible.app.domain.model.*
import com.lingubible.app.domain.repository.CourseRepository
import com.lingubible.app.ui.viewmodels.TimetableViewModel
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TimetableToolsTest {
    private val dispatcher = StandardTestDispatcher()
    private val repo = mockk<TimetableRepository>()
    private val catalog = mockk<CourseRepository>()
    private val saved = mutableMapOf<String, Set<String>>()
    private val appearances = mutableMapOf<String, TimetableAppearance>()
    private val store = ViewModelStore()
    private val sections = (1..15).map { TimetableSection("$it", "$it", "BUS1102", "Course $it") }
    @Before fun setup() {
        Dispatchers.setMain(dispatcher)
        coEvery { catalog.getCourses(any(), any(), any()) } returns Result.success(emptyList())
        coEvery { repo.getSectionsForTerm(any()) } returns sections
        every { repo.getAppearance(any()) } answers { appearances[firstArg()] ?: TimetableAppearance() }
        every { repo.saveAppearance(any(), any()) } answers { appearances[firstArg()] = secondArg() }
        every { repo.getSelectedSectionIds(any()) } answers { saved[firstArg()].orEmpty() }
        every { repo.saveSelectedSectionIds(any(), any()) } answers { saved[firstArg()] = secondArg<Set<String>>().toSet() }
        every { repo.findConflicts(any()) } returns emptyList()
    }
    @After fun cleanup() { store.clear(); Dispatchers.resetMain() }
    private fun model() = TimetableViewModel(repo, catalog).also { store.put("timetable", it) }

    @Test fun appearanceIsPerTermSurvivesReloadAndNeverChangesSelections() = runTest(dispatcher) {
        val vm = model(); advanceUntilIdle()
        vm.toggleSection(sections.first())
        val original = vm.uiState.value.selectedSectionIds
        val custom = TimetableAppearance(title = "My timetable", colors = mapOf("BUS1102" to "#FFFF00"), fields = setOf("code", "credits"))
        assertTrue(vm.updateAppearance(custom)); assertEquals(original, vm.uiState.value.selectedSectionIds)
        assertFalse(vm.updateAppearance(custom.copy(days = emptySet())))
        vm.selectTerm(DEFAULT_TERMS[1]); advanceUntilIdle(); assertEquals(TimetableAppearance(), vm.uiState.value.appearance)
        vm.selectTerm(DEFAULT_TERMS.first()); advanceUntilIdle(); assertEquals(custom, vm.uiState.value.appearance); assertEquals(original, vm.uiState.value.selectedSectionIds)
        val restored = model(); advanceUntilIdle(); assertEquals(custom, restored.uiState.value.appearance); assertEquals(original, restored.uiState.value.selectedSectionIds)
    }
    @Test fun shareLinkRoundTripsAndDeduplicatesWebIds() {
        val term = DEFAULT_TERMS[3]
        val link = timetableShareLink(term, setOf("12345", "S1-9021"))
        assertEquals(TimetableShare(term, setOf("12345", "S1-9021")), parseTimetableShare(link).getOrThrow())
        assertEquals(setOf("12345"), parseTimetableShare("https://lingubible.com/planner?term=${term.id}&sections=12345.12345").getOrThrow().sectionIds)
    }
    @Test fun shareRejectsUntrustedAndMalformedLinks() {
        val valid = timetableShareLink(DEFAULT_TERMS.first(), setOf("1"))
        listOf(valid.replace("https:", "http:"), valid.replace("www.lingubible.com", "evil.example"),
            valid.replace("/planner", "/settings"), valid.replace("sections=1", "sections=../secret"),
            valid.replace("sections=1", "sections=%ZZ"), valid.replace("2026-27-t1", "unknown"),
            "$valid&term=2025-26-t2", valid.replace("sections=1", "sections="),
            valid.replace("sections=1", "sections=" + (1..61).joinToString(".")),
            valid.replace("www.lingubible.com", "user@www.lingubible.com")
        ).forEach { assertTrue(it, parseTimetableShare(it).isFailure) }
    }
    @Test fun addRemoveClearCanUndoRedoAndNewChangesDiscardRedo() = runTest(dispatcher) {
        val vm = model(); advanceUntilIdle()
        vm.toggleSection(sections[0]); vm.toggleSection(sections[1]); vm.removeSection("1")
        assertEquals(setOf("2"), vm.uiState.value.selectedSectionIds)
        vm.undo(); assertEquals(setOf("1", "2"), vm.uiState.value.selectedSectionIds)
        vm.redo(); assertEquals(setOf("2"), vm.uiState.value.selectedSectionIds)
        vm.clearAll(); assertTrue(vm.uiState.value.selectedSectionIds.isEmpty())
        vm.undo(); assertEquals(setOf("2"), vm.uiState.value.selectedSectionIds)
        vm.toggleSection(sections[2]); assertFalse(vm.uiState.value.canRedo)
        assertEquals(setOf("2", "3"), saved[DEFAULT_TERMS.first().id])
    }
    @Test fun historyIsBoundedAndSeparateForEachTerm() = runTest(dispatcher) {
        val vm = model(); advanceUntilIdle()
        sections.forEach(vm::toggleSection)
        repeat(15) { vm.undo() }
        assertEquals(5, vm.uiState.value.selectedSectionIds.size)
        assertFalse(vm.uiState.value.canUndo)
        vm.selectTerm(DEFAULT_TERMS[1]); advanceUntilIdle()
        assertFalse(vm.uiState.value.canUndo); assertFalse(vm.uiState.value.canRedo)
        vm.toggleSection(sections[0]); vm.undo()
        assertTrue(vm.uiState.value.selectedSectionIds.isEmpty())
        vm.selectTerm(DEFAULT_TERMS.first()); advanceUntilIdle()
        assertEquals(5, vm.uiState.value.selectedSectionIds.size); assertTrue(vm.uiState.value.canRedo)
    }
    @Test fun previewAndCancelNeverWriteAndConfirmMergesTargetTermAsOneUndoStep() = runTest(dispatcher) {
        val target = DEFAULT_TERMS[1]
        saved[target.id] = setOf("1")
        val vm = model(); advanceUntilIdle()
        val link = timetableShareLink(target, setOf("1", "2", "999"))
        vm.prepareImport(link); advanceUntilIdle()
        val preview = vm.uiState.value.importPreview!!
        assertEquals(1, preview.newCount); assertEquals(1, preview.existingCount); assertEquals(setOf("999"), preview.missingIds)
        assertEquals(DEFAULT_TERMS.first(), vm.uiState.value.selectedTerm)
        verify(exactly = 0) { repo.saveSelectedSectionIds(any(), any()) }
        vm.cancelImport(); assertNull(vm.uiState.value.importPreview)
        vm.prepareImport(link); advanceUntilIdle(); vm.confirmImport()
        assertEquals(target, vm.uiState.value.selectedTerm)
        assertEquals(setOf("1", "2"), saved[target.id])
        vm.undo(); assertEquals(setOf("1"), saved[target.id])
        vm.redo(); assertEquals(setOf("1", "2"), saved[target.id])
    }
    @Test fun cancelledImportAndInvalidLinksNeverReplacePreviewOrSave() = runTest(dispatcher) {
        val vm = model(); advanceUntilIdle()
        val late = CompletableDeferred<List<TimetableSection>>()
        coEvery { repo.getSectionsForTerm(DEFAULT_TERMS[1]) } coAnswers { withContext(NonCancellable) { late.await() } }
        vm.prepareImport(timetableShareLink(DEFAULT_TERMS[1], setOf("1"))); runCurrent()
        vm.cancelImport()
        vm.prepareImport(timetableShareLink(DEFAULT_TERMS.first(), setOf("2"))); runCurrent()
        late.complete(sections); advanceUntilIdle()
        assertEquals(DEFAULT_TERMS.first(), vm.uiState.value.importPreview!!.term)
        assertEquals(setOf("2"), vm.uiState.value.importPreview!!.incomingIds)
        vm.prepareImport("https://evil.example/planner?term=2026-27-t1&sections=1"); advanceUntilIdle()
        assertNotNull(vm.uiState.value.importError); assertNull(vm.uiState.value.importPreview)
        verify(exactly = 0) { repo.saveSelectedSectionIds(any(), any()) }
    }

    @Test fun slowOldTermCannotOverwriteNewTermOrItsSavedSelections() = runTest(dispatcher) {
        val late = CompletableDeferred<List<TimetableSection>>()
        coEvery { repo.getSectionsForTerm(DEFAULT_TERMS.first()) } coAnswers { withContext(NonCancellable) { late.await() } }
        saved[DEFAULT_TERMS[1].id] = setOf("2")
        val vm = model(); runCurrent()
        vm.selectTerm(DEFAULT_TERMS[1]); runCurrent()
        late.complete(listOf(sections[0])); advanceUntilIdle()
        assertEquals(DEFAULT_TERMS[1], vm.uiState.value.selectedTerm)
        assertEquals(setOf("2"), vm.uiState.value.selectedSectionIds)
        assertEquals(sections, vm.uiState.value.allSections)
    }
    @Test fun catalogueFailureDoesNotClearSavedSelectionsAndRetryRecovers() = runTest(dispatcher) {
        saved[DEFAULT_TERMS.first().id] = setOf("1")
        var attempt = 0
        coEvery { repo.getSectionsForTerm(any()) } coAnswers { if (attempt++ == 0) throw IllegalStateException("offline") else sections }
        val vm = model(); advanceUntilIdle()
        assertEquals("offline", vm.uiState.value.errorMessage)
        assertEquals(setOf("1"), saved[DEFAULT_TERMS.first().id])
        verify(exactly = 0) { repo.saveSelectedSectionIds(any(), any()) }
        vm.retry(); advanceUntilIdle()
        assertNull(vm.uiState.value.errorMessage)
        assertEquals(setOf("1"), vm.uiState.value.selectedSectionIds)
    }

    @Test fun pdfFitsBothOrientationsWithMarginsAndPreservesImageAspect() {
        listOf(TimetableExportOrientation.LANDSCAPE to (2400 to 1400), TimetableExportOrientation.PORTRAIT to (1440 to 2560)).forEach { (orientation, image) ->
            val p = timetablePdfPlacement(orientation, image.first, image.second)
            assertTrue(p.left >= 15.99f && p.top >= 15.99f)
            assertTrue(p.right <= p.width - 15.99f && p.bottom <= p.height - 15.99f)
            assertEquals(image.first.toFloat() / image.second, (p.right - p.left) / (p.bottom - p.top), .001f)
            assertEquals(orientation == TimetableExportOrientation.LANDSCAPE, p.width > p.height)
        }
    }
}
