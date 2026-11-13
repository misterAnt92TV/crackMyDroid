package com.crackmydroid.shared.domain.usecase.root

import co.touchlab.kermit.Logger
import com.crackmydroid.architecture.domain.usecase.BaseUseCase
import com.crackmydroid.architecture.logging.AppLogger
import com.crackmydroid.shared.domain.model.PlayIntegrityResult
import com.crackmydroid.shared.domain.repository.IntegrityVerificationRepository

class VerifyPlayIntegrityTokenUseCase(
    private val repository: IntegrityVerificationRepository,
    logger: Logger = AppLogger.logger,
) : BaseUseCase(TAG, logger = logger) {
    private companion object {
        private const val TAG = "VerifyPlayIntegrityTokenUseCase"
    }

    suspend operator fun invoke(token: String, requestBinding: String): Result<PlayIntegrityResult> =
        execute { repository.verify(token, requestBinding) }
}
