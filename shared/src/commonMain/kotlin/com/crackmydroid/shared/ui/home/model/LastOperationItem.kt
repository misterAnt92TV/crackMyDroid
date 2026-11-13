package com.crackmydroid.shared.ui.home.model

/** Render-ready model for an operation shown on the home screen. */
data class LastOperationItem(
    val operationId: String,
    val description: String,
    val result: String?,
    val timestamp: String,
    val outcome: OperationOutcome,
)

