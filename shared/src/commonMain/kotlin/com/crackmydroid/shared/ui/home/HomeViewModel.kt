package com.crackmydroid.shared.ui.home

import com.crackmydroid.shared.domain.model.operation.OperationEntry
import com.crackmydroid.shared.domain.repository.log.OperationLogRepository
import com.crackmydroid.shared.presentation.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val operationLogs: OperationLogRepository
) : BaseViewModel() {
    private companion object {
        const val MAX_RECENT_OPERATIONS = 5
    }

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state

    init {
        scope.launch {
            operationLogs.entries.collect { entries ->
                _state.update {
                    it.copy(
                        recentOperations = entries
                            .filterNot(OperationEntry::hiddenFromRecent)
                            .distinctBy { entry -> entry.description to entry.success }
                                .take(MAX_RECENT_OPERATIONS)
                    )
                }
            }
        }
    }

    fun hideFromRecent(operationId: String) {
        operationLogs.entries.value
            .firstOrNull { entry -> entry.stableId() == operationId }
            ?.let(operationLogs::hideFromRecent)
    }

    fun remove(operationId: String) {
        operationLogs.entries.value
            .firstOrNull { entry -> entry.stableId() == operationId }
            ?.let(operationLogs::remove)
    }

    private fun OperationEntry.stableId(): String =
        operationId.ifBlank { "$timestamp:$description:$success" }
}
