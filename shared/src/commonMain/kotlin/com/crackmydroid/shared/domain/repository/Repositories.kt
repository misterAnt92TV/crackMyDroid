package com.crackmydroid.shared.domain.repository

import com.crackmydroid.shared.domain.model.ActivityEntry
import com.crackmydroid.shared.domain.model.AppTheme
import com.crackmydroid.shared.domain.model.DeviceInfo
import com.crackmydroid.shared.domain.model.InstalledAppEntry
import com.crackmydroid.shared.domain.model.RootStatus
import com.crackmydroid.shared.domain.model.ShellCommand
import com.crackmydroid.shared.domain.model.ShellCommandResult
import com.crackmydroid.shared.domain.model.ExportFormat
import com.crackmydroid.shared.domain.model.ConnectedDevice
import com.crackmydroid.shared.domain.model.SnapshotMetadata
import kotlinx.coroutines.flow.StateFlow

interface ActivityRepository {
    suspend fun getActivities(): List<ActivityEntry>
    suspend fun launch(
        entry: ActivityEntry,
        action: String? = null,
        dataUri: String? = null,
        mimeType: String? = null
    ): Result<Unit>
}

interface RootCheckRepository {
    suspend fun checkRoot(): RootStatus
    suspend fun checkPlayIntegrity(nonce: String = "sample-nonce"): com.crackmydroid.shared.domain.model.PlayIntegrityResult
}

interface DeviceInfoRepository {
    suspend fun getDeviceInfo(): DeviceInfo
}

interface ShellCommandRepository {
    suspend fun listCommands(): List<ShellCommand>
    suspend fun execute(command: ShellCommand): ShellCommandResult
}

interface SettingsRepository {
    suspend fun getTheme(): AppTheme
    suspend fun setTheme(theme: AppTheme)
    suspend fun isVerboseLog(): Boolean
    suspend fun setVerboseLog(enabled: Boolean)
    suspend fun isIntroSeen(): Boolean
    suspend fun setIntroSeen(seen: Boolean)
    suspend fun getExportFormat(): ExportFormat
    suspend fun setExportFormat(format: ExportFormat)
    suspend fun isHighContrast(): Boolean
    suspend fun setHighContrast(enabled: Boolean)
    suspend fun isReduceMotion(): Boolean
    suspend fun setReduceMotion(enabled: Boolean)
    suspend fun isLargeText(): Boolean
    suspend fun setLargeText(enabled: Boolean)
    suspend fun isSuggestionsEnabled(): Boolean
    suspend fun setSuggestionsEnabled(enabled: Boolean)
    suspend fun isFeatureHintsEnabled(): Boolean
    suspend fun setFeatureHintsEnabled(enabled: Boolean)
    suspend fun getAdbPath(): String
    suspend fun setAdbPath(path: String)
}

interface InstalledAppsRepository {
    suspend fun listInstalled(): List<InstalledAppEntry>
    suspend fun exportApk(packageName: String): Result<String>
    suspend fun shareApk(packageName: String, bluetoothOnly: Boolean): Result<Unit>
}

interface RemotePackageManager {
    suspend fun listInstalledApps(): List<InstalledAppEntry>
    suspend fun readPackageDump(packageName: String): String
    suspend fun readPackagePaths(packageName: String): List<String>
}

interface AdbBridge {
    suspend fun resolveBinary(configuredPath: String? = null): Result<String>
    suspend fun listDevices(configuredPath: String? = null): Result<List<ConnectedDevice>>
    suspend fun shell(
        serial: String,
        command: String,
        configuredPath: String? = null,
        asRoot: Boolean = false
    ): Result<String>
    suspend fun pull(
        serial: String,
        remotePath: String,
        localPath: String,
        configuredPath: String? = null
    ): Result<String>
    suspend fun logcat(
        serial: String,
        configuredPath: String? = null,
        lines: Int = 1500
    ): Result<List<String>>
}

data class DeviceSessionState(
    val adbPath: String = "",
    val adbPathConfigured: Boolean = false,
    val availableDevices: List<ConnectedDevice> = emptyList(),
    val selectedDevice: ConnectedDevice? = null,
    val loading: Boolean = false,
    val error: String? = null,
    val dataSourceLabel: String = "ADB live",
    val snapshotStatus: String? = null,
    val snapshotMetadata: SnapshotMetadata? = null,
    val sessionVersion: Int = 0
)

interface DeviceSessionController {
    val state: StateFlow<DeviceSessionState>
    suspend fun refreshDevices(adbPathOverride: String? = null): Result<List<ConnectedDevice>>
    suspend fun selectDevice(device: ConnectedDevice, adbPathOverride: String? = null): Result<Unit>
    suspend fun clearSelection()
    suspend fun rescanSelectedDevice(): Result<Unit>
}
