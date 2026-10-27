package com.crackmydroid.shared.domain.repository

import com.crackmydroid.shared.domain.model.OperationEntry
import kotlinx.coroutines.flow.StateFlow

interface LogRepository {
    suspend fun fetchLog(): List<String>
    suspend fun export(logs: List<String>): Result<String>
}

interface OperationLogRepository {
    val entries: StateFlow<List<OperationEntry>>

    fun record(description: String, success: Boolean, details: String? = null)
}

interface PermissionsRepository {
    suspend fun listAppPermissions(): List<com.crackmydroid.shared.domain.model.AppPermissionEntry>
    suspend fun listAppPermissionsPaginated(offset: Int = 0, limit: Int = 50): List<com.crackmydroid.shared.domain.model.AppPermissionEntry>
}
