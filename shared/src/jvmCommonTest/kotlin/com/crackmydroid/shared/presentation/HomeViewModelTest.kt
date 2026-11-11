package com.crackmydroid.shared.presentation

import app.cash.turbine.test
import com.crackmydroid.shared.domain.model.operation.OperationEntry
import com.crackmydroid.shared.domain.repository.log.OperationLogRepository
import com.crackmydroid.shared.ui.home.HomeEvent
import com.crackmydroid.shared.ui.home.HomeViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val operationLogs = mockk<OperationLogRepository>(relaxed = true)
    private val entriesFlow = MutableStateFlow<List<OperationEntry>>(emptyList())

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { operationLogs.entries } returns entriesFlow
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun recentOperations_isEmptyWhenRepositoryIsEmpty() = runTest(dispatcher) {
        val viewModel = HomeViewModel(operationLogs, TestAppDispatchers(dispatcher))

        viewModel.state.test {
            val item = awaitItem()
            assertEquals(emptyList(), item.recentOperations)
        }
        viewModel.onCleared()
    }

    @Test
    fun recentOperations_keepsOnlyFiveInRepositoryOrder() = runTest(dispatcher) {
        val entries = (1..6).map { index -> entry("operation-$index", timestamp = 100L - index) }
        entriesFlow.value = entries
        val viewModel = HomeViewModel(operationLogs, TestAppDispatchers(dispatcher))

        advanceUntilIdle()

        viewModel.state.test {
            val current = awaitItem()
            assertEquals(5, current.recentOperations.size)
            assertEquals(entries.take(5), current.recentOperations)
        }
        viewModel.onCleared()
    }

    @Test
    fun recentOperations_filtersHiddenAndDeduplicatesBeforeApplyingLimit() = runTest(dispatcher) {
        val entries = listOf(
            entry("duplicate", 6),
            entry("duplicate", 5, success = false),
            entry("duplicate", 4),
            entry("hidden", 3, hidden = true),
            entry("one", 2),
            entry("two", 1),
            entry("three", 0),
        )
        entriesFlow.value = entries
        val viewModel = HomeViewModel(operationLogs, TestAppDispatchers(dispatcher))

        advanceUntilIdle()

        assertEquals(
            listOf("duplicate", "duplicate", "one", "two", "three"),
            viewModel.state.value.recentOperations.map { it.description },
        )
        viewModel.onCleared()
    }

    @Test
    fun handleEvent_hideFromRecent_callsRepository() = runTest(dispatcher) {
        val target = entry("test-op", 10)
        entriesFlow.value = listOf(target)
        val viewModel = HomeViewModel(operationLogs, TestAppDispatchers(dispatcher))
        advanceUntilIdle()

        viewModel.handleEvent(HomeEvent.HideFromRecent(target.operationId))
        advanceUntilIdle()

        verify { operationLogs.hideFromRecent(target) }
        viewModel.onCleared()
    }

    @Test
    fun handleEvent_remove_callsRepository() = runTest(dispatcher) {
        val target = entry("test-op", 10)
        entriesFlow.value = listOf(target)
        val viewModel = HomeViewModel(operationLogs, TestAppDispatchers(dispatcher))
        advanceUntilIdle()

        viewModel.handleEvent(HomeEvent.Remove(target.operationId))
        advanceUntilIdle()

        verify { operationLogs.remove(target) }
        viewModel.onCleared()
    }

    private fun entry(description: String, timestamp: Long, success: Boolean = true, hidden: Boolean = false) =
        OperationEntry(
            description = description,
            success = success,
            timestamp = timestamp,
            operationId = "$description-$timestamp",
            hiddenFromRecent = hidden,
        )
}
