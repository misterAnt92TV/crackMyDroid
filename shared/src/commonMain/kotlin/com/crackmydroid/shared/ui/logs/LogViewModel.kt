package com.crackmydroid.shared.ui.logs

import com.crackmydroid.shared.domain.usecase.ExportLogsUseCase
import com.crackmydroid.shared.domain.usecase.GetLogsUseCase
import com.crackmydroid.shared.presentation.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LogViewModel(
    private val getLogs: GetLogsUseCase,
    private val exportLogs: ExportLogsUseCase
) : BaseViewModel() {
    private val _state = MutableStateFlow(LogState())
    val state: StateFlow<LogState> = _state
    private var recomputeJob: Job? = null

    fun refresh() {
        scope.launch {
            _state.update { it.copy(loading = true, error = null, message = null) }
            runCatching { getLogs() }
                .onSuccess { list ->
                    val trimmed = if (list.size > MAX_LINES) list.takeLast(MAX_LINES) else list
                    recomputeAsync(_state.value.copy(logs = trimmed, trimmed = list.size > MAX_LINES, loading = false))
                }
                .onFailure { err -> _state.update { it.copy(error = err.message, loading = false) } }
        }
    }

    fun setFilter(value: String) {
        recomputeAsync(_state.value.copy(filter = value))
    }

    fun filtered(): List<String> = _state.value.visibleLogs

    fun export() {
        scope.launch {
            val current = _state.value.visibleLogs
            runCatching { exportLogs(current) }
                .onSuccess { res -> _state.update { it.copy(message = res.getOrNull()) } }
                .onFailure { err -> _state.update { it.copy(error = err.message) } }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun toggleLevel(level: Char) {
        val current = _state.value
        val set = current.levels.toMutableSet()
        if (set.contains(level)) set.remove(level) else set.add(level)
        recomputeAsync(current.copy(levels = set))
    }

    fun setColorByLevel(enabled: Boolean) {
        _state.update { it.copy(colorByLevel = enabled) }
    }

    private fun recompute(state: LogState): LogState {
        val f = state.filter
        val lv = state.levels
        val filtered = state.logs.filter { line ->
            (f.isBlank() || line.contains(f, ignoreCase = true)) &&
                (lv.isEmpty() || lv.any { level -> line.startsWith("$level/") || line.contains(" $level/", ignoreCase = true) })
        }
        return state.copy(visibleLogs = filtered)
    }

    private fun recomputeAsync(state: LogState) {
        _state.value = state
        recomputeJob?.cancel()
        recomputeJob = scope.launch(Dispatchers.Default) {
            val newState = recompute(state)
            withContext(Dispatchers.Main) { _state.value = newState }
        }
    }

    companion object {
        private const val MAX_LINES = 1500
    }
}
