package com.crackmydroid.shared.ui.logs

import com.crackmydroid.architecture.presentation.model.UiState
import com.crackmydroid.architecture.presentation.model.UiLoadState
import com.crackmydroid.architecture.presentation.model.errorMessage

data class LogState(
    val logs: List<String> = emptyList(),
    val filter: String = "",
    val levels: Set<Char> = emptySet(), // E, W, I, D, V
    val colorByLevel: Boolean = true,
    val loadState: UiLoadState = UiLoadState.Idle,
    val exportLoadState: UiLoadState = UiLoadState.Idle,
    val message: String? = null,
    val visibleLogs: List<String> = emptyList(),
    val trimmed: Boolean = false,
) : UiState {
    val loading: Boolean get() = loadState is UiLoadState.Loading
    val errorMessage: String? get() = loadState.errorMessage ?: exportLoadState.errorMessage
}
