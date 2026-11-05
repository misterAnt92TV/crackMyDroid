package com.crackmydroid.shared.domain.model

import com.crackmydroid.shared.domain.model.scansnapshot.SnapshotMetadata

data class DeviceSessionState(
    val adbPath: String = "",
    val adbPathConfigured: Boolean = false,
    val availableDevices: List<ConnectedDevice> = emptyList(),
    val selectedDevice: ConnectedDevice? = null,
    val loading: Boolean = false,
    val error: String? = null,
    val dataSource: DeviceSessionSource = DeviceSessionSource.ADB,
    val snapshotStatus: String? = null,
    val snapshotMetadata: SnapshotMetadata? = null,
    val sessionVersion: Int = 0
)
