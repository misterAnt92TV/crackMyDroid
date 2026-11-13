package com.crackmydroid.shared.presentation.operations

import com.crackmydroid.shared.domain.model.operation.OperationEntry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class OperationLogRepositoryTest {
    @Test
    fun persistsTheLatestEntriesWhenRecordsArriveBackToBack() = runTest {
        val persistence = RecordingPersistence()
        val repository = DefaultOperationLogRepository(
            persistence = persistence,
            scope = CoroutineScope(StandardTestDispatcher(testScheduler)),
        )

        repository.record("first", success = true)
        repository.record("second", success = true)
        advanceUntilIdle()

        assertEquals(listOf("second", "first"), persistence.savedEntries.map { it.description })
    }

    @Test
    fun removesAnEntryAndPersistsTheRemainingHistory() = runTest {
        val persistence = RecordingPersistence()
        val repository = DefaultOperationLogRepository(
            persistence = persistence,
            scope = CoroutineScope(StandardTestDispatcher(testScheduler)),
        )

        repository.record("first", success = true)
        repository.record("second", success = false)
        advanceUntilIdle()

        repository.remove(repository.entries.value.first())
        advanceUntilIdle()

        assertEquals(listOf("first"), repository.entries.value.map { it.description })
        assertEquals(listOf("first"), persistence.savedEntries.map { it.description })
    }

    @Test
    fun removesOnlyTheSelectedOccurrenceWhenEntriesAreIdentical() = runTest {
        val first = OperationEntry("same", success = true, details = null, timestamp = 42L, operationId = "first")
        val second = first.copy(operationId = "second")
        val persistence = RecordingPersistence(initialEntries = listOf(first, second))
        val repository = DefaultOperationLogRepository(
            persistence = persistence,
            scope = CoroutineScope(StandardTestDispatcher(testScheduler)),
        )
        advanceUntilIdle()

        repository.remove(second)
        advanceUntilIdle()

        assertEquals(listOf(first), repository.entries.value)
        assertEquals(listOf(first), persistence.savedEntries)
    }

    @Test
    fun hidesOnlyTheSelectedEntryFromRecentHistory() = runTest {
        val first = OperationEntry("same", success = true, timestamp = 42L, operationId = "first")
        val second = first.copy(operationId = "second")
        val persistence = RecordingPersistence(initialEntries = listOf(first, second))
        val repository = DefaultOperationLogRepository(
            persistence = persistence,
            scope = CoroutineScope(StandardTestDispatcher(testScheduler)),
        )
        advanceUntilIdle()

        repository.hideFromRecent(second)
        advanceUntilIdle()

        assertEquals(listOf(first, second.copy(hiddenFromRecent = true)), repository.entries.value)
        assertEquals(repository.entries.value, persistence.savedEntries)
    }

    @Test
    fun noOpRepositoryDoesNotRemoveEntries() {
        NoOpOperationLogRepository.hideFromRecent(
            OperationEntry("operation", success = true, timestamp = 0L),
        )
        NoOpOperationLogRepository.remove(
            OperationEntry("operation", success = true, timestamp = 0L),
        )

        assertTrue(NoOpOperationLogRepository.entries.value.isEmpty())
    }

    private class RecordingPersistence(private val initialEntries: List<OperationEntry> = emptyList()) :
        OperationLogPersistence {
        var savedEntries = emptyList<OperationEntry>()

        override suspend fun load() = initialEntries

        override suspend fun save(entries: List<OperationEntry>) {
            savedEntries = entries
        }
    }
}
