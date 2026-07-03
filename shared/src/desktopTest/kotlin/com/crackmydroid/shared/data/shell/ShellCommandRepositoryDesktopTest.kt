package com.crackmydroid.shared.data.shell

import com.crackmydroid.shared.data.DesktopScanCache
import com.crackmydroid.shared.domain.model.ConnectedDevice
import com.crackmydroid.shared.domain.model.ShellCommandResult
import com.crackmydroid.shared.domain.repository.AdbBridge
import com.crackmydroid.shared.domain.repository.DeviceSessionController
import com.crackmydroid.shared.domain.repository.DeviceSessionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ShellCommandRepositoryDesktopTest {
    @Test
    fun marksRootCommandsUnsupportedWhenSuIsUnavailable() = runTest {
        val repository = ShellCommandRepositoryDesktop(
            adbBridge = FakeShellAdbBridge(),
            sessionController = FakeShellSessionController(),
            cache = DesktopScanCache()
        )

        val commands = repository.listCommands()
        val rootCommand = commands.first { it.requiresRoot }
        val safeCommand = commands.first { !it.requiresRoot }

        assertFalse(rootCommand.supported)
        assertTrue(rootCommand.unsupportedReason?.contains("adb-only") == true)
        assertTrue(safeCommand.supported)

        val result = repository.execute(rootCommand)
        assertEquals(
            ShellCommandResult(
                success = false,
                stdout = "",
                stderr = rootCommand.unsupportedReason.orEmpty(),
                code = 126
            ),
            result
        )
    }
}

private class FakeShellAdbBridge : AdbBridge {
    override suspend fun resolveBinary(configuredPath: String?): Result<String> =
        Result.success(configuredPath ?: "/usr/bin/adb")

    override suspend fun listDevices(configuredPath: String?): Result<List<ConnectedDevice>> =
        Result.success(emptyList())

    override suspend fun shell(
        serial: String,
        command: String,
        configuredPath: String?,
        asRoot: Boolean
    ): Result<String> = when {
        command == "which su" -> Result.failure(IllegalStateException("su not found"))
        command == "id" && asRoot -> Result.failure(IllegalStateException("root unavailable"))
        else -> Result.success("")
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

private class FakeShellSessionController : DeviceSessionController {
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
