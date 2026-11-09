package com.crackmydroid.shared.domain.usecase.info

import co.touchlab.kermit.Logger
import com.crackmydroid.shared.AppLogger
import com.crackmydroid.shared.domain.model.DeviceInfo
import com.crackmydroid.shared.domain.repository.DeviceInfoRepository
import com.crackmydroid.shared.domain.usecase.BaseUseCase

class GetDeviceInfoUseCase(
    private val repo: DeviceInfoRepository,
    logger: Logger = AppLogger.logger,
) : BaseUseCase(TAG, logger = logger) {
    private companion object {
        private const val TAG = "GetDeviceInfoUseCase"
    }

    suspend operator fun invoke(): Result<DeviceInfo> = execute { repo.getDeviceInfo() }
}
