package com.crackmydroid.shared.data.device

import com.crackmydroid.shared.data.DesktopScanCache
import com.crackmydroid.shared.domain.model.AppTheme
import com.crackmydroid.shared.domain.model.ConnectedDevice
import com.crackmydroid.shared.domain.model.ExportFormat
import com.crackmydroid.shared.domain.repository.AdbBridge
import com.crackmydroid.shared.domain.repository.SettingsRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DeviceSessionControllerDesktopTest {
    @Test
    fun refreshAndSelectDeviceUpdatesStateAndClearsCache() = runTest {
        val cache = DesktopScanCache().apply {
            installedApps = listOf(com.crackmydroid.shared.domain.model.InstalledAppEntry("Pkg", "pkg", "/tmp/pkg.apk"))
        }
        val settings = FakeSettingsRepository()
        val bridge = FakeAdbBridge(
            devices = listOf(
                ConnectedDevice(serial = "SERIAL1", state = "device", model = "Pixel 8")
            )
        )
        val controller = DeviceSessionControllerDesktop(bridge, settings, cache)

        controller.refreshDevices("/opt/homebrew/bin/adb")
        controller.selectDevice(bridge.devices.first(), "/opt/homebrew/bin/adb")

        assertEquals("SERIAL1", controller.state.value.selectedDevice?.serial)
        assertEquals("/opt/homebrew/bin/adb", settings.adbPath)
        assertEquals(null, cache.installedApps)
        assertEquals(1, controller.state.value.sessionVersion)
    }

    @Test
    fun rescanAndClearSelectionWork() = runTest {
        val cache = DesktopScanCache()
        val settings = FakeSettingsRepository()
        val bridge = FakeAdbBridge(
            devices = listOf(
                ConnectedDevice(serial = "SERIAL2", state = "device", model = "Galaxy")
            )
        )
        val controller = DeviceSessionControllerDesktop(bridge, settings, cache)

        controller.refreshDevices("/usr/local/bin/adb")
        controller.selectDevice(bridge.devices.first(), "/usr/local/bin/adb")
        controller.rescanSelectedDevice()
        controller.clearSelection()

        assertEquals(2, controller.state.value.sessionVersion)
        assertNull(controller.state.value.selectedDevice)
        assertTrue(controller.state.value.availableDevices.isNotEmpty())
    }
}

private class FakeAdbBridge(
    val devices: List<ConnectedDevice>
) : AdbBridge {
    override suspend fun resolveBinary(configuredPath: String?): Result<String> =
        Result.success(configuredPath ?: "/usr/bin/adb")

    override suspend fun listDevices(configuredPath: String?): Result<List<ConnectedDevice>> =
        Result.success(devices)

    override suspend fun shell(
        serial: String,
        command: String,
        configuredPath: String?,
        asRoot: Boolean
    ): Result<String> = Result.success("")

    override suspend fun pull(
        serial: String,
        remotePath: String,
        localPath: String,
        configuredPath: String?
    ): Result<String> = Result.success(localPath)

    override suspend fun logcat(
        serial: String,
        configuredPath: String?,
        lines: Int
    ): Result<List<String>> = Result.success(emptyList())
}

private class FakeSettingsRepository : SettingsRepository {
    var adbPath: String = ""
    private var theme = AppTheme.SYSTEM
    private var verbose = false
    private var introSeen = false
    private var exportFormat = ExportFormat.TXT
    private var highContrast = false
    private var reduceMotion = false
    private var largeText = false
    private var suggestions = true
    private var featureHints = true

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
    override suspend fun isSuggestionsEnabled(): Boolean = suggestions
    override suspend fun setSuggestionsEnabled(enabled: Boolean) { suggestions = enabled }
    override suspend fun isFeatureHintsEnabled(): Boolean = featureHints
    override suspend fun setFeatureHintsEnabled(enabled: Boolean) { featureHints = enabled }
    override suspend fun getAdbPath(): String = adbPath
    override suspend fun setAdbPath(path: String) { adbPath = path }
}
