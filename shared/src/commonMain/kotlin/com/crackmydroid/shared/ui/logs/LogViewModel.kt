package com.crackmydroid.shared.ui.logs

import com.crackmydroid.architecture.coroutines.AppDispatchers
import com.crackmydroid.architecture.presentation.model.UiLoadState
import com.crackmydroid.shared.domain.usecase.log.ExportLogsUseCase
import com.crackmydroid.shared.domain.usecase.log.GetLogsUseCase
import com.crackmydroid.shared.presentation.LocalizedBaseViewModel
import kotlinx.coroutines.withContext

class LogViewModel(
    private val getLogsUseCase: GetLogsUseCase,
    private val exportLogsUseCase: ExportLogsUseCase,
    dispatchers: AppDispatchers,
) : LocalizedBaseViewModel<LogEvent, LogState>(LogState(), dispatchers) {
    private companion object {
        const val REFRESH_KEY = "logs-refresh"
        const val RECOMPUTE_KEY = "logs-recompute"
        const val MAX_LINES = 1500
    }

    override fun onEvent(event: LogEvent) = when (event) {
        is LogEvent.Refresh -> refresh()
        is LogEvent.FilterChanged -> setFilter(event.value)
        is LogEvent.Export -> export()
        is LogEvent.ErrorDismissed -> clearError()
        is LogEvent.LevelToggled -> toggleLevel(event.level)
        is LogEvent.ColorByLevelChanged -> setColorByLevel(event.enabled)
    }

    private fun refresh() {
        launchLatest(REFRESH_KEY) {
            updateState { it.copy(loadState = UiLoadState.Loading, message = null) }
            getLogsUseCase()
                .onSuccess { list ->
                    if (!isCurrent()) return@onSuccess
                    val trimmed = if (list.size > MAX_LINES) list.takeLast(MAX_LINES) else list
                    recomputeAsync(
                        currentState.copy(
                            logs = trimmed,
                            trimmed = list.size > MAX_LINES,
                            loadState = UiLoadState.Success,
                        ),
                    )
                }
                .onFailure { err ->
                    if (!isCurrent()) return@onFailure
                    updateState {
                        it.copy(
                            loadState = UiLoadState.Error(err.message ?: presentationTexts.error),
                        )
                    }
                }
        }
    }

    private fun setFilter(value: String) {
        recomputeAsync(currentState.copy(filter = value))
    }

    private fun filtered(): List<String> = currentState.visibleLogs

    private fun export() {
        launchTask {
            val current = currentState.visibleLogs
            exportLogsUseCase(current)
                .onSuccess { path -> updateState { it.copy(message = path, exportLoadState = UiLoadState.Success) } }
                .onFailure { err ->
                    updateState { it.copy(exportLoadState = UiLoadState.Error(err.message ?: presentationTexts.error)) }
                }
        }
    }

    private fun clearError() {
        updateState { it.copy(loadState = UiLoadState.Idle, exportLoadState = UiLoadState.Idle) }
    }

    private fun toggleLevel(level: Char) {
        val current = currentState
        val set = current.levels.toMutableSet()
        if (set.contains(level)) set.remove(level) else set.add(level)
        recomputeAsync(current.copy(levels = set))
    }

    private fun setColorByLevel(enabled: Boolean) {
        updateState { it.copy(colorByLevel = enabled) }
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
        emitState(state)
        launchLatest(RECOMPUTE_KEY) {
            val newState = withContext(dispatchers.default) { recompute(state) }
            ensureCurrent()
            emitState(newState)
        }
    }

}
