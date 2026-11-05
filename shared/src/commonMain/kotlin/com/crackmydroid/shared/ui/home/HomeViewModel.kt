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
                            .take(5)
                    )
                }
            }
        }
    }

    fun hideFromRecent(entry: OperationEntry) = operationLogs.hideFromRecent(entry)

    fun remove(entry: OperationEntry) = operationLogs.remove(entry)
}
