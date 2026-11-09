package com.crackmydroid.shared.domain.usecase.installedapps

import co.touchlab.kermit.Logger
import com.crackmydroid.shared.AppLogger
import com.crackmydroid.shared.domain.repository.InstalledAppsRepository
import com.crackmydroid.shared.domain.usecase.BaseUseCase

class ShareApkUseCase(
    private val repository: InstalledAppsRepository,
    logger: Logger = AppLogger.logger,
) : BaseUseCase(TAG, logger = logger) {
    private companion object {
        private const val TAG = "ShareApkUseCase"
    }

    suspend operator fun invoke(packageName: String, bluetoothOnly: Boolean): Result<Unit> =
        execute { repository.shareApk(packageName, bluetoothOnly).getOrThrow() }
}
