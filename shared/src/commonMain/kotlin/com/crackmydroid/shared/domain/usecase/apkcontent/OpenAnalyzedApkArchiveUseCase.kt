package com.crackmydroid.shared.domain.usecase.apkcontent

import co.touchlab.kermit.Logger
import com.crackmydroid.architecture.domain.usecase.BaseUseCase
import com.crackmydroid.architecture.logging.AppLogger
import com.crackmydroid.shared.domain.repository.ApkContentRepository

class OpenAnalyzedApkArchiveUseCase(
    private val repository: ApkContentRepository,
    logger: Logger = AppLogger.logger,
) : BaseUseCase(TAG, logger = logger) {
    private companion object { const val TAG = "OpenAnalyzedApkArchiveUseCase" }

    suspend operator fun invoke(packageName: String): Result<Unit> =
        execute { repository.prepareAndOpenArchive(packageName).getOrThrow() }
}
