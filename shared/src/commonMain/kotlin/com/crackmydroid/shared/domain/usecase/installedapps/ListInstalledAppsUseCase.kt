package com.crackmydroid.shared.domain.usecase.installedapps

import com.crackmydroid.shared.domain.model.InstalledAppEntry
import com.crackmydroid.shared.domain.repository.InstalledAppsRepository
import co.touchlab.kermit.Logger
import com.crackmydroid.shared.AppLogger
import com.crackmydroid.shared.domain.usecase.BaseUseCase

class ListInstalledAppsUseCase(
    private val repository: InstalledAppsRepository,
    logger: Logger = AppLogger.logger
) : BaseUseCase(TAG, logger = logger) {
    private companion object {
        private const val TAG = "ListInstalledAppsUseCase"
    }

    suspend operator fun invoke(): Result<List<InstalledAppEntry>> = execute { repository.listInstalled() }
}
