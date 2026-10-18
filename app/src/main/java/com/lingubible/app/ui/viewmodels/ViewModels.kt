package com.lingubible.app.ui.viewmodels

import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lingubible.app.domain.model.*
import com.lingubible.app.domain.repository.*
import com.lingubible.app.domain.util.EmailValidator
import com.lingubible.app.domain.util.WordCountValidator
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.distinctUntilChanged
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
    val errorMessage: String? = null,
    val filters: CourseFilters = CourseFilters(),
    val availableTerms: List<CourseTerm> = emptyList(),
    val optionsError: String? = null,
    val isSignedIn: Boolean = false
)

class CoursesViewModel(
    private val courseRepository: CourseRepository,
    private val materialRepository: MaterialRepository? = null,
    private val authRepository: AuthRepository? = null
) : ViewModel() {
    private val _uiState = MutableStateFlow(CoursesUiState())
    val uiState: StateFlow<CoursesUiState> = _uiState.asStateFlow()
    private var loadJob: kotlinx.coroutines.Job? = null
    private val termCache = mutableMapOf<Set<String>, Set<String>>()
    private var materials: Set<String>? = null
    private var accountId: String? = null

    init {
        loadCourses()
        if (authRepository != null) viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                if (accountId != user?.id) {
                    accountId = user?.id; materials = null
                    _uiState.update { it.copy(isSignedIn = user != null, filters = it.filters.copy(hasMaterials = false)) }
                    loadCourses()
                }
            }
        }
    }

    fun search(query: String) { _uiState.update { it.copy(searchQuery = query) }; loadCourses() }
    fun filterSubject(subject: String?) {
        val selected = subject?.takeUnless { it == _uiState.value.selectedSubject }
        applyFilters(_uiState.value.filters.copy(subjects = selected?.let { setOf(it) }.orEmpty()))
    }
    fun applyFilters(filters: CourseFilters) {
        _uiState.update { it.copy(filters = filters, selectedSubject = filters.subjects.singleOrNull()) }
        loadCourses()
    }
    fun loadFilterOptions() {
        if (_uiState.value.availableTerms.isNotEmpty()) return
        viewModelScope.launch {
            courseRepository.getTerms().onSuccess { terms -> _uiState.update { it.copy(availableTerms = terms, optionsError = null) } }
                .onFailure { error -> _uiState.update { it.copy(optionsError = error.localizedMessage ?: "無法載入學期 · Could not load terms") } }
        }
    }
    fun loadCourses() {
        loadJob?.cancel()
        val request = _uiState.value
        val userId = accountId
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        loadJob = viewModelScope.launch {
            try {
                val courses = courseRepository.getCourses(request.searchQuery.ifBlank { null }, null, 1).getOrThrow()
                val termCodes = if (request.filters.terms.isEmpty()) null else termCache[request.filters.terms]
                    ?: courseRepository.getCourseCodesForTerms(request.filters.terms).getOrThrow().also { termCache[request.filters.terms] = it }
                val materialCodes = if (!request.filters.hasMaterials) null else {
                    check(userId != null) { "請登入以篩選學習資料 · Sign in to filter study materials" }
                    materials ?: requireNotNull(materialRepository).getStudyMaterialCourseCodes().getOrThrow().also { materials = it }
                }
                kotlinx.coroutines.currentCoroutineContext().ensureActive()
                if (userId != accountId) return@launch
                _uiState.update { it.copy(isLoading = false, courses = filterCourses(courses, request.filters, termCodes, materialCodes)) }
            } catch (e: kotlinx.coroutines.CancellationException) { throw e
            } catch (e: Exception) { _uiState.update { it.copy(isLoading = false, errorMessage = e.localizedMessage ?: "Could not load courses") } }
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
    val isPapersLoading: Boolean = false,
    val papersError: String? = null,
    val syllabus: CourseSyllabus? = null,
    val isSyllabusLoading: Boolean = false,
    val reviewsError: String? = null,
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

    private var papersJob: kotlinx.coroutines.Job? = null
    private var papersGeneration = 0
    init {
        authRepository?.let { auth ->
            viewModelScope.launch {
                auth.currentUser.collect { user ->
                    val changed = _uiState.value.currentUser?.id != user?.id
                    if (changed) { papersJob?.cancel(); ++papersGeneration }
                    _uiState.update { if (changed) it.copy(currentUser = user, pastPapers = emptyList(), isPapersLoading = false, papersError = null) else it.copy(currentUser = user) }
                }
            }
        }
    }

    fun loadCourseDetail(courseCode: String) {
        val paperOwner = authRepository?.currentUser?.value?.id
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
                            reviewsError = reviewsRes.exceptionOrNull()?.localizedMessage,
                            gradeDistribution = gradesRes.getOrDefault(emptyMap()),
                            pastPapers = if (authRepository?.currentUser?.value?.id == paperOwner) papersRes.getOrDefault(emptyList()) else emptyList(),
                            papersError = papersRes.exceptionOrNull()?.localizedMessage,
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

    fun loadPastPapers(courseCode: String) {
        papersJob?.cancel()
        val run = ++papersGeneration
        val owner = authRepository?.currentUser?.value?.id
        _uiState.update { it.copy(isPapersLoading = true, pastPapers = emptyList(), papersError = null) }
        papersJob = viewModelScope.launch {
            val result = try { materialRepository.getPastPapers(courseCode) }
                catch (e: kotlinx.coroutines.CancellationException) { throw e }
                catch (e: Exception) { Result.failure(e) }
            if (run == papersGeneration && authRepository?.currentUser?.value?.id == owner) _uiState.update { it.copy(isPapersLoading = false,
                pastPapers = result.getOrDefault(emptyList()), papersError = result.exceptionOrNull()?.localizedMessage) }
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
    val isLoading: Boolean = false, val instructors: List<Instructor> = emptyList(),
    val allInstructors: List<Instructor> = emptyList(), val searchQuery: String = "",
    val filters: InstructorFilters = InstructorFilters(), val terms: List<CourseTerm> = emptyList(),
    val errorMessage: String? = null, val optionsError: String? = null
)
class InstructorsViewModel(
    private val instructorRepository: InstructorRepository,
    private val courseRepository: CourseRepository? = null
) : ViewModel() {
    private val _uiState = MutableStateFlow(InstructorsUiState())
    val uiState = _uiState.asStateFlow()
    private var job: kotlinx.coroutines.Job? = null
    private var generation = 0
    private val termCache = mutableMapOf<Set<String>, Set<String>>()
    init {
        loadInstructors()
    }
    private fun loadOptions() {
        viewModelScope.launch { courseRepository?.getTerms()?.onSuccess { terms -> _uiState.update { it.copy(terms = terms, optionsError = null) } }
            ?.onFailure { e -> _uiState.update { it.copy(optionsError = e.localizedMessage) } } }
    }
    fun search(query: String) { _uiState.update { it.copy(searchQuery = query) }; applyFilters(uiState.value.filters) }
    fun applyFilters(filters: InstructorFilters) {
        _uiState.update { it.copy(filters = filters) }
        load(refresh = false)
    }
    fun loadInstructors() { termCache.clear(); loadOptions(); load(refresh = true) }
    private fun load(refresh: Boolean) {
        job?.cancel(); val run = ++generation
        job = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val all = if (refresh || uiState.value.allInstructors.isEmpty()) instructorRepository.getAllInstructors().getOrThrow() else uiState.value.allInstructors
                val filters = uiState.value.filters
                val names = if (filters.terms.isEmpty()) emptySet() else termCache[filters.terms]
                    ?: instructorRepository.getInstructorNamesForTerms(filters.terms).getOrThrow().also { termCache[filters.terms] = it }
                if (run == generation) _uiState.update { it.copy(isLoading = false, allInstructors = all,
                    instructors = filterInstructors(all, it.searchQuery, filters, names)) }
            } catch (e: kotlinx.coroutines.CancellationException) { throw e
            } catch (e: Exception) { if (run == generation) _uiState.update { it.copy(isLoading = false, errorMessage = e.localizedMessage) } }
        }
    }
}

// ==========================================
// Reviews ViewModel
// ==========================================
data class ReviewsUiState(
    val isLoading: Boolean = false,
    val reviews: List<Review> = emptyList(),
    val errorMessage: String? = null,
    val signInRequired: Boolean = false
)

class ReviewsViewModel(
    private val reviewRepository: ReviewRepository,
    private val authRepository: AuthRepository,
    private val myReviewsOnly: Boolean = false,
    private val previewLimit: Int? = null
) : ViewModel() {
    private val _uiState = MutableStateFlow(ReviewsUiState())
    val uiState: StateFlow<ReviewsUiState> = _uiState.asStateFlow()
    private var loadJob: kotlinx.coroutines.Job? = null
    private var loadGeneration = 0

    init {
        viewModelScope.launch {
            authRepository.currentUser.map { it?.id }.distinctUntilChanged().collect { loadReviews() }
        }
    }

    fun loadReviews() {
        loadJob?.cancel()
        val generation = ++loadGeneration
        val userId = authRepository.currentUser.value?.id
        if (myReviewsOnly && userId.isNullOrBlank()) {
            _uiState.value = ReviewsUiState(signInRequired = true)
            return
        }
        // Clear account-specific results before refreshing or switching accounts.
        _uiState.update {
            it.copy(isLoading = true, errorMessage = null, signInRequired = false,
                reviews = if (myReviewsOnly) emptyList() else it.reviews.map { review -> review.copy(userVote = null) })
        }
        loadJob = viewModelScope.launch {
            val result = if (myReviewsOnly) {
                reviewRepository.getReviewsForUser(requireNotNull(userId))
            } else if (previewLimit != null) {
                reviewRepository.getLatestReviews(previewLimit)
            } else {
                reviewRepository.getAllReviewsForBrowsing()
            }
            if (generation != loadGeneration ||
                (authRepository.currentUser.value?.id != userId)) return@launch
            _uiState.update {
                it.copy(isLoading = false, reviews = result.getOrElse { if (myReviewsOnly) emptyList() else _uiState.value.reviews }.let { reviews ->
                        if (myReviewsOnly) reviews.filter { review -> review.userId == userId } else reviews
                    },
                    errorMessage = result.exceptionOrNull()?.localizedMessage)
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
// ==========================================
// Home / Stats ViewModel
// ==========================================
data class HomeUiState(
    val isLoading: Boolean = false,
    val stats: PlatformStats = PlatformStats(),
    val hasLoadedStats: Boolean = false,
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
                        hasLoadedStats = true,
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
