package com.crackmydroid.shared.ui.home

import com.crackmydroid.architecture.coroutines.AppDispatchers
import com.crackmydroid.shared.domain.model.operation.OperationEntry
import com.crackmydroid.shared.domain.repository.log.OperationLogRepository
import com.crackmydroid.shared.presentation.LocalizedBaseViewModel
import com.crackmydroid.shared.ui.home.model.HomeState

class HomeViewModel(private val operationLogs: OperationLogRepository, dispatchers: AppDispatchers) :
    LocalizedBaseViewModel<HomeEvent, HomeState>(HomeState(), dispatchers) {
    private companion object {
        const val MAX_RECENT_OPERATIONS = 5
    }


    override fun onEvent(event: HomeEvent) = when (event) {
        is HomeEvent.HideFromRecent -> hideFromRecent(event.operationId)
        is HomeEvent.Remove -> remove(event.operationId)
    }

    init {
        launchTask {
            operationLogs.entries.collect { entries ->
                updateState {
                    it.copy(
                        recentOperations = entries
                            .filterNot(OperationEntry::hiddenFromRecent)
                            .distinctBy { entry -> entry.description to entry.success }
                            .take(MAX_RECENT_OPERATIONS),
                    )
                }
            }
        }
    }

    private fun hideFromRecent(operationId: String) {
        operationLogs.entries.value
            .firstOrNull { entry -> entry.stableId() == operationId }
            ?.let(operationLogs::hideFromRecent)
    }

    private fun remove(operationId: String) {
        operationLogs.entries.value
            .firstOrNull { entry -> entry.stableId() == operationId }
            ?.let(operationLogs::remove)
    }

    private fun OperationEntry.stableId(): String = operationId.ifBlank { "$timestamp:$description:$success" }
}
