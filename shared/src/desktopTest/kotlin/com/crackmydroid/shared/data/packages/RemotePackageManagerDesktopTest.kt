package com.crackmydroid.shared.data.packages

import com.crackmydroid.shared.data.DesktopScanCache
import com.crackmydroid.shared.domain.model.ConnectedDevice
import com.crackmydroid.shared.domain.repository.AdbBridge
import com.crackmydroid.shared.domain.repository.DeviceSessionController
import com.crackmydroid.shared.domain.repository.DeviceSessionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class RemotePackageManagerDesktopTest {
    @Test
    fun cachesPackageQueriesPerSession() = runTest {
        val bridge = FakePackageAdbBridge(
            outputs = mapOf(
                "pm list packages -f" to """
                    package:/system/app/Calculator/Calculator.apk=com.android.calculator2
                    package:/data/app/~~hash==/base.apk=com.example.demo
                """.trimIndent(),
                "dumpsys package com.example.demo" to "requested permissions:\n  android.permission.CAMERA",
                "pm path com.example.demo" to "package:/data/app/~~hash==/base.apk"
            )
        )
        val packageManager = RemotePackageManagerDesktop(
            adbBridge = bridge,
            sessionController = FakeDeviceSessionController(),
            cache = DesktopScanCache()
        )

        val firstList = packageManager.listInstalledApps()
        val secondList = packageManager.listInstalledApps()
        val firstDump = packageManager.readPackageDump("com.example.demo")
        val secondDump = packageManager.readPackageDump("com.example.demo")
        val firstPaths = packageManager.readPackagePaths("com.example.demo")
        val secondPaths = packageManager.readPackagePaths("com.example.demo")

        assertEquals(firstList, secondList)
        assertEquals(firstDump, secondDump)
        assertEquals(firstPaths, secondPaths)
        assertEquals(1, bridge.commands.count { it == "pm list packages -f" })
        assertEquals(1, bridge.commands.count { it == "dumpsys package com.example.demo" })
        assertEquals(1, bridge.commands.count { it == "pm path com.example.demo" })
    }
}

private class FakePackageAdbBridge(
    private val outputs: Map<String, String>
) : AdbBridge {
    val commands: MutableList<String> = mutableListOf()

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
        commands += command
        return Result.success(outputs.getValue(command))
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

private class FakeDeviceSessionController : DeviceSessionController {
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
