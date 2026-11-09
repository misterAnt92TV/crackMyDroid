package com.crackmydroid.shared.domain.model.shell

data class ShellCommandResult(
    val success: Boolean,
    val stdout: String,
    val stderr: String,
    val code: Int,
)
