package com.crackmydroid.shared.domain.repository

import com.crackmydroid.shared.domain.model.ConnectedDevice
import com.crackmydroid.shared.domain.model.DeviceSessionState
import kotlinx.coroutines.flow.StateFlow

interface DeviceSessionController {
    val state: StateFlow<DeviceSessionState>
    suspend fun refreshDevices(adbPathOverride: String? = null): Result<List<ConnectedDevice>>
    suspend fun selectDevice(device: ConnectedDevice, adbPathOverride: String? = null): Result<Unit>
    suspend fun clearSelection()
    suspend fun rescanSelectedDevice(): Result<Unit>
}
