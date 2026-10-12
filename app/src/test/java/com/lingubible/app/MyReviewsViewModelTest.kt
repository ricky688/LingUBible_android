package com.lingubible.app

import androidx.lifecycle.ViewModelStore
import com.lingubible.app.domain.model.Review
import com.lingubible.app.domain.model.User
import com.lingubible.app.domain.repository.AuthRepository
import com.lingubible.app.domain.repository.ReviewRepository
import com.lingubible.app.ui.viewmodels.ReviewsViewModel
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MyReviewsViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val user = MutableStateFlow<User?>(null)
    private val auth = mockk<AuthRepository>()
    private val reviews = mockk<ReviewRepository>()
    private val store = ViewModelStore()

    @Before fun setup() {
        Dispatchers.setMain(dispatcher)
        every { auth.currentUser } returns user
    }
    @After fun cleanup() {
        store.clear()
        Dispatchers.resetMain()
    }
    private fun model(mine: Boolean = true) = ReviewsViewModel(reviews, auth, mine).also {
        store.put("reviews", it)
    }
    private fun review(owner: String, id: String = owner) = Review(id, "BUS1102", userId = owner)

    @Test fun `guest never loads the public feed from my reviews`() = runTest(dispatcher) {
        val vm = model()
        advanceUntilIdle()
        assertTrue(vm.uiState.value.signInRequired)
        assertTrue(vm.uiState.value.reviews.isEmpty())
        coVerify(exactly = 0) { reviews.getLatestReviews(any()) }
        coVerify(exactly = 0) { reviews.getReviewsForUser(any()) }
    }

    @Test fun `my reviews queries the account and excludes another owner`() = runTest(dispatcher) {
        user.value = User("a", "A", "a@ln.hk")
        coEvery { reviews.getReviewsForUser("a") } returns Result.success(listOf(review("a"), review("b")))
        val vm = model()
        advanceUntilIdle()
        assertEquals(listOf("a"), vm.uiState.value.reviews.map { it.userId })
        coVerify(exactly = 1) { reviews.getReviewsForUser("a") }
        coVerify(exactly = 0) { reviews.getLatestReviews(any()) }
    }

    @Test fun `logout clears reviews and ignores an old in flight response`() = runTest(dispatcher) {
        user.value = User("a", "A", "a@ln.hk")
        val response = CompletableDeferred<Result<List<Review>>>()
        coEvery { reviews.getReviewsForUser("a") } coAnswers { withContext(NonCancellable) { response.await() } }
        val vm = model()
        runCurrent()
        user.value = null
        runCurrent()
        assertTrue(vm.uiState.value.signInRequired)
        response.complete(Result.success(listOf(review("a"))))
        advanceUntilIdle()
        assertTrue(vm.uiState.value.reviews.isEmpty())
        assertTrue(vm.uiState.value.signInRequired)
    }

    @Test fun `account switch discards late results from the previous account`() = runTest(dispatcher) {
        user.value = User("a", "A", "a@ln.hk")
        val old = CompletableDeferred<Result<List<Review>>>()
        coEvery { reviews.getReviewsForUser("a") } coAnswers { withContext(NonCancellable) { old.await() } }
        coEvery { reviews.getReviewsForUser("b") } returns Result.success(listOf(review("b")))
        val vm = model()
        runCurrent()
        user.value = User("b", "B", "b@ln.hk")
        runCurrent()
        old.complete(Result.success(listOf(review("a"))))
        advanceUntilIdle()
        assertEquals(listOf("b"), vm.uiState.value.reviews.map { it.userId })
    }

    @Test fun `failed personal query and retry never fall back to latest`() = runTest(dispatcher) {
        user.value = User("a", "A", "a@ln.hk")
        coEvery { reviews.getReviewsForUser("a") } returnsMany listOf(
            Result.failure(IllegalStateException("offline")), Result.success(listOf(review("a"))))
        val vm = model()
        advanceUntilIdle()
        assertNotNull(vm.uiState.value.errorMessage)
        assertTrue(vm.uiState.value.reviews.isEmpty())
        vm.loadReviews()
        advanceUntilIdle()
        assertNull(vm.uiState.value.errorMessage)
        assertEquals(1, vm.uiState.value.reviews.size)
        coVerify(exactly = 0) { reviews.getLatestReviews(any()) }
    }

    @Test fun `public reviews still load latest reviews`() = runTest(dispatcher) {
        coEvery { reviews.getLatestReviews(30) } returns Result.success(listOf(review("a"), review("b")))
        val vm = model(false)
        advanceUntilIdle()
        assertEquals(2, vm.uiState.value.reviews.size)
        coVerify(exactly = 0) { reviews.getReviewsForUser(any()) }
    }
}
