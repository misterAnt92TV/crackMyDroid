package com.crackmydroid.shared.domain.usecase.root

import com.crackmydroid.shared.domain.model.PlayIntegrityResult
import com.crackmydroid.shared.domain.repository.RootCheckRepository
import co.touchlab.kermit.Logger
import com.crackmydroid.shared.AppLogger
import com.crackmydroid.shared.domain.usecase.BaseUseCase

class CheckPlayIntegrityUseCase(
    private val repository: RootCheckRepository,
    logger: Logger = AppLogger.logger
) : BaseUseCase(TAG, logger = logger) {
    private companion object {
        private const val TAG = "CheckPlayIntegrityUseCase"
    }

    suspend operator fun invoke(nonce: String = "sample-nonce"): Result<PlayIntegrityResult> =
        execute { repository.checkPlayIntegrity(nonce) }
}
