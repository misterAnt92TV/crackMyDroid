package com.crackmydroid.shared.ui.home

import com.crackmydroid.shared.ui.home.model.LastOperationItem

data class HomeUiState(
    val latestOperations: List<LastOperationItem> = emptyList(),
    val operationHistory: List<LastOperationItem> = emptyList(),
)
