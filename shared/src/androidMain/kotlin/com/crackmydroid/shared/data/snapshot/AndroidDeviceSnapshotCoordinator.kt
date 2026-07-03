package com.crackmydroid.shared.data.snapshot

import android.content.Context
import com.crackmydroid.database.CacheDatabase
import com.crackmydroid.shared.AppLogger
import com.crackmydroid.shared.domain.model.DeviceScanSnapshot
import com.crackmydroid.shared.domain.model.DeviceScanSnapshotContract
import com.crackmydroid.shared.domain.model.DeviceScanSnapshotJson
import com.crackmydroid.shared.domain.model.PlayIntegrityResult
import com.crackmydroid.shared.domain.model.SnapshotSection
import com.crackmydroid.shared.domain.repository.ActivityRepository
import com.crackmydroid.shared.domain.repository.DeviceInfoRepository
import com.crackmydroid.shared.domain.repository.InstalledAppsRepository
import com.crackmydroid.shared.domain.repository.LogRepository
import com.crackmydroid.shared.domain.repository.PermissionsRepository
import com.crackmydroid.shared.domain.repository.RootCheckRepository
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString

class AndroidDeviceSnapshotCoordinator(
    private val context: Context,
    private val db: CacheDatabase,
    private val activityRepository: ActivityRepository,
    private val deviceInfoRepository: DeviceInfoRepository,
    private val rootCheckRepository: RootCheckRepository,
    private val logRepository: LogRepository,
    private val permissionsRepository: PermissionsRepository,
    private val installedAppsRepository: InstalledAppsRepository
) {
    private val started = AtomicBoolean(false)
    private val running = AtomicBoolean(false)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun start() {
        if (!started.compareAndSet(false, true)) return
        scope.launch {
            runCatching { runSnapshotIfIdle() }
                .onFailure { error ->
                    AppLogger.logger.e(error) { "Snapshot Android non scritto" }
                }
        }
    }

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
        clearRepositoryCaches()

        val deviceInfoSection = captureSection { deviceInfoRepository.getDeviceInfo() }
        val installedAppsSection = captureSection { installedAppsRepository.listInstalled() }
        val permissionsSection = captureSection { permissionsRepository.listAppPermissions() }
        val activitiesSection = captureSection { activityRepository.getActivities() }
        val rootStatusSection = captureSection { rootCheckRepository.checkRoot() }
        val logsSection = captureSection { logRepository.fetchLog().takeLast(MAX_LOG_LINES) }
        val playIntegritySection = SnapshotSection.skipped(
            error = "Play Integrity non eseguito automaticamente in background",
            data = PlayIntegrityResult(
                basicIntegrity = null,
                deviceIntegrity = null,
                details = "Play Integrity non eseguito automaticamente in background"
            )
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
            logs = logsSection
        )

        writeAtomically(snapshot)
        AppLogger.logger.i { "Snapshot Android scritto in ${targetFile().absolutePath}" }
    }

    private fun clearRepositoryCaches() {
        db.cacheDatabaseQueries.transaction {
            db.cacheDatabaseQueries.clearActivities()
            db.cacheDatabaseQueries.clearPermissions()
            db.cacheDatabaseQueries.clearInstalled()
            db.cacheDatabaseQueries.clearDeviceInfo()
        }
    }

    private suspend inline fun <T> captureSection(crossinline loader: suspend () -> T): SnapshotSection<T> =
        try {
            SnapshotSection.ready(loader())
        } catch (error: Throwable) {
            SnapshotSection.failed(error.message ?: "Errore sconosciuto")
        }

    private fun writeAtomically(snapshot: DeviceScanSnapshot) {
        val target = targetFile()
        target.parentFile?.mkdirs()
        val temp = File(target.parentFile, "${target.name}.tmp")
        temp.writeText(DeviceScanSnapshotJson.format.encodeToString(snapshot))
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
            context.getExternalFilesDir(DeviceScanSnapshotContract.SNAPSHOT_DIRECTORY)
        ) {
            "Storage esterno app non disponibile"
        }
        return File(baseDir, DeviceScanSnapshotContract.SNAPSHOT_FILE_NAME)
    }

    private fun appVersionName(): String =
        runCatching {
            val info = context.packageManager.getPackageInfo(context.packageName, 0)
            info.versionName ?: "n/d"
        }.getOrDefault("n/d")

    private companion object {
        const val MAX_LOG_LINES: Int = 1500
    }
}
