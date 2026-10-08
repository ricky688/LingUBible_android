package com.lingubible.app.ui.viewmodels

import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lingubible.app.domain.model.*
import com.lingubible.app.domain.repository.*
import com.lingubible.app.domain.util.EmailValidator
import com.lingubible.app.domain.util.WordCountValidator
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ==========================================
// Auth ViewModel
// ==========================================
// Auth ViewModel
// ==========================================
data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val currentUser: User? = null,
    val currentAvatar: CustomAvatar? = null,
    val isAvatarLoading: Boolean = false,
    val isGoogleLinked: Boolean = false,
    val isGoogleLoading: Boolean = false,
    val isUpdatingUsername: Boolean = false,
    val isUpdatingPassword: Boolean = false
)

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val avatarRepository: AvatarRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _uiState.update { it.copy(currentUser = user) }
                if (user != null) {
                    loadAvatar(user.id)
                    checkGoogleLinkStatus()
                } else {
                    loadAvatar("guest")
                    _uiState.update { it.copy(isGoogleLinked = false) }
                }
            }
        }
        viewModelScope.launch {
            avatarRepository.currentAvatar.collect { avatar ->
                _uiState.update { it.copy(currentAvatar = avatar) }
            }
        }
        checkCurrentSession()
    }

    fun checkCurrentSession() {
        viewModelScope.launch {
            authRepository.checkSession()
        }
    }

    fun loadAvatar(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAvatarLoading = true) }
            val result = avatarRepository.getUserAvatar(userId)
            _uiState.update { it.copy(isAvatarLoading = false, currentAvatar = result.getOrNull()) }
        }
    }

    fun saveCustomAvatar(animal: String, backgroundIndex: Int, onComplete: ((Boolean) -> Unit)? = null) {
        val targetUserId = _uiState.value.currentUser?.id ?: "guest"
        viewModelScope.launch {
            _uiState.update { it.copy(isAvatarLoading = true) }
            val result = avatarRepository.saveUserAvatar(targetUserId, animal, backgroundIndex)
            _uiState.update { it.copy(isAvatarLoading = false) }
            onComplete?.invoke(result.isSuccess)
        }
    }

    fun deleteCustomAvatar(onComplete: ((Boolean) -> Unit)? = null) {
        val targetUserId = _uiState.value.currentUser?.id ?: "guest"
        viewModelScope.launch {
            _uiState.update { it.copy(isAvatarLoading = true) }
            val result = avatarRepository.deleteUserAvatar(targetUserId)
            _uiState.update { it.copy(isAvatarLoading = false) }
            onComplete?.invoke(result.isSuccess)
        }
    }

    fun updateUsername(newUsername: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdatingUsername = true) }
            val result = authRepository.updateName(newUsername)
            _uiState.update { it.copy(isUpdatingUsername = false) }
            if (result.isSuccess) {
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.localizedMessage ?: "Failed to update username")
            }
        }
    }

    fun updatePassword(oldPassword: String, newPassword: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdatingPassword = true) }
            val result = authRepository.updatePassword(newPassword = newPassword, oldPassword = oldPassword)
            _uiState.update { it.copy(isUpdatingPassword = false) }
            if (result.isSuccess) {
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.localizedMessage ?: "Failed to update password")
            }
        }
    }

    fun checkGoogleLinkStatus() {
        viewModelScope.launch {
            _uiState.update { it.copy(isGoogleLoading = true) }
            val result = authRepository.isGoogleLinked()
            _uiState.update { it.copy(isGoogleLoading = false, isGoogleLinked = result.getOrDefault(false)) }
        }
    }

    fun unlinkGoogle(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isGoogleLoading = true) }
            val result = authRepository.unlinkGoogle()
            _uiState.update { it.copy(isGoogleLoading = false) }
            if (result.isSuccess) {
                _uiState.update { it.copy(isGoogleLinked = false) }
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.localizedMessage ?: "Failed to unlink Google account")
            }
        }
    }

    fun linkGoogle(activity: ComponentActivity, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isGoogleLoading = true) }
            val result = authRepository.loginWithGoogle(activity)
            _uiState.update { it.copy(isGoogleLoading = false) }
            if (result.isSuccess) {
                _uiState.update { it.copy(isGoogleLinked = true) }
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.localizedMessage ?: "Failed to link Google account")
            }
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

    fun register(
        email: String,
        password: String,
        confirmPassword: String,
        name: String,
        onSuccess: () -> Unit
    ) {
        if (!EmailValidator.isValidLingnanEmail(email)) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid Lingnan University email (@ln.hk or @ln.edu.hk)") }
            return
        }

        if (password.length < 8) {
            _uiState.update { it.copy(errorMessage = "Password must be at least 8 characters long") }
            return
        }

        if (password != confirmPassword) {
            _uiState.update { it.copy(errorMessage = "Passwords do not match") }
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

    fun loginWithGoogle(activity: ComponentActivity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.loginWithGoogle(activity)
            if (result.isSuccess) {
                _uiState.update { it.copy(isLoading = false, errorMessage = null) }
                onSuccess()
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.localizedMessage
                            ?: "Google sign-in could not be completed."
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
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
    val syllabus: CourseSyllabus? = null,
    val isSyllabusLoading: Boolean = false,
    val errorMessage: String? = null,
    val currentUser: com.lingubible.app.domain.model.User? = null
)

class CourseDetailViewModel(
    private val courseRepository: CourseRepository,
    private val reviewRepository: ReviewRepository,
    private val materialRepository: MaterialRepository,
    private val authRepository: AuthRepository? = null,
    val clientProvider: com.lingubible.app.data.remote.AppwriteClientProvider? = null
) : ViewModel() {
    private val _uiState = MutableStateFlow(CourseDetailUiState())
    val uiState: StateFlow<CourseDetailUiState> = _uiState.asStateFlow()

    init {
        authRepository?.let { auth ->
            viewModelScope.launch {
                auth.currentUser.collect { user ->
                    _uiState.update { it.copy(currentUser = user) }
                }
            }
        }
    }

    fun loadCourseDetail(courseCode: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isSyllabusLoading = true, errorMessage = null) }

            try {
                coroutineScope {
                    val courseDeferred = async { courseRepository.getCourseByCode(courseCode) }
                    val recordsDeferred = async { courseRepository.getTeachingRecords(courseCode) }
                    val reviewsDeferred = async { reviewRepository.getReviewsForCourse(courseCode) }
                    val gradesDeferred = async { courseRepository.getGradeDistribution(courseCode) }
                    val papersDeferred = async { materialRepository.getPastPapers(courseCode) }
                    val syllabusDeferred = async { materialRepository.getSyllabus(courseCode) }

                    val courseRes = courseDeferred.await()
                    val recordsRes = recordsDeferred.await()
                    val reviewsRes = reviewsDeferred.await()
                    val gradesRes = gradesDeferred.await()
                    val papersRes = papersDeferred.await()
                    val syllabusRes = syllabusDeferred.await()

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSyllabusLoading = false,
                            course = courseRes.getOrNull(),
                            teachingRecords = recordsRes.getOrDefault(emptyList()),
                            reviews = reviewsRes.getOrDefault(emptyList()),
                            gradeDistribution = gradesRes.getOrDefault(emptyMap()),
                            pastPapers = papersRes.getOrDefault(emptyList()),
                            syllabus = syllabusRes.getOrNull(),
                            errorMessage = courseRes.exceptionOrNull()?.localizedMessage
                        )
                    }
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isSyllabusLoading = false,
                        errorMessage = e.localizedMessage ?: "Failed to load course details"
                    )
                }
            }
        }
    }

    fun loadSyllabus(courseCode: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyllabusLoading = true) }
            val syllabusRes = materialRepository.getSyllabus(courseCode)
            _uiState.update {
                it.copy(
                    isSyllabusLoading = false,
                    syllabus = syllabusRes.getOrNull()
                )
            }
        }
    }

    fun resolveAndOpenSyllabus(courseCode: String, onResolved: (CourseSyllabus) -> Unit) {
        val currentSyllabus = _uiState.value.syllabus
        if (currentSyllabus != null) {
            onResolved(currentSyllabus)
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSyllabusLoading = true) }
            val res = materialRepository.getSyllabus(courseCode)
            val syllabus = res.getOrNull()
            _uiState.update {
                it.copy(
                    isSyllabusLoading = false,
                    syllabus = syllabus
                )
            }
            if (syllabus != null) {
                onResolved(syllabus)
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
