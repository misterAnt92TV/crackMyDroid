package com.crackmydroid.shared.ui.home.model

import com.crackmydroid.architecture.presentation.model.UiState
import com.crackmydroid.shared.domain.model.operation.OperationEntry

data class HomeState(
    val recentOperations: List<OperationEntry> = emptyList(),
    val operationHistory: List<OperationEntry> = emptyList(),
) : UiState
