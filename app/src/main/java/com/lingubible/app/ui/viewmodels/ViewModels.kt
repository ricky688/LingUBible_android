package com.lingubible.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lingubible.app.domain.model.*
import com.lingubible.app.domain.repository.*
import com.lingubible.app.domain.util.EmailValidator
import com.lingubible.app.domain.util.WordCountValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ==========================================
// Auth ViewModel
// ==========================================
data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val currentUser: User? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _uiState.update { it.copy(currentUser = user) }
            }
        }
        checkCurrentSession()
    }

    fun checkCurrentSession() {
        viewModelScope.launch {
            authRepository.checkSession()
        }
    }

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        if (!EmailValidator.isValidLingnanEmail(email)) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid Lingnan University email (@ln.hk or @ln.edu.hk)") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.login(email, password)
            if (result.isSuccess) {
                _uiState.update { it.copy(isLoading = false, errorMessage = null) }
                onSuccess()
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Login failed. Please verify credentials."
                    )
                }
            }
        }
    }

    fun register(email: String, password: String, name: String, onSuccess: () -> Unit) {
        if (!EmailValidator.isValidLingnanEmail(email)) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid Lingnan University email (@ln.hk or @ln.edu.hk)") }
            return
        }

        if (password.length < 8) {
            _uiState.update { it.copy(errorMessage = "Password must be at least 8 characters long") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.register(email, password, name)
            if (result.isSuccess) {
                // Auto login after registration
                authRepository.login(email, password)
                _uiState.update { it.copy(isLoading = false, errorMessage = null) }
                onSuccess()
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Registration failed."
                    )
                }
            }
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onSuccess()
        }
    }
}

// ==========================================
// Courses ViewModel
// ==========================================
data class CoursesUiState(
    val isLoading: Boolean = false,
    val courses: List<Course> = emptyList(),
    val searchQuery: String = "",
    val selectedSubject: String? = null,
    val errorMessage: String? = null
)

class CoursesViewModel(
    private val courseRepository: CourseRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(CoursesUiState())
    val uiState: StateFlow<CoursesUiState> = _uiState.asStateFlow()

    init {
        loadCourses()
    }

    fun search(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        loadCourses()
    }

    fun filterSubject(subject: String?) {
        _uiState.update { it.copy(selectedSubject = if (it.selectedSubject == subject) null else subject) }
        loadCourses()
    }

    fun loadCourses() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = courseRepository.getCourses(
                search = _uiState.value.searchQuery.ifBlank { null },
                subject = _uiState.value.selectedSubject,
                page = 1
            )
            if (result.isSuccess) {
                _uiState.update { it.copy(isLoading = false, courses = result.getOrDefault(emptyList())) }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Failed to load courses"
                    )
                }
            }
        }
    }
}

// ==========================================
// Course Detail ViewModel
// ==========================================
data class CourseDetailUiState(
    val isLoading: Boolean = false,
    val course: Course? = null,
    val teachingRecords: List<TeachingRecord> = emptyList(),
    val reviews: List<Review> = emptyList(),
    val gradeDistribution: Map<String, Int> = emptyMap(),
    val pastPapers: List<PastPaper> = emptyList(),
    val errorMessage: String? = null
)

class CourseDetailViewModel(
    private val courseRepository: CourseRepository,
    private val reviewRepository: ReviewRepository,
    private val materialRepository: MaterialRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(CourseDetailUiState())
    val uiState: StateFlow<CourseDetailUiState> = _uiState.asStateFlow()

    fun loadCourseDetail(courseCode: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val courseRes = courseRepository.getCourseByCode(courseCode)
            val recordsRes = courseRepository.getTeachingRecords(courseCode)
            val reviewsRes = reviewRepository.getReviewsForCourse(courseCode)
            val gradesRes = courseRepository.getGradeDistribution(courseCode)
            val papersRes = materialRepository.getPastPapers(courseCode)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    course = courseRes.getOrNull(),
                    teachingRecords = recordsRes.getOrDefault(emptyList()),
                    reviews = reviewsRes.getOrDefault(emptyList()),
                    gradeDistribution = gradesRes.getOrDefault(emptyMap()),
                    pastPapers = papersRes.getOrDefault(emptyList()),
                    errorMessage = courseRes.exceptionOrNull()?.localizedMessage
                )
            }
        }
    }

    fun voteReview(reviewId: String, voteType: String) {
        viewModelScope.launch {
            val res = reviewRepository.voteReview(reviewId, voteType)
            if (res.isSuccess) {
                val voteState = res.getOrThrow()
                _uiState.update { current ->
                    val updated = current.reviews.map { review ->
                        if (review.id == reviewId) {
                            review.copy(
                                upvotes = voteState.upvotes,
                                downvotes = voteState.downvotes,
                                userVote = voteState.userVote
                            )
                        } else review
                    }
                    current.copy(reviews = updated)
                }
            }
        }
    }
}

// ==========================================
// Instructors ViewModel
// ==========================================
data class InstructorsUiState(
    val isLoading: Boolean = false,
    val instructors: List<Instructor> = emptyList(),
    val searchQuery: String = "",
    val selectedDepartment: String? = null,
    val errorMessage: String? = null
)

