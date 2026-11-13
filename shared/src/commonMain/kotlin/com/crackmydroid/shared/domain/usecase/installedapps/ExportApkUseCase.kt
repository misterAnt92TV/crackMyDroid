package com.crackmydroid.shared.domain.usecase.installedapps

import co.touchlab.kermit.Logger
import com.crackmydroid.architecture.domain.usecase.BaseUseCase
import com.crackmydroid.architecture.logging.AppLogger
import com.crackmydroid.shared.domain.repository.InstalledAppsRepository

class ExportApkUseCase(private val repository: InstalledAppsRepository, logger: Logger = AppLogger.logger) :
    BaseUseCase(TAG, logger = logger) {
    private companion object {
        private const val TAG = "ExportApkUseCase"
    }

    suspend operator fun invoke(packageName: String): Result<String> =
        execute { repository.exportApk(packageName).getOrThrow() }
}
