package com.crackmydroid.shared.data.apps

import com.crackmydroid.shared.data.DesktopAdbRepositorySupport
import com.crackmydroid.shared.data.DesktopScanCache
import com.crackmydroid.shared.data.adb.AdbParsers
import com.crackmydroid.shared.domain.model.InstalledAppEntry
import com.crackmydroid.shared.domain.model.ManifestSecurityReport
import com.crackmydroid.shared.domain.model.ManifestSecurityAnalyzer
import com.crackmydroid.shared.domain.repository.AdbBridge
import com.crackmydroid.shared.domain.repository.DeviceSessionController
import com.crackmydroid.shared.domain.repository.InstalledAppsRepository
import com.crackmydroid.shared.domain.repository.RemotePackageManager
import com.crackmydroid.shared.util.DesktopPaths
import com.crackmydroid.shared.util.revealInDesktop
import com.crackmydroid.shared.data.cache.PaginationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class InstalledAppsRepositoryDesktop(
    adbBridge: AdbBridge,
    sessionController: DeviceSessionController,
    private val packageManager: RemotePackageManager,
    private val cache: DesktopScanCache
) : DesktopAdbRepositorySupport(adbBridge, sessionController), InstalledAppsRepository {
    override suspend fun listInstalled(): List<InstalledAppEntry> =
        withContext(Dispatchers.IO) { packageManager.listInstalledApps() }

    override suspend fun listInstalledPaginated(offset: Int, limit: Int): List<InstalledAppEntry> =
        withContext(Dispatchers.IO) {
            val all = listInstalled()
            PaginationHelper.paginate(all, offset, limit)
        }

    override suspend fun analyzeManifestSecurity(): List<ManifestSecurityReport> = withContext(Dispatchers.IO) {
        runCatching {
            val installedApps = packageManager.listInstalledApps()
            installedApps.mapNotNull { app ->
                val dump = packageManager.readPackageDump(app.packageName)
                if (dump.isNotBlank()) {
                    val security = AdbParsers.parseManifestSecurity(app.packageName, app.appLabel, dump)
                    ManifestSecurityAnalyzer.analyze(security)
                } else null
            }
        }.getOrNull() ?: emptyList()
    }

    override suspend fun analyzeManifestSecurityCached(): List<ManifestSecurityReport> =
        withContext(Dispatchers.IO) {
            cache.manifestSecurityReports ?: analyzeManifestSecurity().also {
                cache.manifestSecurityReports = it
            }
        }

    override suspend fun exportApk(packageName: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val device = selectedDevice()
            val paths = packageManager.readPackagePaths(packageName)
            val remotePath = paths.firstOrNull() ?: error("APK non trovato per $packageName")
            val localDir = File(DesktopPaths.exportsDir(), device.serial).apply { mkdirs() }
            val localPath = File(localDir, "$packageName.apk").absolutePath
            adbBridge.pull(
                serial = device.serial,
                remotePath = remotePath,
                localPath = localPath,
                configuredPath = adbPath()
            ).getOrThrow()
        }
    }

    override suspend fun shareApk(packageName: String, bluetoothOnly: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val path = exportApk(packageName).getOrThrow()
            revealInDesktop(File(path))
        }
    }
}

