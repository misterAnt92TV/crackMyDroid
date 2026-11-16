package com.crackmydroid.shared.domain.usecase.installedapps

import co.touchlab.kermit.Logger
import com.crackmydroid.architecture.coroutines.AppDispatchers
import com.crackmydroid.architecture.coroutines.DefaultAppDispatchers
import com.crackmydroid.architecture.domain.usecase.BaseUseCase
import com.crackmydroid.architecture.logging.AppLogger
import com.crackmydroid.shared.domain.model.app.ManifestSecurityReport
import com.crackmydroid.shared.domain.repository.InstalledAppsRepository

class AnalyzeAppSecurityUseCase(
    private val installedAppsRepository: InstalledAppsRepository,
    dispatchers: AppDispatchers = DefaultAppDispatchers,
    logger: Logger = AppLogger.logger,
) : BaseUseCase(TAG, dispatcher = dispatchers.default, logger = logger) {
    suspend operator fun invoke(packageName: String): Result<ManifestSecurityReport> = execute {
        installedAppsRepository.analyzePackageManifestSecurity(packageName).getOrThrow()
    }

    private companion object {
        private const val TAG = "AnalyzeAppSecurityUseCase"
    }
}
