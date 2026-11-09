package com.crackmydroid.shared.domain.usecase.root

import co.touchlab.kermit.Logger
import com.crackmydroid.shared.AppLogger
import com.crackmydroid.shared.domain.model.RootStatus
import com.crackmydroid.shared.domain.model.root.RootCheckMode
import com.crackmydroid.shared.domain.repository.RootCheckRepository
import com.crackmydroid.shared.domain.usecase.BaseUseCase

class CheckRootUseCase(
    private val repository: RootCheckRepository,
    logger: Logger = AppLogger.logger,
) : BaseUseCase(TAG, logger = logger) {
    private companion object {
        private const val TAG = "CheckRootUseCase"
    }

    suspend operator fun invoke(mode: RootCheckMode = RootCheckMode.PASSIVE): Result<RootStatus> =
        execute { repository.checkRoot(mode) }
}
