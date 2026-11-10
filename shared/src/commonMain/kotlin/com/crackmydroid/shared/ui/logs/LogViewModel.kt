package com.crackmydroid.shared.ui.logs

import com.crackmydroid.architecture.coroutines.AppDispatchers
import com.crackmydroid.shared.domain.usecase.log.ExportLogsUseCase
import com.crackmydroid.shared.domain.usecase.log.GetLogsUseCase
import com.crackmydroid.shared.presentation.LocalizedBaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

class LogViewModel(
    private val getLogs: GetLogsUseCase,
    private val exportLogs: ExportLogsUseCase,
    dispatchers: AppDispatchers,
) : LocalizedBaseViewModel(dispatchers) {
    private val _state = MutableStateFlow(LogState())
    val state: StateFlow<LogState> = _state

    fun refresh() {
        launchLatest("logs-refresh") {
            _state.update { it.copy(loading = true, error = null, message = null) }
            getLogs()
                .onSuccess { list ->
                    if (!isCurrent()) return@onSuccess
                    val trimmed = if (list.size > MAX_LINES) list.takeLast(MAX_LINES) else list
                    recomputeAsync(
                        _state.value.copy(
                            logs = trimmed,
                            trimmed = list.size > MAX_LINES,
                            loading = false,
                        ),
                    )
                }
                .onFailure { err ->
                    if (!isCurrent()) return@onFailure
                    _state.update {
                        it.copy(
                            error = err.message,
                            loading = false,
                        )
                    }
                }
        }
    }

    fun setFilter(value: String) {
        recomputeAsync(_state.value.copy(filter = value))
    }

    fun filtered(): List<String> = _state.value.visibleLogs

    fun export() {
        launchTask {
            val current = _state.value.visibleLogs
            exportLogs(current)
                .onSuccess { path -> _state.update { it.copy(message = path) } }
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
                (
                    lv.isEmpty() || lv.any { level ->
                        line.startsWith("$level/") || line.contains(
                            " $level/",
                            ignoreCase = true,
                        )
                    }
                    )
        }
        return state.copy(visibleLogs = filtered)
    }

    private fun recomputeAsync(state: LogState) {
        _state.value = state
        launchLatest("logs-recompute") {
            val newState = withContext(dispatchers.default) { recompute(state) }
            ensureCurrent()
            _state.value = newState
        }
    }

    companion object {
        private const val MAX_LINES = 1500
    }
}
