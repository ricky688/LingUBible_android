package com.lingubible.app

import androidx.lifecycle.ViewModelStore
import com.lingubible.app.domain.model.*
import com.lingubible.app.domain.repository.*
import com.lingubible.app.ui.viewmodels.CourseDetailViewModel
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PastPapersAccountTest {
    private val paper = PastPaper("file", "BUS1102", "2025-2026", "Term 2", fileId = "file", fileName = "BUS1102_25262.pdf")
    @Test fun accountChangeIgnoresLatePapersAndProfileRefreshKeepsCurrentPapers() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val store = ViewModelStore()
        try {
            val user = MutableStateFlow<User?>(User("a", "A", "a@example.com"))
            val auth = mockk<AuthRepository>(); every { auth.currentUser } returns user
            val materials = mockk<MaterialRepository>()
            val late = CompletableDeferred<Result<List<PastPaper>>>()
            coEvery { materials.getPastPapers(any()) } coAnswers { withContext(NonCancellable) { late.await() } }
            val vm = CourseDetailViewModel(mockk(), mockk(), materials, auth); store.put("detail", vm)
            runCurrent(); vm.loadPastPapers("BUS1102"); runCurrent()
            user.value = User("b", "B", "b@example.com"); runCurrent()
            late.complete(Result.success(listOf(paper))); advanceUntilIdle()
            assertTrue(vm.uiState.value.pastPapers.isEmpty()); assertFalse(vm.uiState.value.isPapersLoading)
            coEvery { materials.getPastPapers(any()) } returns Result.success(listOf(paper))
            vm.loadPastPapers("BUS1102"); advanceUntilIdle()
            assertEquals(listOf(paper), vm.uiState.value.pastPapers)
            user.value = user.value!!.copy(name = "Updated B"); advanceUntilIdle()
            assertEquals(listOf(paper), vm.uiState.value.pastPapers)
            user.value = null; advanceUntilIdle()
            assertTrue(vm.uiState.value.pastPapers.isEmpty())
        } finally { store.clear(); Dispatchers.resetMain() }
    }
    @Test fun failedPaperRefreshCanRetryWithoutHanging() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val store = ViewModelStore()
        try {
            val materials = mockk<MaterialRepository>()
            var attempt = 0
            coEvery { materials.getPastPapers(any()) } coAnswers { if (attempt++ == 0) throw IllegalStateException("offline") else Result.success(listOf(paper)) }
            val vm = CourseDetailViewModel(mockk(), mockk(), materials); store.put("detail", vm)
            vm.loadPastPapers("BUS1102"); advanceUntilIdle()
            assertEquals("offline", vm.uiState.value.papersError); assertFalse(vm.uiState.value.isPapersLoading)
            vm.loadPastPapers("BUS1102"); advanceUntilIdle()
            assertNull(vm.uiState.value.papersError); assertEquals(listOf(paper), vm.uiState.value.pastPapers)
        } finally { store.clear(); Dispatchers.resetMain() }
    }
}
