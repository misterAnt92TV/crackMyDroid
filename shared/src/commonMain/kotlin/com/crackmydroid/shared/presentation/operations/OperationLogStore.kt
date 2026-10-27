package com.crackmydroid.shared.presentation.operations

import com.crackmydroid.shared.domain.model.OperationEntry
import com.crackmydroid.shared.domain.repository.OperationLogRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

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
    private val _entries = MutableStateFlow<List<OperationEntry>>(emptyList())
    override val entries: StateFlow<List<OperationEntry>> = _entries

    init {
        scope.launch {
            runCatching { persistence.load() }
                .onSuccess { loaded -> _entries.value = loaded.take(maxEntries) }
        }
    }

    override fun record(description: String, success: Boolean, details: String?) {
        _entries.update { list ->
            (listOf(OperationEntry(description, success, details, currentTimeMillis())) + list).take(maxEntries)
        }
        persist()
    }

    private fun persist() {
        val current = _entries.value
        scope.launch { runCatching { persistence.save(current) } }
    }
}

object NoOpOperationLogRepository : OperationLogRepository {
    private val emptyEntries = MutableStateFlow<List<OperationEntry>>(emptyList())
    override val entries: StateFlow<List<OperationEntry>> = emptyEntries

    override fun record(description: String, success: Boolean, details: String?) = Unit
}
