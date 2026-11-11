package com.crackmydroid.shared.domain.repository

import com.crackmydroid.shared.domain.model.DeviceInfo

interface DeviceInfoRepository {
    suspend fun getDeviceInfo(): DeviceInfo
}
