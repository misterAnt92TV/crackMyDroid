package com.crackmydroid.shared.data.packages

import com.crackmydroid.shared.data.DesktopAdbRepositorySupport
import com.crackmydroid.shared.data.DesktopScanCache
import com.crackmydroid.shared.data.adb.AdbParsers
import com.crackmydroid.shared.domain.model.InstalledAppEntry
import com.crackmydroid.shared.domain.repository.AdbBridge
import com.crackmydroid.shared.domain.repository.DeviceSessionController
import com.crackmydroid.shared.domain.repository.RemotePackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RemotePackageManagerDesktop(
    adbBridge: AdbBridge,
    sessionController: DeviceSessionController,
    private val cache: DesktopScanCache
) : DesktopAdbRepositorySupport(adbBridge, sessionController), RemotePackageManager {
    override suspend fun listInstalledApps(): List<InstalledAppEntry> = withContext(Dispatchers.IO) {
        cache.installedApps ?: AdbParsers.parseInstalledApps(shell("pm list packages -f")).also {
            cache.installedApps = it
        }
    }

    override suspend fun readPackageDump(packageName: String): String = withContext(Dispatchers.IO) {
        cache.packageDumps[packageName] ?: shell("dumpsys package $packageName").also {
            cache.packageDumps[packageName] = it
        }
    }

    override suspend fun readPackagePaths(packageName: String): List<String> = withContext(Dispatchers.IO) {
        cache.packagePaths[packageName] ?: AdbParsers.parsePmPath(shell("pm path $packageName")).also {
            cache.packagePaths[packageName] = it
        }
    }
}
