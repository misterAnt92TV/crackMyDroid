package com.crackmydroid.shared.presentation.operations

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class OperationEntry(
    val description: String,
    val success: Boolean,
    val details: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

interface OperationLogPersistence {
    suspend fun load(): List<OperationEntry>
    suspend fun save(entries: List<OperationEntry>)
}

object OperationLogStore {
    private const val MAX = 50
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _entries = MutableStateFlow<List<OperationEntry>>(emptyList())
    val entries: StateFlow<List<OperationEntry>> = _entries
    private var persistence: OperationLogPersistence? = null

    fun record(description: String, success: Boolean, details: String? = null) {
        _entries.update { list -> (listOf(OperationEntry(description, success, details)) + list).take(MAX) }
        persist()
    }

    fun configure(persistence: OperationLogPersistence) {
        this.persistence = persistence
        scope.launch {
            runCatching { persistence.load() }
                .onSuccess { loaded ->
                    if (loaded.isNotEmpty()) {
                        _entries.value = loaded.take(MAX)
                    }
                }
        }
    }

    private fun persist() {
        val current = _entries.value
        persistence?.let { p ->
            scope.launch { runCatching { p.save(current) } }
        }
    }
}
