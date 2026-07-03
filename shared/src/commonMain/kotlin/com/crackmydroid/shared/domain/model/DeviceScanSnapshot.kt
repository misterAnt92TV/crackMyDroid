package com.crackmydroid.shared.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class DeviceScanSnapshot(
    val schemaVersion: Int = SCHEMA_VERSION,
    val createdAtEpochMs: Long,
    val sourceAppPackage: String,
    val sourceAppVersion: String,
    val deviceSerial: String? = null,
    val deviceModel: String? = null,
    val deviceInfo: SnapshotSection<DeviceInfo>,
    val rootStatus: SnapshotSection<RootStatus>,
    val playIntegrity: SnapshotSection<PlayIntegrityResult>,
    val installedApps: SnapshotSection<List<InstalledAppEntry>>,
    val permissions: SnapshotSection<List<AppPermissionEntry>>,
    val activities: SnapshotSection<List<ActivityEntry>>,
    val logs: SnapshotSection<List<String>>
) {
    companion object {
        const val SCHEMA_VERSION: Int = 1
    }
}

@Serializable
data class SnapshotMetadata(
    val schemaVersion: Int,
    val createdAtEpochMs: Long,
    val sourceAppPackage: String,
    val sourceAppVersion: String,
    val deviceSerial: String? = null,
    val deviceModel: String? = null
)

@Serializable
enum class SnapshotSectionState {
    @SerialName("ready")
    READY,

    @SerialName("failed")
    FAILED,

    @SerialName("skipped")
    SKIPPED
}

@Serializable
data class SnapshotSection<T>(
    val state: SnapshotSectionState,
    val data: T? = null,
    val error: String? = null
) {
    companion object {
        fun <T> ready(data: T): SnapshotSection<T> =
            SnapshotSection(state = SnapshotSectionState.READY, data = data)

        fun <T> failed(error: String): SnapshotSection<T> =
            SnapshotSection(state = SnapshotSectionState.FAILED, error = error)

        fun <T> skipped(error: String, data: T? = null): SnapshotSection<T> =
            SnapshotSection(state = SnapshotSectionState.SKIPPED, data = data, error = error)
    }
}

object DeviceScanSnapshotJson {
    val format: Json = Json {
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = true
    }
}
