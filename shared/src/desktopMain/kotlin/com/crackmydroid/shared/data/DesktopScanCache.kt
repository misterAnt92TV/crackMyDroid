package com.crackmydroid.shared.data

import com.crackmydroid.shared.domain.model.ActivityEntry
import com.crackmydroid.shared.domain.model.AppPermissionEntry
import com.crackmydroid.shared.domain.model.DeviceInfo
import com.crackmydroid.shared.domain.model.InstalledAppEntry
import com.crackmydroid.shared.domain.model.PlayIntegrityResult
import com.crackmydroid.shared.domain.model.RootStatus
import com.crackmydroid.shared.domain.model.SnapshotMetadata

class DesktopScanCache {
    var deviceInfo: DeviceInfo? = null
    var installedApps: List<InstalledAppEntry>? = null
    var permissions: List<AppPermissionEntry>? = null
    var activities: List<ActivityEntry>? = null
    var rootStatus: RootStatus? = null
    var playIntegrity: PlayIntegrityResult? = null
    var logs: List<String>? = null
    var rootShellAvailable: Boolean? = null
    var snapshotMetadata: SnapshotMetadata? = null
    var snapshotLoadAttempted: Boolean = false
    val packageDumps: MutableMap<String, String> = mutableMapOf()
    val packagePaths: MutableMap<String, List<String>> = mutableMapOf()

    fun clearAll() {
        deviceInfo = null
        installedApps = null
        permissions = null
        activities = null
        rootStatus = null
        playIntegrity = null
        logs = null
        rootShellAvailable = null
        snapshotMetadata = null
        snapshotLoadAttempted = false
        packageDumps.clear()
        packagePaths.clear()
    }
}
