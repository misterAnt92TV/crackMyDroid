package com.crackmydroid.shared.presentation

import com.crackmydroid.shared.domain.model.operation.OperationEntry
import com.crackmydroid.shared.domain.model.operation.OperationEvent
import com.crackmydroid.shared.domain.repository.log.OperationLogRepository
import com.crackmydroid.shared.ui.home.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun recentOperations_isEmptyWhenRepositoryIsEmpty() = runTest(dispatcher) {
        val repository = FakeOperationLogRepository()
        val viewModel = HomeViewModel(repository)

        advanceUntilIdle()

        assertEquals(emptyList(), viewModel.state.value.recentOperations)
        viewModel.onCleared()
    }

    @Test
    fun recentOperations_keepsOnlyFiveInRepositoryOrder() = runTest(dispatcher) {
        val entries = (1..6).map { index -> entry("operation-$index", timestamp = 100L - index) }
        val repository = FakeOperationLogRepository(entries)
        val viewModel = HomeViewModel(repository)

        advanceUntilIdle()

        assertEquals(5, viewModel.state.value.recentOperations.size)
        assertEquals(entries.take(5), viewModel.state.value.recentOperations)
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
            entry("three", 0)
        )
        val repository = FakeOperationLogRepository(entries)
        val viewModel = HomeViewModel(repository)

        advanceUntilIdle()

        assertEquals(
            listOf("duplicate", "duplicate", "one", "two", "three"),
            viewModel.state.value.recentOperations.map { it.description }
        )
        viewModel.onCleared()
    }

    private fun entry(
        description: String,
        timestamp: Long,
        success: Boolean = true,
        hidden: Boolean = false
    ) = OperationEntry(
        description = description,
        success = success,
        timestamp = timestamp,
        operationId = "$description-$timestamp",
        hiddenFromRecent = hidden
    )

    private class FakeOperationLogRepository(initialEntries: List<OperationEntry> = emptyList()) :
        OperationLogRepository {
        private val flow = MutableStateFlow(initialEntries)
        override val entries = flow

        override fun record(description: String, success: Boolean, details: String?) = Unit
        override fun record(event: OperationEvent, success: Boolean, details: String?) = Unit
        override fun hideFromRecent(entry: OperationEntry) = Unit
        override fun remove(entry: OperationEntry) = Unit
    }
}
