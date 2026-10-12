package com.lingubible.app.ui.viewmodels

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lingubible.app.domain.calculator.HonoursCalculator
import com.lingubible.app.domain.model.Course
import com.lingubible.app.domain.repository.CourseRepository
import com.lingubible.app.core.util.GpaBackup
import com.lingubible.app.core.util.GpaPlannerSnapshot
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID
import java.io.ByteArrayOutputStream
import java.io.FileInputStream

@Serializable
enum class TermPart(val code: String, val titleZh: String, val titleEn: String) {
    TERM_1("term1", "第一學期", "Term 1"),
    TERM_2("term2", "第二學期", "Term 2"),
    SUMMER("summer", "暑期學期", "Summer Term");

    val maxCourses: Int get() = if (this == SUMMER) 2 else 8
}

@Serializable
data class GpaCourseEntry(
    val id: String = UUID.randomUUID().toString(),
    val code: String = "",
    val title: String = "",
    val credits: String = "3",
    val grade: String = ""
)

@Serializable
data class GpaTerm(
    val id: String = UUID.randomUUID().toString(),
    val year: Int = 1,
    val part: TermPart = TermPart.TERM_1,
    val courses: List<GpaCourseEntry> = listOf(GpaCourseEntry())
)

@Serializable
data class GpaDocument(
    val terms: List<GpaTerm> = listOf(GpaTerm(year = 1, part = TermPart.TERM_1)),
    val yearAcademic: Map<Int, String> = emptyMap()
)

data class TermComputedStats(
    val termId: String,
    val points: Double,
    val gpaCredits: Double,
    val loadCredits: Double,
    val termGpa: Double?,
    val hasDisqualifying: Boolean
)

data class YearComputedStats(
    val year: Int,
    val terms: List<GpaTerm>,
    val yearGpa: Double?,
    val loadCredits: Double,
    val award: HonoursCalculator.YearAward?
)

data class GpaHonsUiState(
    val document: GpaDocument = GpaDocument(),
    val earnedPoints: Double = 0.0,
    val earnedCredits: Double = 0.0,
    val cgpa: Double? = null,
    val currentHonoursTier: HonoursCalculator.HonoursTier? = null,
    val targetCgpaInput: String = "3.50",
    val remainingCreditsInput: String = "",
    val targetResult: HonoursCalculator.RequiredAvgResult = HonoursCalculator.RequiredAvgResult(
        required = 0.0,
        status = HonoursCalculator.RequiredAvgStatus.FEASIBLE,
        projectedCgpa = 3.50
    ),
    val yearStats: List<YearComputedStats> = emptyList(),
    val termStats: Map<String, TermComputedStats> = emptyMap(),
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val courseCatalog: Map<String, Course> = emptyMap(),
    val backupBusy: Boolean = false,
    val backupMessage: String? = null,
    val pendingRestore: GpaPlannerSnapshot? = null
)

