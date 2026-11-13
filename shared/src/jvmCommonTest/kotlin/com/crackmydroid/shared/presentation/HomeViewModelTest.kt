package com.crackmydroid.shared.presentation

import com.crackmydroid.architecture.testing.*


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
class HomeViewModelTest : BaseViewModelUnitTest() {
    private val operationLogs = mockk<OperationLogRepository>(relaxed = true)
    private val entriesFlow = MutableStateFlow<List<OperationEntry>>(emptyList())

    override fun setUp() {
        super.setUp()
        every { operationLogs.entries } returns entriesFlow
    }

    private fun createViewModel() = autoClear(HomeViewModel(operationLogs, appDispatchers))

    @Test
    fun recentOperations_isEmptyWhenRepositoryIsEmpty() = runViewModelTest {
        val viewModel = createViewModel()

        viewModel.state.test {
            val item = awaitItem()
            assertEquals(emptyList(), item.recentOperations)
        }
    }

    @Test
    fun recentOperations_keepsOnlyFiveInRepositoryOrder() = runViewModelTest {
        val entries = (1..6).map { index -> entry("operation-$index", timestamp = 100L - index) }
        entriesFlow.value = entries
        val viewModel = createViewModel()

        advanceUntilIdle()

        viewModel.state.test {
            val current = awaitItem()
            assertEquals(5, current.recentOperations.size)
            assertEquals(entries.take(5), current.recentOperations)
        }
    }

    @Test
    fun recentOperations_filtersHiddenAndDeduplicatesBeforeApplyingLimit() = runViewModelTest {
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
        val viewModel = createViewModel()

        advanceUntilIdle()

        assertEquals(
            listOf("duplicate", "duplicate", "one", "two", "three"),
            viewModel.state.value.recentOperations.map { it.description },
        )
    }

    @Test
    fun handleEvent_hideFromRecent_callsRepository() = runViewModelTest {
        val target = entry("test-op", 10)
        entriesFlow.value = listOf(target)
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.emitEventAndAwaitIdle(HomeEvent.HideFromRecent(target.operationId))

        verify { operationLogs.hideFromRecent(target) }
    }

    @Test
    fun handleEvent_remove_callsRepository() = runViewModelTest {
        val target = entry("test-op", 10)
        entriesFlow.value = listOf(target)
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.emitEventAndAwaitIdle(HomeEvent.Remove(target.operationId))

        verify { operationLogs.remove(target) }
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
