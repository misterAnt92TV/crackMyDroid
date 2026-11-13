package com.crackmydroid.shared.data.snapshot

import android.content.Context
import com.crackmydroid.architecture.logging.AppLogger
import com.crackmydroid.shared.domain.model.PlayIntegrityResult
import com.crackmydroid.shared.domain.model.scansnapshot.DeviceScanSnapshot
import com.crackmydroid.shared.domain.model.scansnapshot.DeviceScanSnapshotContract
import com.crackmydroid.shared.domain.model.scansnapshot.DeviceScanSnapshotJson
import com.crackmydroid.shared.domain.model.scansnapshot.SnapshotSection
import com.crackmydroid.shared.domain.repository.ActivityRepository
import com.crackmydroid.shared.domain.repository.DeviceInfoRepository
import com.crackmydroid.shared.domain.repository.InstalledAppsRepository
import com.crackmydroid.shared.domain.repository.PermissionsRepository
import com.crackmydroid.shared.domain.repository.RootCheckRepository
import com.crackmydroid.shared.domain.repository.log.LogRepository
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.encodeToStream

class AndroidDeviceSnapshotCoordinator(
    private val context: Context,
    private val activityRepository: ActivityRepository,
    private val deviceInfoRepository: DeviceInfoRepository,
    private val rootCheckRepository: RootCheckRepository,
    private val logRepository: LogRepository,
    private val permissionsRepository: PermissionsRepository,
    private val installedAppsRepository: InstalledAppsRepository,
) {
    private val running = AtomicBoolean(false)

    suspend fun runSnapshotIfIdle(): Boolean {
        if (!running.compareAndSet(false, true)) {
            AppLogger.logger.i { "Snapshot Android gia in esecuzione, skip nuovo trigger" }
            return false
        }
        return try {
            writeSnapshot()
            true
        } finally {
            running.set(false)
        }
    }

    private suspend fun writeSnapshot() {
        val deviceInfoSection = captureSection { deviceInfoRepository.getDeviceInfo() }
        val installedAppsSection = captureSection { installedAppsRepository.listInstalled() }
        val permissionsSection = captureSection { permissionsRepository.listAppPermissions() }
        val activitiesSection = captureSection { activityRepository.getActivities() }
        val manifestSecuritySection =
            captureSection { installedAppsRepository.analyzeManifestSecurity() }
        val rootStatusSection = captureSection { rootCheckRepository.checkRoot() }
        val logsSection = captureSection { logRepository.fetchLog().takeLast(MAX_LOG_LINES) }
        val playIntegritySection = SnapshotSection.skipped(
            error = "Play Integrity non eseguito automaticamente in background",
            data = PlayIntegrityResult(
                basicIntegrity = null,
                deviceIntegrity = null,
                details = "Play Integrity non eseguito automaticamente in background",
            ),
        )

        val deviceInfo = deviceInfoSection.data
        val snapshot = DeviceScanSnapshot(
            createdAtEpochMs = System.currentTimeMillis(),
            sourceAppPackage = context.packageName,
            sourceAppVersion = appVersionName(),
            deviceSerial = deviceInfo?.serial,
            deviceModel = deviceInfo?.model,
            deviceInfo = deviceInfoSection,
            rootStatus = rootStatusSection,
            playIntegrity = playIntegritySection,
            installedApps = installedAppsSection,
            permissions = permissionsSection,
            activities = activitiesSection,
            logs = logsSection,
            manifestSecurityReports = manifestSecuritySection,
        )

        writeAtomically(snapshot)
        AppLogger.logger.i { "Snapshot Android scritto in ${targetFile().absolutePath}" }
    }

    private suspend inline fun <T> captureSection(crossinline loader: suspend () -> T): SnapshotSection<T> = try {
        SnapshotSection.ready(loader())
    } catch (error: Throwable) {
        SnapshotSection.failed(error.message ?: "Errore sconosciuto")
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun writeAtomically(snapshot: DeviceScanSnapshot) {
        val target = targetFile()
        target.parentFile?.mkdirs()
        val temp = File(target.parentFile, "${target.name}.tmp")
        temp.outputStream().buffered().use { outputStream ->
            DeviceScanSnapshotJson.format.encodeToStream(snapshot, outputStream)
        }
        if (target.exists() && !target.delete()) {
            error("Impossibile sostituire snapshot esistente")
        }
        if (!temp.renameTo(target)) {
            temp.copyTo(target, overwrite = true)
            temp.delete()
        }
    }

    private fun targetFile(): File {
        val baseDir = requireNotNull(
            context.getExternalFilesDir(DeviceScanSnapshotContract.SNAPSHOT_DIRECTORY),
        ) {
            "Storage esterno app non disponibile"
        }
        return File(baseDir, DeviceScanSnapshotContract.SNAPSHOT_FILE_NAME)
    }

    private fun appVersionName(): String = runCatching {
        val info = context.packageManager.getPackageInfo(context.packageName, 0)
        info.versionName ?: "n/d"
    }.getOrDefault("n/d")

    private companion object {
        const val MAX_LOG_LINES: Int = 1500
    }
}
