package com.crackmydroid.shared.ui.home

/** Render-ready model for an operation shown on the home screen. */
data class LastOperationItem(
    val operationId: String,
    val description: String,
    val result: String?,
    val timestamp: String,
    val outcome: OperationOutcome,
)

enum class OperationOutcome {
    Success,
    Failure,
}

data class HomeUiState(
    val latestOperations: List<LastOperationItem> = emptyList(),
    val operationHistory: List<LastOperationItem> = emptyList(),
)
