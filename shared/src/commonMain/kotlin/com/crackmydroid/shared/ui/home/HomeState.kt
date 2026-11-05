package com.crackmydroid.shared.ui.home

import com.crackmydroid.shared.domain.model.operation.OperationEntry

data class HomeState(
    val recentOperations: List<OperationEntry> = emptyList()
)
