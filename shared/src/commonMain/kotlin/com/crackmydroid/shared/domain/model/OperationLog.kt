package com.crackmydroid.shared.domain.model

data class OperationEntry(
    val description: String,
    val success: Boolean,
    val details: String? = null,
    val timestamp: Long
)
