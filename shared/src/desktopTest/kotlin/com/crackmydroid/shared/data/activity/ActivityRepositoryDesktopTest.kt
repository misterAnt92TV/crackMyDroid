package com.crackmydroid.shared.data.activity

import com.crackmydroid.shared.data.DesktopScanCache
import com.crackmydroid.shared.domain.model.ActivityEntry
import com.crackmydroid.shared.domain.model.ConnectedDevice
import com.crackmydroid.shared.domain.model.InstalledAppEntry
import com.crackmydroid.shared.domain.repository.AdbBridge
import com.crackmydroid.shared.domain.repository.DeviceSessionController
import com.crackmydroid.shared.domain.repository.DeviceSessionState
import com.crackmydroid.shared.domain.repository.RemotePackageManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ActivityRepositoryDesktopTest {
    @Test
    fun guidedLaunchBuildsIntentCommandWithQuotedArguments() = runTest {
        val adbBridge = CapturingActivityAdbBridge()
        val repository = ActivityRepositoryDesktop(
            adbBridge = adbBridge,
            sessionController = FakeActivitySessionController(),
            cache = DesktopScanCache(),
            packageManager = FakeActivityRemotePackageManager()
        )

        repository.launch(
            entry = ActivityEntry(
                label = "DeepLink",
                appLabel = "Demo",
                packageName = "pkg.demo",
                activityName = "pkg.demo.Settings\$DeepLinkActivity",
                launchableViaShell = false,
                launchIntentAction = "android.intent.action.VIEW",
                launchIntentData = "demo://host/details",
                launchIntentMimeType = "image/*"
            ),
            action = "android.intent.action.VIEW",
            dataUri = "demo://host/details",
            mimeType = "image/*"
        ).getOrThrow()

        assertEquals(
            "am start -n 'pkg.demo/pkg.demo.Settings\$DeepLinkActivity' -a 'android.intent.action.VIEW' -d 'demo://host/details' -t 'image/*'",
            adbBridge.lastCommand
        )
    }
}

private class CapturingActivityAdbBridge : AdbBridge {
    var lastCommand: String = ""

    override suspend fun resolveBinary(configuredPath: String?): Result<String> =
        Result.success(configuredPath ?: "/usr/bin/adb")

    override suspend fun listDevices(configuredPath: String?): Result<List<ConnectedDevice>> =
        Result.success(emptyList())

    override suspend fun shell(
        serial: String,
        command: String,
        configuredPath: String?,
        asRoot: Boolean
    ): Result<String> {
        lastCommand = command
        return Result.success("Starting: Intent { ... }")
    }

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

private class FakeActivitySessionController : DeviceSessionController {
    private val internalState = MutableStateFlow(
        DeviceSessionState(
            adbPath = "/usr/bin/adb",
            selectedDevice = ConnectedDevice(serial = "SERIAL1", state = "device")
        )
    )

    override val state: StateFlow<DeviceSessionState> = internalState

    override suspend fun refreshDevices(adbPathOverride: String?): Result<List<ConnectedDevice>> =
        Result.success(emptyList())

    override suspend fun selectDevice(device: ConnectedDevice, adbPathOverride: String?): Result<Unit> =
        Result.success(Unit)

    override suspend fun clearSelection() = Unit

    override suspend fun rescanSelectedDevice(): Result<Unit> =
        Result.success(Unit)
}

private class FakeActivityRemotePackageManager : RemotePackageManager {
    override suspend fun listInstalledApps(): List<InstalledAppEntry> = emptyList()

    override suspend fun readPackageDump(packageName: String): String = ""

    override suspend fun readPackagePaths(packageName: String): List<String> = emptyList()
}
