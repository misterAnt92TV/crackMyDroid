package com.crackmydroid.shared.domain.repository

interface LogRepository {
    suspend fun fetchLog(): List<String>
    suspend fun export(logs: List<String>): Result<String>
}

interface PermissionsRepository {
    suspend fun listAppPermissions(): List<com.crackmydroid.shared.domain.model.AppPermissionEntry>
}
