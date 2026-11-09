package com.crackmydroid.shared.domain.usecase.permissions

import co.touchlab.kermit.Logger
import com.crackmydroid.shared.AppLogger
import com.crackmydroid.shared.domain.model.app.AppPermissionEntry
import com.crackmydroid.shared.domain.repository.PermissionsRepository
import com.crackmydroid.shared.domain.usecase.BaseUseCase

class ListPermissionsUseCase(
    private val repo: PermissionsRepository,
    logger: Logger = AppLogger.logger,
) : BaseUseCase(TAG, logger = logger) {
    private companion object {
        private const val TAG = "ListPermissionsUseCase"
    }

    suspend operator fun invoke(): Result<List<AppPermissionEntry>> = execute { repo.listAppPermissions() }
}
