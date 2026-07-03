package com.crackmydroid.shared.data

import com.crackmydroid.shared.domain.model.ConnectedDevice
import com.crackmydroid.shared.domain.repository.AdbBridge
import com.crackmydroid.shared.domain.repository.DeviceSessionController

abstract class DesktopAdbRepositorySupport(
    protected val adbBridge: AdbBridge,
    protected val sessionController: DeviceSessionController
) {
    protected fun selectedDevice(): ConnectedDevice =
        requireNotNull(sessionController.state.value.selectedDevice) {
            "Nessun device selezionato"
        }

    protected fun adbPath(): String? =
        sessionController.state.value.adbPath.takeIf { it.isNotBlank() }

    protected suspend fun shell(command: String, asRoot: Boolean = false): String {
        val device = selectedDevice()
        return adbBridge.shell(
            serial = device.serial,
            command = command,
            configuredPath = adbPath(),
            asRoot = asRoot
        ).getOrThrow()
    }
}
