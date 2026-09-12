package com.lingubible.app.ui.viewmodels

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lingubible.app.domain.calculator.HonoursCalculator
import com.lingubible.app.domain.model.Course
import com.lingubible.app.domain.repository.CourseRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

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
    val courseCatalog: Map<String, Course> = emptyMap()
)

class GpaHonsViewModel(
    private val context: Context,
    private val courseRepository: CourseRepository
) : ViewModel() {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private val undoStack = mutableListOf<GpaDocument>()
    private val redoStack = mutableListOf<GpaDocument>()

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

        recompute(initialDoc, _uiState.value.targetCgpaInput, _uiState.value.remainingCreditsInput)
    }

    private fun persist(doc: GpaDocument) {
        viewModelScope.launch {
            try {
                val raw = json.encodeToString(doc)
                prefs.edit().putString(KEY_DOC, raw).apply()
            } catch (_: Exception) {}
        }
    }

    private fun mutateDocument(newDoc: GpaDocument) {
        undoStack.add(_uiState.value.document)
        if (undoStack.size > 50) undoStack.removeAt(0)
        redoStack.clear()

        recompute(newDoc, _uiState.value.targetCgpaInput, _uiState.value.remainingCreditsInput)
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
        recompute(_uiState.value.document, value, _uiState.value.remainingCreditsInput)
    }

    fun setRemainingCredits(value: String) {
        recompute(_uiState.value.document, _uiState.value.targetCgpaInput, value)
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
        val maxYear = currentDoc.terms.maxOfOrNull { it.year } ?: 0
        if (maxYear >= 8) return
        val nextYear = maxYear + 1
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
        redoStack.add(_uiState.value.document)
        recompute(prior, _uiState.value.targetCgpaInput, _uiState.value.remainingCreditsInput)
        persist(prior)
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        val next = redoStack.removeAt(redoStack.lastIndex)
        undoStack.add(_uiState.value.document)
        recompute(next, _uiState.value.targetCgpaInput, _uiState.value.remainingCreditsInput)
        persist(next)
    }

    companion object {
        private const val PREFS_NAME = "gpa_planner_v2"
        private const val KEY_DOC = "doc_json"
    }
}
