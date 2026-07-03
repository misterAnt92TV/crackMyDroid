package com.crackmydroid.shared.data.permissions

import com.crackmydroid.shared.data.DesktopAdbRepositorySupport
import com.crackmydroid.shared.data.DesktopScanCache
import com.crackmydroid.shared.data.adb.AdbParsers
import com.crackmydroid.shared.domain.model.AppPermissionEntry
import com.crackmydroid.shared.domain.repository.AdbBridge
import com.crackmydroid.shared.domain.repository.DeviceSessionController
import com.crackmydroid.shared.domain.repository.PermissionsRepository
import com.crackmydroid.shared.domain.repository.RemotePackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PermissionsRepositoryDesktop(
    adbBridge: AdbBridge,
    sessionController: DeviceSessionController,
    private val cache: DesktopScanCache,
    private val packageManager: RemotePackageManager
) : DesktopAdbRepositorySupport(adbBridge, sessionController), PermissionsRepository {
    override suspend fun listAppPermissions(): List<AppPermissionEntry> = withContext(Dispatchers.IO) {
        cache.permissions?.let { return@withContext it }

        val installedApps = packageManager.listInstalledApps()
        val fresh = installedApps.map { app ->
            val dump = packageManager.readPackageDump(app.packageName)
            AppPermissionEntry(
                appLabel = app.appLabel,
                packageName = app.packageName,
                permissions = AdbParsers.parseRequestedPermissions(dump)
            )
        }.sortedBy { it.appLabel.lowercase() }
        cache.permissions = fresh
        fresh
    }
}
