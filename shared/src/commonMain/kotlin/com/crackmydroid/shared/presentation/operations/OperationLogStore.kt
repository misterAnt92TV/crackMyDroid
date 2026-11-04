package com.crackmydroid.shared.presentation.operations

import com.crackmydroid.shared.domain.model.operation.OperationEntry
import com.crackmydroid.shared.domain.model.operation.OperationEvent
import com.crackmydroid.shared.domain.repository.log.OperationLogRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

interface OperationLogPersistence {
    suspend fun load(): List<OperationEntry>
    suspend fun save(entries: List<OperationEntry>)
}

class InMemoryOperationLogPersistence : OperationLogPersistence {
    private var entries: List<OperationEntry> = emptyList()

    override suspend fun load(): List<OperationEntry> = entries

    override suspend fun save(entries: List<OperationEntry>) {
        this.entries = entries
    }
}

class DefaultOperationLogRepository(
    private val persistence: OperationLogPersistence,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) : OperationLogRepository {
    private val maxEntries = 50
    private val persistenceMutex = Mutex()
    private val _entries = MutableStateFlow<List<OperationEntry>>(emptyList())
    override val entries: StateFlow<List<OperationEntry>> = _entries

    init {
        scope.launch {
            runCatching { persistence.load() }
                .onSuccess { loaded ->
                    _entries.update { current ->
                        if (current.isEmpty()) loaded.take(maxEntries) else current
                    }
                }
        }
    }

    override fun record(description: String, success: Boolean, details: String?) {
        appendEntry(OperationEntry(description, success, details, currentTimeMillis()))
    }

    override fun record(event: OperationEvent, success: Boolean, details: String?) {
        appendEntry(
            OperationEntry(
                description = event.key,
                success = success,
                details = details,
                timestamp = currentTimeMillis(),
                event = event,
                legacyDescription = null,
            )
        )
    }

    private fun appendEntry(entry: OperationEntry) {
        _entries.update { list ->
            (listOf(entry) + list).take(maxEntries)
        }
        persist()
    }

    private fun persist() {
        scope.launch {
            persistenceMutex.withLock {
                runCatching { persistence.save(_entries.value) }
            }
        }
    }
}

object NoOpOperationLogRepository : OperationLogRepository {
    private val emptyEntries = MutableStateFlow<List<OperationEntry>>(emptyList())
    override val entries: StateFlow<List<OperationEntry>> = emptyEntries

    override fun record(description: String, success: Boolean, details: String?) = Unit

    override fun record(event: OperationEvent, success: Boolean, details: String?) = Unit
}
