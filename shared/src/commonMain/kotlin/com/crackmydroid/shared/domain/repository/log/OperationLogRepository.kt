package com.crackmydroid.shared.domain.repository.log

import com.crackmydroid.shared.domain.model.operation.OperationEntry
import com.crackmydroid.shared.domain.model.operation.OperationEvent
import kotlinx.coroutines.flow.StateFlow

interface OperationLogRepository {
    val entries: StateFlow<List<OperationEntry>>

    fun record(description: String, success: Boolean, details: String? = null)

    fun record(event: OperationEvent, success: Boolean, details: String? = null)
}