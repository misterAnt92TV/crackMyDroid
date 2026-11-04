package com.crackmydroid.shared.presentation.operations

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class OperationLogRepositoryTest {
    @Test
    fun persistsTheLatestEntriesWhenRecordsArriveBackToBack() = runTest {
        val persistence = RecordingPersistence()
        val repository = DefaultOperationLogRepository(
            persistence = persistence,
            scope = CoroutineScope(StandardTestDispatcher(testScheduler))
        )

        repository.record("first", success = true)
        repository.record("second", success = true)
        advanceUntilIdle()

        assertEquals(listOf("second", "first"), persistence.savedEntries.map { it.description })
    }

    private class RecordingPersistence : OperationLogPersistence {
        var savedEntries = emptyList<com.crackmydroid.shared.domain.model.operation.OperationEntry>()

        override suspend fun load() = emptyList<com.crackmydroid.shared.domain.model.operation.OperationEntry>()

        override suspend fun save(entries: List<com.crackmydroid.shared.domain.model.operation.OperationEntry>) {
            savedEntries = entries
        }
    }
}
