package com.crackmydroid.shared.domain.usecase

import com.crackmydroid.shared.domain.model.AppPermissionEntry
import com.crackmydroid.shared.domain.repository.PermissionsRepository

class ListPermissionsUseCase(private val repo: PermissionsRepository) {
    suspend operator fun invoke(): List<AppPermissionEntry> = repo.listAppPermissions()
}
