package com.crackmydroid.shared.domain.model.operation

/** Stable event identifier stored in operation history instead of translated copy. */
data class OperationEvent(val key: String, val arguments: List<String> = emptyList())