class GpaHonsViewModel(
    private val context: Context,
    private val courseRepository: CourseRepository,
    private val fileDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private val undoStack = mutableListOf<GpaPlannerSnapshot>()
    private val redoStack = mutableListOf<GpaPlannerSnapshot>()

    private val _uiState = MutableStateFlow(GpaHonsUiState())
    val uiState: StateFlow<GpaHonsUiState> = _uiState.asStateFlow()

    init {
        loadSavedDocument()
        loadCourseCatalog()
    }

    private fun loadCourseCatalog() {
        viewModelScope.launch {
            courseRepository.getCourses(page = 1).onSuccess { courses ->
                val map = courses.associateBy { it.code.trim().uppercase() }
                _uiState.update { it.copy(courseCatalog = map) }
            }
        }
    }

    private fun loadSavedDocument() {
        val raw = prefs.getString(KEY_DOC, null)
        val initialDoc = if (!raw.isNullOrBlank()) {
            try {
                json.decodeFromString<GpaDocument>(raw)
            } catch (_: Exception) {
                GpaDocument()
            }
        } else {
            GpaDocument()
        }

        recompute(initialDoc, prefs.getString(KEY_TARGET, null) ?: "3.50", prefs.getString(KEY_REMAINING, null) ?: "")
    }

    private fun persist(doc: GpaDocument) {
        viewModelScope.launch {
            try {
                val raw = json.encodeToString(doc)
                prefs.edit().putString(KEY_DOC, raw)
                    .putString(KEY_TARGET, _uiState.value.targetCgpaInput)
                    .putString(KEY_REMAINING, _uiState.value.remainingCreditsInput).apply()
            } catch (_: Exception) {}
        }
    }

    private fun snapshot() = GpaPlannerSnapshot(
        _uiState.value.document, _uiState.value.targetCgpaInput, _uiState.value.remainingCreditsInput
    )

    private fun mutateDocument(
        newDoc: GpaDocument,
        target: String = _uiState.value.targetCgpaInput,
        remaining: String = _uiState.value.remainingCreditsInput
    ) {
        if (GpaPlannerSnapshot(newDoc, target, remaining) == snapshot()) return
        undoStack.add(snapshot())
        if (undoStack.size > 50) undoStack.removeAt(0)
        redoStack.clear()

        recompute(newDoc, target, remaining)
        persist(newDoc)
    }

    private fun recompute(doc: GpaDocument, targetCgpaInput: String, remainingCreditsInput: String) {
        val sortedTerms = doc.terms.sortedWith(compareBy({ it.year }, { it.part.ordinal }))

        val termStatsMap = mutableMapOf<String, TermComputedStats>()
        var totalPoints = 0.0
        var totalGpaCredits = 0.0

        for (term in sortedTerms) {
            var termPoints = 0.0
            var termGpaCredits = 0.0
            var termLoadCredits = 0.0
            var hasDisqualifying = false

            for (course in term.courses) {
                if (HonoursCalculator.isAwardDisqualifying(course.grade)) {
                    hasDisqualifying = true
                }
                val cr = course.credits.toDoubleOrNull() ?: 0.0
                if (cr <= 0.0) continue
                termLoadCredits += cr

                if (!HonoursCalculator.isGpaBearing(course.grade)) continue
                val gp = HonoursCalculator.getGradePoint(course.grade) ?: continue
                termPoints += gp * cr
                termGpaCredits += cr
            }

            totalPoints += termPoints
            totalGpaCredits += termGpaCredits

            termStatsMap[term.id] = TermComputedStats(
                termId = term.id,
                points = termPoints,
                gpaCredits = termGpaCredits,
                loadCredits = termLoadCredits,
                termGpa = if (termGpaCredits > 0.0) termPoints / termGpaCredits else null,
                hasDisqualifying = hasDisqualifying
            )
        }

        val cgpa = if (totalGpaCredits > 0.0) totalPoints / totalGpaCredits else null
        val currentTier = HonoursCalculator.classifyHonours(cgpa)

        // Compute Year Stats
        val distinctYears = sortedTerms.map { it.year }.distinct().sorted()
        val yearStatsList = distinctYears.map { year ->
            val yearTerms = sortedTerms.filter { it.year == year }
            var yPoints = 0.0
            var yGpaCredits = 0.0
            var yLoadCredits = 0.0
            var maxTermCredits = 0.0
            var hasDisqualifying = false

            for (t in yearTerms) {
                val s = termStatsMap[t.id] ?: continue
                yPoints += s.points
                yGpaCredits += s.gpaCredits
                yLoadCredits += s.loadCredits
                maxTermCredits = maxOf(maxTermCredits, s.loadCredits)
                if (s.hasDisqualifying) hasDisqualifying = true
            }

            val yearGpa = if (yGpaCredits > 0.0) yPoints / yGpaCredits else null
            val award = HonoursCalculator.classifyYearAward(
                yearGpa = yearGpa,
                yearCredits = yLoadCredits,
                maxTermCredits = maxTermCredits,
                hasDisqualifyingGrade = hasDisqualifying
            )

            YearComputedStats(
                year = year,
                terms = yearTerms,
                yearGpa = yearGpa,
                loadCredits = yLoadCredits,
                award = award
            )
        }

        // Target calculation
        val defaultRemaining = maxOf(0.0, 120.0 - totalGpaCredits)
        val remainingCredits = if (remainingCreditsInput.isBlank()) {
            defaultRemaining
        } else {
            remainingCreditsInput.toDoubleOrNull() ?: defaultRemaining
        }
        val targetCgpa = (targetCgpaInput.toDoubleOrNull() ?: 3.50).coerceIn(0.0, 4.0)

        val targetResult = HonoursCalculator.calculateRequiredRemainingAvg(
            earnedPoints = totalPoints,
            earnedCredits = totalGpaCredits,
            remainingCredits = remainingCredits,
            targetCgpa = targetCgpa
        )

        _uiState.update {
            it.copy(
                document = doc,
                earnedPoints = totalPoints,
                earnedCredits = totalGpaCredits,
                cgpa = cgpa,
                currentHonoursTier = currentTier,
                targetCgpaInput = targetCgpaInput,
                remainingCreditsInput = remainingCreditsInput,
                targetResult = targetResult,
                yearStats = yearStatsList,
                termStats = termStatsMap,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }
    }

    fun setTargetCgpa(value: String) {
        val parts = value.filter { it in '0'..'9' || it == '.' }.split('.')
        val cleaned = parts.first() + if (parts.size > 1) "." + parts.drop(1).joinToString("") else ""
        val bounded = if ((cleaned.toDoubleOrNull() ?: 0.0) > 4.0) "4" else cleaned
        recompute(_uiState.value.document, bounded, _uiState.value.remainingCreditsInput)
        persist(_uiState.value.document)
    }

    fun setRemainingCredits(value: String) {
        recompute(_uiState.value.document, _uiState.value.targetCgpaInput, value.filter { it in '0'..'9' })
        persist(_uiState.value.document)
    }

    fun setAcademicYear(year: Int, academic: String) {
        if (academic !in (2022..2029).map { "$it-${it + 1}" }) return
        val doc = _uiState.value.document
        if (doc.yearAcademic[year] == academic) return
        mutateDocument(doc.copy(yearAcademic = doc.yearAcademic + (year to academic)))
    }

    fun updateCourse(termId: String, courseId: String, code: String? = null, title: String? = null, credits: String? = null, grade: String? = null) {
        val currentDoc = _uiState.value.document
        val updatedTerms = currentDoc.terms.map { term ->
            if (term.id != termId) term
            else {
                term.copy(courses = term.courses.map { c ->
                    if (c.id != courseId) c
                    else c.copy(
                        code = code ?: c.code,
                        title = title ?: c.title,
                        credits = credits ?: c.credits,
                        grade = grade ?: c.grade
                    )
                })
            }
        }
        mutateDocument(currentDoc.copy(terms = updatedTerms))
    }

    fun pickCourseSuggestion(termId: String, courseId: String, course: Course) {
        val code = course.code.trim().uppercase()
        val title = if (course.titleZh.isNotBlank()) "${course.titleZh} ${course.titleEn}" else course.titleEn
        val cr = course.credits.toString()
        updateCourse(termId, courseId, code = code, title = title, credits = cr)
    }

    fun addCourse(termId: String) {
        val currentDoc = _uiState.value.document
        val updatedTerms = currentDoc.terms.map { term ->
            if (term.id != termId) term
            else {
                if (term.courses.size >= term.part.maxCourses) term
                else term.copy(courses = term.courses + GpaCourseEntry())
            }
        }
        mutateDocument(currentDoc.copy(terms = updatedTerms))
    }

    fun removeCourse(termId: String, courseId: String) {
        val currentDoc = _uiState.value.document
        val updatedTerms = currentDoc.terms.map { term ->
            if (term.id != termId) term
            else term.copy(courses = term.courses.filter { it.id != courseId })
        }
        mutateDocument(currentDoc.copy(terms = updatedTerms))
    }

    fun addTerm(year: Int) {
        val currentDoc = _uiState.value.document
        val existingParts = currentDoc.terms.filter { it.year == year }.map { it.part }.toSet()
        if (existingParts.size >= TermPart.entries.size) return
        val nextPart = TermPart.entries.firstOrNull { !existingParts.contains(it) } ?: return
        val newTerm = GpaTerm(year = year, part = nextPart, courses = listOf(GpaCourseEntry()))
        mutateDocument(currentDoc.copy(terms = currentDoc.terms + newTerm))
    }

    fun removeTerm(termId: String) {
        val currentDoc = _uiState.value.document
        val updatedTerms = currentDoc.terms.filter { it.id != termId }
        mutateDocument(currentDoc.copy(terms = updatedTerms))
    }

    fun addYear() {
        val currentDoc = _uiState.value.document
        val usedYears = currentDoc.terms.map { it.year }.toSet()
        val nextYear = (1..8).firstOrNull { it !in usedYears } ?: return
        val newTerm = GpaTerm(year = nextYear, part = TermPart.TERM_1, courses = listOf(GpaCourseEntry()))
        mutateDocument(currentDoc.copy(terms = currentDoc.terms + newTerm))
    }

    fun removeYear(year: Int) {
        val currentDoc = _uiState.value.document
        val updatedTerms = currentDoc.terms.filter { it.year != year }
        val updatedAcademic = currentDoc.yearAcademic.toMutableMap().apply { remove(year) }
        mutateDocument(currentDoc.copy(terms = updatedTerms, yearAcademic = updatedAcademic))
    }

    fun resetAll() {
        val defaultDoc = GpaDocument()
        mutateDocument(defaultDoc)
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        val prior = undoStack.removeAt(undoStack.lastIndex)
        redoStack.add(snapshot())
        recompute(prior.document, prior.targetCgpaInput, prior.remainingCreditsInput)
        persist(prior.document)
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        val next = redoStack.removeAt(redoStack.lastIndex)
        undoStack.add(snapshot())
        recompute(next.document, next.targetCgpaInput, next.remainingCreditsInput)
        persist(next.document)
    }

    fun exportCalculator(uri: Uri, asCsv: Boolean) {
        if (_uiState.value.backupBusy) return
        val currentState = _uiState.value
        val currentSnapshot = snapshot()
        runFileOperation("匯出失敗，請重新選擇儲存位置。 Export failed; choose another location.") {
            withContext(fileDispatcher) {
                val contents = if (asCsv) GpaBackup.csv(currentState) else GpaBackup.encode(currentSnapshot)
                val stream = context.contentResolver.openOutputStream(uri, "wt") ?: error("Cannot open output")
                stream.use { it.write(contents.toByteArray(Charsets.UTF_8)) }
            }
            _uiState.update { it.copy(backupMessage = if (asCsv) "CSV 已匯出 · CSV exported" else "備份已儲存 · Backup saved") }
        }
    }

    fun readCalculatorBackup(uri: Uri) {
        if (_uiState.value.backupBusy) return
        runFileOperation("無法讀取此備份。請選擇有效的 LingUBible JSON 備份（最多 1 MB）。 Invalid or unreadable backup (max 1 MB).") {
            val restored = withContext(fileDispatcher) {
                val descriptor = try {
                    context.contentResolver.openFileDescriptor(uri, "r")
                } catch (_: NullPointerException) {
                    // Some Android 13 document providers crash in their read-only media
                    // attribution path. "rw" does not truncate; we still only read bytes.
                    // This works only when the picker has also granted write access.
                    context.contentResolver.openFileDescriptor(uri, "rw")
                } ?: error("Cannot open input")
                val bytes = descriptor.use { file ->
                    FileInputStream(file.fileDescriptor).use { input ->
                        val output = ByteArrayOutputStream()
                        val buffer = ByteArray(8192)
                        while (true) {
                            val count = input.read(buffer)
                            if (count == -1) break
                            require(output.size() + count <= GpaBackup.MAX_BYTES)
                            output.write(buffer, 0, count)
                        }
                        output.toByteArray()
                    }
                }
                GpaBackup.decode(bytes.toString(Charsets.UTF_8))
            }
            _uiState.update { it.copy(pendingRestore = restored) }
        }
    }

    private fun runFileOperation(failure: String, operation: suspend () -> Unit) {
        _uiState.update { it.copy(backupBusy = true, backupMessage = null) }
        viewModelScope.launch {
            try {
                operation()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                Log.w("GpaBackup", "Calculator file operation failed: ${error.javaClass.simpleName}")
                _uiState.update { it.copy(backupMessage = failure) }
            } finally {
                _uiState.update { it.copy(backupBusy = false) }
            }
        }
    }

    fun confirmCalculatorRestore() {
        val restored = _uiState.value.pendingRestore ?: return
        mutateDocument(restored.document, restored.targetCgpaInput, restored.remainingCreditsInput)
        _uiState.update { it.copy(pendingRestore = null, backupMessage = "備份已還原，可按復原。 Backup restored; Undo is available.") }
    }

    fun dismissCalculatorRestore() { _uiState.update { it.copy(pendingRestore = null) } }
    fun clearBackupMessage() { _uiState.update { it.copy(backupMessage = null) } }

    companion object {
        private const val PREFS_NAME = "gpa_planner_v2"
        private const val KEY_DOC = "doc_json"
        private const val KEY_TARGET = "target_cgpa"
        private const val KEY_REMAINING = "remaining_credits"
    }
}
