package com.lingubible.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lingubible.app.data.repository.ReviewDraftStore
import com.lingubible.app.domain.model.*
import com.lingubible.app.domain.repository.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class WriteReviewUiState(
    val form: ReviewForm = ReviewForm(), val userId: String? = null,
    val courses: List<Course> = emptyList(), val terms: List<CourseTerm> = emptyList(),
    val records: List<TeachingRecord> = emptyList(), val loadingOptions: Boolean = false,
    val loadingRecords: Boolean = false, val isLoading: Boolean = false,
    val errorMessage: String? = null, val optionsError: String? = null,
    val draftRestored: Boolean = false, val draftError: String? = null
)

class WriteReviewViewModel(
    private val reviews: ReviewRepository, private val courses: CourseRepository,
    private val auth: AuthRepository, private val drafts: ReviewDraftStore
) : ViewModel() {
    private val _uiState = MutableStateFlow(WriteReviewUiState())
    val uiState = _uiState.asStateFlow()
    private var recordsJob: Job? = null
    private var optionsJob: Job? = null
    private var started = false
    private var initialCourse = ""

    fun start(courseCode: String) {
        if (started) return
        started = true; initialCourse = courseCode
        viewModelScope.launch {
            auth.currentUser.map { it?.id }.distinctUntilChanged().collect { id ->
                recordsJob?.cancel(); optionsJob?.cancel()
                val loaded = id?.let { drafts.load(it) }
                val restored = loaded?.getOrNull()
                _uiState.value = WriteReviewUiState(userId = id,
                    form = restored ?: ReviewForm(courseCode = initialCourse.trim().uppercase()),
                    draftRestored = restored != null, draftError = loaded?.exceptionOrNull()?.localizedMessage)
                if (id != null) { loadOptions(); loadRecords() }
            }
        }
    }
    fun loadOptions() {
        optionsJob?.cancel()
        val owner = uiState.value.userId ?: return
        optionsJob = viewModelScope.launch {
            _uiState.update { it.copy(loadingOptions = true, optionsError = null) }
            val catalog = async { courses.getCourses() }
            val terms = async { courses.getTerms() }
            val c = catalog.await(); val t = terms.await()
            if (uiState.value.userId == owner) _uiState.update { it.copy(loadingOptions = false,
                courses = c.getOrDefault(emptyList()), terms = t.getOrDefault(emptyList()),
                optionsError = (c.exceptionOrNull() ?: t.exceptionOrNull())?.localizedMessage) }
        }
    }
    fun change(transform: (ReviewForm) -> ReviewForm) {
        val s = uiState.value
        val owner = s.userId ?: return
        if (s.isLoading || auth.currentUser.value?.id != owner) return
        val form = transform(s.form)
        val error = drafts.save(owner, form).exceptionOrNull()?.localizedMessage
        _uiState.update { it.copy(form = form, errorMessage = null, draftError = error) }
    }
    fun selectCourse(code: String) {
        if (code == uiState.value.form.courseCode) return
        change { ReviewForm(courseCode = code, anonymous = it.anonymous, language = it.language) }
        loadRecords()
    }
    fun selectTerm(term: String) {
        if (term != uiState.value.form.term) change { it.copy(term = term, instructors = emptyList()) }
    }
    fun toggleInstructor(record: TeachingRecord) = change { form ->
        val evaluation = InstructorEvaluation(name = record.instructorName, session = record.section, serviceLearning = record.serviceLearning ?: "none")
        form.copy(instructors = if (form.instructors.any { it.key == evaluation.key })
            form.instructors.filterNot { it.key == evaluation.key } else form.instructors + evaluation)
    }
    fun updateInstructor(key: String, transform: (InstructorEvaluation) -> InstructorEvaluation) = change { form ->
        form.copy(instructors = form.instructors.map { if (it.key == key) transform(it) else it })
    }
    fun loadRecords() {
        recordsJob?.cancel()
        val owner = uiState.value.userId ?: return
        val code = uiState.value.form.courseCode
        if (code.isBlank()) return
        recordsJob = viewModelScope.launch {
            _uiState.update { it.copy(loadingRecords = true, records = emptyList(), errorMessage = null) }
            val result = courses.getTeachingRecords(code)
            if (uiState.value.userId == owner && uiState.value.form.courseCode == code)
                _uiState.update { it.copy(loadingRecords = false, records = result.getOrDefault(emptyList()),
                    errorMessage = result.exceptionOrNull()?.localizedMessage) }
        }
    }
    fun discard() {
        val s = uiState.value
        if (s.isLoading) return
        val error = s.userId?.let { drafts.clear(it).exceptionOrNull()?.localizedMessage }
        if (error != null) { _uiState.update { it.copy(draftError = error) }; return }
        _uiState.update { it.copy(form = ReviewForm(), records = emptyList(), errorMessage = null, draftRestored = false, draftError = null) }
    }
    fun validate(): Boolean {
        val error = runCatching { uiState.value.form.validate() }.exceptionOrNull()?.localizedMessage
        _uiState.update { it.copy(errorMessage = error) }
        return error == null
    }
    fun submitReview(onSuccess: () -> Unit) {
        val s = uiState.value
        val owner = s.userId ?: return
        if (s.isLoading || s.loadingRecords || !validate() || auth.currentUser.value?.id != owner) return
        val validRecords = s.form.instructors.all { evaluation -> s.records.any {
            it.term == s.form.term && it.instructorName == evaluation.name && it.section == evaluation.session &&
                (it.serviceLearning == null || it.serviceLearning == evaluation.serviceLearning)
        } }
        if (!validRecords) { _uiState.update { it.copy(errorMessage = "請重新選擇有效講師／課節 · Select valid instructors and sessions") }; return }
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = reviews.submitReview(ReviewSubmission(owner, s.form))
            if (result.isSuccess) drafts.clear(owner)
            if (auth.currentUser.value?.id != owner || uiState.value.userId != owner) return@launch
            _uiState.update { it.copy(isLoading = false, errorMessage = result.exceptionOrNull()?.localizedMessage) }
            if (result.isSuccess) onSuccess()
        }
    }
}
