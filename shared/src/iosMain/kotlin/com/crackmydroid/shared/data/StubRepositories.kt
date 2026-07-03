package com.crackmydroid.shared.data

import com.crackmydroid.shared.domain.model.ActivityEntry
import com.crackmydroid.shared.domain.model.AppTheme
import com.crackmydroid.shared.domain.model.DeviceInfo
import com.crackmydroid.shared.domain.model.ExportFormat
import com.crackmydroid.shared.domain.model.InstalledAppEntry
import com.crackmydroid.shared.domain.model.RootStatus
import com.crackmydroid.shared.domain.model.ShellCommand
import com.crackmydroid.shared.domain.model.ShellCommandResult
import com.crackmydroid.shared.domain.repository.ActivityRepository
import com.crackmydroid.shared.domain.repository.DeviceInfoRepository
import com.crackmydroid.shared.domain.repository.LogRepository
import com.crackmydroid.shared.domain.repository.InstalledAppsRepository
import com.crackmydroid.shared.domain.repository.RootCheckRepository
import com.crackmydroid.shared.domain.repository.SettingsRepository
import com.crackmydroid.shared.domain.repository.ShellCommandRepository
import com.crackmydroid.shared.domain.repository.PermissionsRepository

class ActivityRepositoryIos : ActivityRepository {
    override suspend fun getActivities(): List<ActivityEntry> = emptyList()
    override suspend fun launch(
        entry: ActivityEntry,
        action: String?,
        dataUri: String?,
        mimeType: String?
    ): Result<Unit> =
        Result.failure(UnsupportedOperationException("Non supportato su iOS"))
}

class RootCheckRepositoryIos : RootCheckRepository {
    override suspend fun checkRoot(): RootStatus = RootStatus(false, "Non supportato su iOS")
    override suspend fun checkPlayIntegrity(nonce: String): com.crackmydroid.shared.domain.model.PlayIntegrityResult =
        com.crackmydroid.shared.domain.model.PlayIntegrityResult(
            basicIntegrity = null,
            deviceIntegrity = null,
            details = "Play Integrity non supportato su iOS"
        )
}

class DeviceInfoRepositoryIos : DeviceInfoRepository {
    override suspend fun getDeviceInfo(): DeviceInfo = DeviceInfo(
        manufacturer = "Apple",
        model = "iOS",
        brand = "Apple",
        device = "iOS",
        product = "iOS",
        androidVersion = "n/a",
        securityPatch = "n/a",
        buildId = null,
        buildDisplay = null,
        buildFingerprint = null,
        radio = null,
        kernelVersion = null,
        hardware = "n/a",
        bootloader = "n/a",
        batteryLevel = null,
        networkType = "n/a",
        totalMemory = null,
        freeMemory = null,
        screenResolution = null,
        densityDpi = null,
        adbEnabled = false,
        developerOptions = false,
        appDebuggable = false,
        imei = null,
        serial = null
    )
}

class ShellCommandRepositoryIos : ShellCommandRepository {
    override suspend fun listCommands(): List<ShellCommand> = emptyList()
    override suspend fun execute(command: ShellCommand): ShellCommandResult =
        ShellCommandResult(false, "", "Non supportato su iOS", -1)
}

class SettingsRepositoryIos : SettingsRepository {
    private var theme: AppTheme = AppTheme.SYSTEM
    private var verbose = false
    private var introSeen = false
    private var exportFormat = ExportFormat.TXT
    private var highContrast = false
    private var reduceMotion = false
    private var largeText = false
    private var suggestionsEnabled = true
    private var featureHintsEnabled = true
    private var adbPath = ""
    override suspend fun getTheme(): AppTheme = theme
    override suspend fun setTheme(theme: AppTheme) { this.theme = theme }
    override suspend fun isVerboseLog(): Boolean = verbose
    override suspend fun setVerboseLog(enabled: Boolean) { verbose = enabled }
    override suspend fun isIntroSeen(): Boolean = introSeen
    override suspend fun setIntroSeen(seen: Boolean) { introSeen = seen }
    override suspend fun getExportFormat(): ExportFormat = exportFormat
    override suspend fun setExportFormat(format: ExportFormat) { exportFormat = format }
    override suspend fun isHighContrast(): Boolean = highContrast
    override suspend fun setHighContrast(enabled: Boolean) { highContrast = enabled }
    override suspend fun isReduceMotion(): Boolean = reduceMotion
    override suspend fun setReduceMotion(enabled: Boolean) { reduceMotion = enabled }
    override suspend fun isLargeText(): Boolean = largeText
    override suspend fun setLargeText(enabled: Boolean) { largeText = enabled }
    override suspend fun isSuggestionsEnabled(): Boolean = suggestionsEnabled
    override suspend fun setSuggestionsEnabled(enabled: Boolean) { suggestionsEnabled = enabled }
    override suspend fun isFeatureHintsEnabled(): Boolean = featureHintsEnabled
    override suspend fun setFeatureHintsEnabled(enabled: Boolean) { featureHintsEnabled = enabled }
    override suspend fun getAdbPath(): String = adbPath
    override suspend fun setAdbPath(path: String) { adbPath = path }
}

class LogRepositoryIos : LogRepository {
    override suspend fun fetchLog(): List<String> = emptyList()
    override suspend fun export(logs: List<String>): Result<String> = Result.failure(UnsupportedOperationException("Non supportato su iOS"))
}

class PermissionsRepositoryIos : PermissionsRepository {
    override suspend fun listAppPermissions(): List<com.crackmydroid.shared.domain.model.AppPermissionEntry> = emptyList()
}

class InstalledAppsRepositoryIos : InstalledAppsRepository {
    override suspend fun listInstalled(): List<InstalledAppEntry> = emptyList()
    override suspend fun exportApk(packageName: String): Result<String> =
        Result.failure(UnsupportedOperationException("Non supportato su iOS"))
    override suspend fun shareApk(packageName: String, bluetoothOnly: Boolean): Result<Unit> =
        Result.failure(UnsupportedOperationException("Non supportato su iOS"))
}
