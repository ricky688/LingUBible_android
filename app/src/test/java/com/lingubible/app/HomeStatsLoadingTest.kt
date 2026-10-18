package com.lingubible.app

import androidx.lifecycle.ViewModelStore
import com.lingubible.app.domain.model.PlatformStats
import com.lingubible.app.domain.repository.StatsRepository
import com.lingubible.app.ui.viewmodels.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeStatsLoadingTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()
    private val responses = Channel<Result<PlatformStats>>(Channel.UNLIMITED)
    private val repository = object : StatsRepository {
        override suspend fun getPlatformStats() = responses.receive()
    }

    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun cleanup() { store.clear(); responses.close(); Dispatchers.resetMain() }
    private fun model() = HomeViewModel(repository).also { store.put("home", it) }

    @Test fun startupRemainsLoadingUntilTheStatisticsArrive() = runTest(dispatcher) {
        val vm = model()
        runCurrent()
        assertTrue(vm.uiState.value.isLoading)
        assertFalse(vm.uiState.value.hasLoadedStats)
        val stats = PlatformStats(verifiedStudentsCount = 587, coursesCount = 1083)
        responses.send(Result.success(stats)); runCurrent()
        assertFalse(vm.uiState.value.isLoading)
        assertTrue(vm.uiState.value.hasLoadedStats)
        assertEquals(stats, vm.uiState.value.stats)
    }

    @Test fun refreshKeepsLoadedStatisticsEvenWhenARealCountIsZero() = runTest(dispatcher) {
        val vm = model()
        responses.send(Result.success(PlatformStats())); runCurrent()
        vm.loadStats(); runCurrent()
        assertTrue(vm.uiState.value.isLoading)
        assertTrue(vm.uiState.value.hasLoadedStats)
        assertEquals(PlatformStats(), vm.uiState.value.stats)
        responses.send(Result.failure(IllegalStateException("Unavailable"))); runCurrent()
        assertFalse(vm.uiState.value.isLoading)
        assertTrue(vm.uiState.value.hasLoadedStats)
    }

    @Test fun failedInitialLoadEndsThePlaceholderAndAllowsRetry() = runTest(dispatcher) {
        val vm = model()
        responses.send(Result.failure(IllegalStateException("Unavailable"))); runCurrent()
        assertFalse(vm.uiState.value.isLoading)
        assertFalse(vm.uiState.value.hasLoadedStats)
        vm.loadStats(); runCurrent()
        assertTrue(vm.uiState.value.isLoading)
        responses.send(Result.success(PlatformStats(reviewsCount = 1323))); runCurrent()
        assertTrue(vm.uiState.value.hasLoadedStats)
        assertFalse(vm.uiState.value.isLoading)
    }
}
