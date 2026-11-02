package com.crackmydroid.shared.domain.model

data class ShellCommand(
    val title: String,
    val command: String,
    val requiresRoot: Boolean,
    val confirmation: String? = null,
    val supported: Boolean = true,
    val unsupportedReason: String? = null
)
