package com.crackmydroid.shared.domain.usecase.apkcontent

import co.touchlab.kermit.Logger
import com.crackmydroid.architecture.domain.usecase.BaseUseCase
import com.crackmydroid.architecture.logging.AppLogger
import com.crackmydroid.shared.domain.model.apkcontent.ApkContentAnalysis
import com.crackmydroid.shared.domain.repository.ApkContentRepository

class AnalyzeApkContentUseCase(
    private val repository: ApkContentRepository,
    logger: Logger = AppLogger.logger,
) : BaseUseCase(TAG, logger = logger) {
    private companion object { const val TAG = "AnalyzeApkContentUseCase" }

    suspend operator fun invoke(packageName: String): Result<ApkContentAnalysis> =
        execute { repository.analyze(packageName).getOrThrow() }
}