class InstructorsViewModel(
    private val instructorRepository: InstructorRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(InstructorsUiState())
    val uiState: StateFlow<InstructorsUiState> = _uiState.asStateFlow()

    init {
        loadInstructors()
    }

    fun search(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        loadInstructors()
    }

    fun filterDepartment(department: String?) {
        _uiState.update { it.copy(selectedDepartment = if (it.selectedDepartment == department) null else department) }
        loadInstructors()
    }

    fun loadInstructors() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = instructorRepository.getInstructors(
                search = _uiState.value.searchQuery.ifBlank { null },
                department = _uiState.value.selectedDepartment,
                page = 1
            )
            if (result.isSuccess) {
                _uiState.update { it.copy(isLoading = false, instructors = result.getOrDefault(emptyList())) }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Failed to load instructors"
                    )
                }
            }
        }
    }
}

// ==========================================
// Reviews ViewModel
// ==========================================
data class ReviewsUiState(
    val isLoading: Boolean = false,
    val reviews: List<Review> = emptyList(),
    val errorMessage: String? = null
)

class ReviewsViewModel(
    private val reviewRepository: ReviewRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ReviewsUiState())
    val uiState: StateFlow<ReviewsUiState> = _uiState.asStateFlow()

    init {
        loadReviews()
    }

    fun loadReviews() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = reviewRepository.getLatestReviews(30)
            if (result.isSuccess) {
                _uiState.update { it.copy(isLoading = false, reviews = result.getOrDefault(emptyList())) }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Failed to load reviews"
                    )
                }
            }
        }
    }

    fun voteReview(reviewId: String, voteType: String) {
        viewModelScope.launch {
            val res = reviewRepository.voteReview(reviewId, voteType)
            if (res.isSuccess) {
                val voteState = res.getOrThrow()
                _uiState.update { current ->
                    val updated = current.reviews.map { review ->
                        if (review.id == reviewId) {
                            review.copy(
                                upvotes = voteState.upvotes,
                                downvotes = voteState.downvotes,
                                userVote = voteState.userVote
                            )
                        } else review
                    }
                    current.copy(reviews = updated)
                }
            }
        }
    }
}

// ==========================================
// Write Review ViewModel
// ==========================================
data class WriteReviewUiState(
    val courseCode: String = "",
    val courseTitle: String = "",
    val instructorName: String = "",
    val term: String = "Term 1",
    val academicYear: String = "2024-2025",
    val grade: String = "A",
    val workloadRating: Double = 3.0,
    val difficultyRating: Double = 3.0,
    val gradingRating: Double = 3.0,
    val teachingRating: Double = 3.0,
    val comment: String = "",
    val isAnonymous: Boolean = true,
    val isLoading: Boolean = false,
    val wordCount: Int = 0,
    val isWordCountValid: Boolean = false,
    val errorMessage: String? = null
)

class WriteReviewViewModel(
    private val reviewRepository: ReviewRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(WriteReviewUiState())
    val uiState: StateFlow<WriteReviewUiState> = _uiState.asStateFlow()

    fun setInitialCourse(code: String) {
        _uiState.update { it.copy(courseCode = code) }
    }

    fun updateCourseInfo(code: String, title: String, instructor: String) {
        _uiState.update {
            it.copy(
                courseCode = code,
                courseTitle = title,
                instructorName = instructor
            )
        }
    }

    fun updateTerm(term: String, year: String, grade: String) {
        _uiState.update {
            it.copy(
                term = term,
                academicYear = year,
                grade = grade
            )
        }
    }

    fun updateRatings(teaching: Double, grading: Double, workload: Double, difficulty: Double) {
        _uiState.update {
            it.copy(
                teachingRating = teaching,
                gradingRating = grading,
                workloadRating = workload,
                difficultyRating = difficulty
            )
        }
    }

    fun updateComment(text: String) {
        val count = WordCountValidator.countWords(text)
        val valid = WordCountValidator.isValid(text, 5, 1000)
        _uiState.update {
            it.copy(
                comment = text,
                wordCount = count,
                isWordCountValid = valid
            )
        }
    }

    fun setAnonymous(isAnon: Boolean) {
        _uiState.update { it.copy(isAnonymous = isAnon) }
    }

    fun submitReview(onSuccess: () -> Unit) {
        val s = _uiState.value
        if (s.courseCode.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please specify a Course Code") }
            return
        }

        if (!s.isWordCountValid) {
            _uiState.update { it.copy(errorMessage = "Comment must contain between 5 and 1000 words (Current: ${s.wordCount})") }
            return
        }

        val submission = ReviewSubmission(
            courseCode = s.courseCode,
            courseTitle = s.courseTitle.ifBlank { s.courseCode },
            instructorName = s.instructorName.ifBlank { "TBD" },
            term = s.term,
            academicYear = s.academicYear,
            grade = s.grade,
            workloadRating = s.workloadRating,
            difficultyRating = s.difficultyRating,
            gradingRating = s.gradingRating,
            teachingRating = s.teachingRating,
            comment = s.comment,
            isAnonymous = s.isAnonymous
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val res = reviewRepository.submitReview(submission)
            if (res.isSuccess) {
                _uiState.update { it.copy(isLoading = false) }
                onSuccess()
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = res.exceptionOrNull()?.localizedMessage ?: "Failed to submit review"
                    )
                }
            }
        }
    }
}

// ==========================================
// Home / Stats ViewModel
// ==========================================
data class HomeUiState(
    val isLoading: Boolean = false,
    val stats: PlatformStats = PlatformStats(),
    val errorMessage: String? = null
)

class HomeViewModel(
    private val statsRepository: StatsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadStats()
    }

    fun loadStats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = statsRepository.getPlatformStats()
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        stats = result.getOrDefault(PlatformStats()),
                        errorMessage = null
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message
                    )
                }
            }
        }
    }
}
