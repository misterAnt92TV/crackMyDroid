package com.crackmydroid.shared.domain.model

data class ShellCommandResult(
    val success: Boolean,
    val stdout: String,
    val stderr: String,
    val code: Int
)
