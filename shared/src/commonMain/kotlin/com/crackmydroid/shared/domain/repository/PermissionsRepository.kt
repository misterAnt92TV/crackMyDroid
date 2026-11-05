package com.crackmydroid.shared.domain.repository

import com.crackmydroid.shared.domain.model.app.AppPermissionEntry

interface PermissionsRepository {
    suspend fun listAppPermissions(): List<AppPermissionEntry>
    suspend fun listAppPermissionsPaginated(
        offset: Int = 0,
        limit: Int = 50
    ): List<AppPermissionEntry>
}
