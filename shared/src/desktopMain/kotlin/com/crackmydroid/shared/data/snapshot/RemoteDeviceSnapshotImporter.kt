package com.crackmydroid.shared.data.snapshot

import com.crackmydroid.shared.data.DesktopScanCache
import com.crackmydroid.shared.domain.model.ConnectedDevice
import com.crackmydroid.shared.domain.model.DeviceScanSnapshot
import com.crackmydroid.shared.domain.model.DeviceScanSnapshotContract
import com.crackmydroid.shared.domain.model.DeviceScanSnapshotJson
import com.crackmydroid.shared.domain.model.PlayIntegrityResult
import com.crackmydroid.shared.domain.model.SnapshotMetadata
import com.crackmydroid.shared.domain.model.SnapshotSectionState
import com.crackmydroid.shared.domain.repository.AdbBridge
import com.crackmydroid.shared.util.DesktopPaths
import java.io.File
import kotlinx.serialization.decodeFromString

class RemoteDeviceSnapshotImporter(
    private val adbBridge: AdbBridge,
    private val cache: DesktopScanCache
) {
    suspend fun import(device: ConnectedDevice, adbPath: String?): SnapshotImportOutcome {
        cache.snapshotLoadAttempted = true

        val localDir = File(DesktopPaths.snapshotsDir(), device.serial).apply { mkdirs() }
        val localFile = File(localDir, DeviceScanSnapshotContract.SNAPSHOT_FILE_NAME)

        val pulled = adbBridge.pull(
            serial = device.serial,
            remotePath = DeviceScanSnapshotContract.remotePath(),
            localPath = localFile.absolutePath,
            configuredPath = adbPath
        )

        if (pulled.isFailure) {
            val reason = pulled.exceptionOrNull()?.message.orEmpty()
                .ifBlank { "snapshot remoto non disponibile" }
            return SnapshotImportOutcome(
                usedSnapshot = false,
                message = "Snapshot non disponibile: $reason"
            )
        }

        val snapshot = runCatching {
            DeviceScanSnapshotJson.format.decodeFromString<DeviceScanSnapshot>(
                localFile.readText()
            )
        }.getOrElse { error ->
            return SnapshotImportOutcome(
                usedSnapshot = false,
                message = "Snapshot non valido: ${error.message ?: "JSON corrotto"}"
            )
        }

        if (snapshot.schemaVersion != DeviceScanSnapshot.SCHEMA_VERSION) {
            return SnapshotImportOutcome(
                usedSnapshot = false,
                message = "Snapshot incompatibile: schema ${snapshot.schemaVersion}"
            )
        }

        var appliedSections = 0
        snapshot.deviceInfo.takeIf { it.state == SnapshotSectionState.READY }?.data?.let {
            cache.deviceInfo = it
            appliedSections++
        }
        snapshot.installedApps.takeIf { it.state == SnapshotSectionState.READY }?.data?.let {
            cache.installedApps = it
            appliedSections++
        }
        snapshot.permissions.takeIf { it.state == SnapshotSectionState.READY }?.data?.let {
            cache.permissions = it
            appliedSections++
        }
        snapshot.activities.takeIf { it.state == SnapshotSectionState.READY }?.data?.let {
            cache.activities = it
            appliedSections++
        }
        snapshot.rootStatus.takeIf { it.state == SnapshotSectionState.READY }?.data?.let {
            cache.rootStatus = it
            appliedSections++
        }
        snapshot.logs.takeIf { it.state == SnapshotSectionState.READY }?.data?.let {
            cache.logs = it
            appliedSections++
        }

        val metadata = SnapshotMetadata(
            schemaVersion = snapshot.schemaVersion,
            createdAtEpochMs = snapshot.createdAtEpochMs,
            sourceAppPackage = snapshot.sourceAppPackage,
            sourceAppVersion = snapshot.sourceAppVersion,
            deviceSerial = snapshot.deviceSerial,
            deviceModel = snapshot.deviceModel
        )

        if (appliedSections == 0) {
            cache.playIntegrity = null
            cache.snapshotMetadata = null
            return SnapshotImportOutcome(
                usedSnapshot = false,
                message = "Snapshot remoto presente ma senza sezioni utilizzabili"
            )
        }

        snapshot.playIntegrity.data?.let {
            cache.playIntegrity = it
        } ?: snapshot.playIntegrity.error?.let { detail ->
            cache.playIntegrity = PlayIntegrityResult(
                basicIntegrity = null,
                deviceIntegrity = null,
                details = detail
            )
        }
        cache.snapshotMetadata = metadata

        return SnapshotImportOutcome(
            usedSnapshot = true,
            message = "Snapshot Android importato ($appliedSections sezioni)",
            metadata = metadata
        )
    }
}

data class SnapshotImportOutcome(
    val usedSnapshot: Boolean,
    val message: String,
    val metadata: SnapshotMetadata? = null
)
