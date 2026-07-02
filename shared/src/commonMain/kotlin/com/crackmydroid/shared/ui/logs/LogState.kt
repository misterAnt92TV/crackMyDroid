package com.crackmydroid.shared.ui.logs

data class LogState(
    val logs: List<String> = emptyList(),
    val filter: String = "",
    val levels: Set<Char> = emptySet(), // E, W, I, D, V
    val colorByLevel: Boolean = true,
    val loading: Boolean = false,
    val message: String? = null,
    val error: String? = null,
    val visibleLogs: List<String> = emptyList(),
    val trimmed: Boolean = false
)
