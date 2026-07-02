package com.crackmydroid.shared.domain.repository

import com.crackmydroid.shared.domain.model.ActivityEntry
import com.crackmydroid.shared.domain.model.AppTheme
import com.crackmydroid.shared.domain.model.DeviceInfo
import com.crackmydroid.shared.domain.model.InstalledAppEntry
import com.crackmydroid.shared.domain.model.RootStatus
import com.crackmydroid.shared.domain.model.ShellCommand
import com.crackmydroid.shared.domain.model.ShellCommandResult
import com.crackmydroid.shared.domain.model.ExportFormat

interface ActivityRepository {
    suspend fun getActivities(): List<ActivityEntry>
    suspend fun launch(entry: ActivityEntry): Result<Unit>
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
}

interface InstalledAppsRepository {
    suspend fun listInstalled(): List<InstalledAppEntry>
    suspend fun exportApk(packageName: String): Result<String>
    suspend fun shareApk(packageName: String, bluetoothOnly: Boolean): Result<Unit>
}
