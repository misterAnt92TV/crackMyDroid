package com.crackmydroid.shared.domain.usecase.info

import com.crackmydroid.shared.domain.model.DeviceInfo
import com.crackmydroid.shared.domain.repository.DeviceInfoRepository

class GetDeviceInfoUseCase(private val repo: DeviceInfoRepository) {
    suspend operator fun invoke(): DeviceInfo = repo.getDeviceInfo()
}
