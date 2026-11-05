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
import kotlin.random.Random

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
                        if (current.isEmpty()) normalizeLoadedEntries(loaded) else current
                    }
                }
        }
    }

    override fun record(description: String, success: Boolean, details: String?) {
        appendEntry(
            OperationEntry(
                description = description,
                success = success,
                details = details,
                timestamp = currentTimeMillis(),
                operationId = newOperationId(),
            )
        )
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
                operationId = newOperationId(),
            )
        )
    }

    override fun hideFromRecent(entry: OperationEntry) {
        var updated = false
        _entries.update { current ->
            current.map { candidate ->
                if (!updated && candidate.operationId == entry.operationId) {
                    updated = true
                    candidate.copy(hiddenFromRecent = true)
                } else {
                    candidate
                }
            }
        }
        persist()
    }

    override fun remove(entry: OperationEntry) {
        _entries.update { current ->
            val index = current.indexOfFirst { candidate ->
                candidate.operationId == entry.operationId
            }
            if (index < 0) current else current.filterIndexed { itemIndex, _ -> itemIndex != index }
        }
        persist()
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

    private fun normalizeLoadedEntries(loaded: List<OperationEntry>): List<OperationEntry> =
        loaded.take(maxEntries).mapIndexed { index, entry ->
            if (entry.operationId.isNotBlank()) {
                entry
            } else {
                entry.copy(operationId = "legacy-$index-${entry.hashCode()}")
            }
        }

    private fun newOperationId(): String = "operation-${Random.nextLong()}"
}

object NoOpOperationLogRepository : OperationLogRepository {
    private val emptyEntries = MutableStateFlow<List<OperationEntry>>(emptyList())
    override val entries: StateFlow<List<OperationEntry>> = emptyEntries

    override fun record(description: String, success: Boolean, details: String?) = Unit

    override fun record(event: OperationEvent, success: Boolean, details: String?) = Unit

    override fun hideFromRecent(entry: OperationEntry) = Unit

    override fun remove(entry: OperationEntry) = Unit
}
