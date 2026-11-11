package com.crackmydroid.shared.ui.home.model

import com.crackmydroid.shared.domain.model.operation.OperationEntry
import com.crackmydroid.shared.i18n.Strings
import com.crackmydroid.shared.i18n.operation.formatOperationTimestamp
import com.crackmydroid.shared.i18n.operation.renderOperation
import com.crackmydroid.shared.ui.home.HomeUiState
import com.crackmydroid.shared.ui.home.LastOperationItem
import com.crackmydroid.shared.ui.home.OperationOutcome

class HomeUiStateMapper {
    fun map(state: HomeState, strings: Strings, languageCode: String): HomeUiState {
        val operations = state.recentOperations.map { entry ->
            entry.toLastOperationItem(strings, languageCode)
        }
        return HomeUiState(
            latestOperations = operations,
            operationHistory = operations,
        )
    }

    private fun OperationEntry.toLastOperationItem(strings: Strings, languageCode: String): LastOperationItem =
        LastOperationItem(
            operationId = operationId.ifBlank { "$timestamp:$description:$success" },
            description = event?.let(strings::renderOperation) ?: legacyDescription ?: description,
            result = details ?: if (success) strings.operationSuccess else strings.operationFailed,
            timestamp = formatOperationTimestamp(timestamp, languageCode),
            outcome = if (success) OperationOutcome.Success else OperationOutcome.Failure,
        )
}
