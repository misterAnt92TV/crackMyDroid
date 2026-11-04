package com.crackmydroid.shared.domain.model.operation

data class OperationEntry(
    val description: String,
    val success: Boolean,
    val details: String? = null,
    val timestamp: Long,
    val event: OperationEvent? = null,
    val legacyDescription: String? = description,
)
