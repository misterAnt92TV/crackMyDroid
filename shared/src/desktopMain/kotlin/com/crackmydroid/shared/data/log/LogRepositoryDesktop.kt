package com.crackmydroid.shared.data.log

import com.crackmydroid.shared.data.DesktopAdbRepositorySupport
import com.crackmydroid.shared.data.DesktopScanCache
import com.crackmydroid.shared.domain.repository.AdbBridge
import com.crackmydroid.shared.domain.repository.DeviceSessionController
import com.crackmydroid.shared.domain.repository.LogRepository
import com.crackmydroid.shared.util.saveTextFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LogRepositoryDesktop(
    adbBridge: AdbBridge,
    sessionController: DeviceSessionController,
    private val cache: DesktopScanCache
) : DesktopAdbRepositorySupport(adbBridge, sessionController), LogRepository {
    override suspend fun fetchLog(): List<String> = withContext(Dispatchers.IO) {
        cache.logs?.let { return@withContext it }
        val device = selectedDevice()
        adbBridge.logcat(
            serial = device.serial,
            configuredPath = adbPath()
        ).getOrElse { listOf("Errore lettura log: ${it.message}") }.also {
            cache.logs = it
        }
    }

    override suspend fun export(logs: List<String>): Result<String> = withContext(Dispatchers.IO) {
        saveTextFile("crackmydroid-log.txt", logs.joinToString("\n"))
    }
}
