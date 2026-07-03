package com.crackmydroid.shared.data.snapshot

import com.crackmydroid.shared.data.DesktopScanCache
import com.crackmydroid.shared.domain.model.ActivityEntry
import com.crackmydroid.shared.domain.model.AppPermissionEntry
import com.crackmydroid.shared.domain.model.ConnectedDevice
import com.crackmydroid.shared.domain.model.DeviceInfo
import com.crackmydroid.shared.domain.model.DeviceScanSnapshot
import com.crackmydroid.shared.domain.model.DeviceScanSnapshotJson
import com.crackmydroid.shared.domain.model.InstalledAppEntry
import com.crackmydroid.shared.domain.model.PlayIntegrityResult
import com.crackmydroid.shared.domain.model.RootStatus
import com.crackmydroid.shared.domain.model.SnapshotSection
import com.crackmydroid.shared.domain.repository.AdbBridge
import java.io.File
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RemoteDeviceSnapshotImporterTest {
    @Test
    fun importsValidSnapshotIntoCache() = runTest {
        val snapshot = DeviceScanSnapshot(
            createdAtEpochMs = 111L,
            sourceAppPackage = "com.crackmydroid.android",
            sourceAppVersion = "1.2",
            deviceSerial = "SERIAL1",
            deviceModel = "Pixel 8",
            deviceInfo = SnapshotSection.ready(
                DeviceInfo(
                    manufacturer = "Google",
                    model = "Pixel 8",
                    brand = "google",
                    device = "husky",
                    product = "husky",
                    androidVersion = "14",
                    securityPatch = "2026-06-05",
                    hardware = "tensor",
                    bootloader = "n/a",
                    batteryLevel = 80,
                    networkType = "Wi-Fi",
                    totalMemory = 8_000L,
                    freeMemory = 4_000L
                )
            ),
            rootStatus = SnapshotSection.ready(RootStatus(false, "No root")),
            playIntegrity = SnapshotSection.skipped(
                error = "Non eseguito automaticamente",
                data = PlayIntegrityResult(null, null, "Non eseguito automaticamente")
            ),
            installedApps = SnapshotSection.ready(
                listOf(InstalledAppEntry("Demo", "pkg.demo", "/data/app/pkg.demo/base.apk"))
            ),
            permissions = SnapshotSection.ready(
                listOf(AppPermissionEntry("Demo", "pkg.demo", listOf("android.permission.INTERNET")))
            ),
            activities = SnapshotSection.ready(
                listOf(ActivityEntry("Main", "Demo", "pkg.demo", "pkg.demo.MainActivity"))
            ),
            logs = SnapshotSection.ready(listOf("I/Test: ok"))
        )
        val cache = DesktopScanCache()
        val importer = RemoteDeviceSnapshotImporter(
            adbBridge = FakeSnapshotAdbBridge(
                snapshotText = DeviceScanSnapshotJson.format.encodeToString(snapshot)
            ),
            cache = cache
        )

        val outcome = importer.import(
            device = ConnectedDevice(serial = "SERIAL1", state = "device"),
            adbPath = "/usr/bin/adb"
        )

        assertTrue(outcome.usedSnapshot)
        val metadata = assertNotNull(outcome.metadata)
        assertEquals("1.2", metadata.sourceAppVersion)
        assertNotNull(cache.deviceInfo)
        assertEquals("pkg.demo", cache.installedApps?.firstOrNull()?.packageName)
        assertEquals("No root", cache.rootStatus?.details)
        assertEquals("I/Test: ok", cache.logs?.firstOrNull())
        assertEquals("Non eseguito automaticamente", cache.playIntegrity?.details)
    }

    @Test
    fun fallsBackWhenSnapshotSchemaIsIncompatible() = runTest {
        val cache = DesktopScanCache()
        val importer = RemoteDeviceSnapshotImporter(
            adbBridge = FakeSnapshotAdbBridge(
                snapshotText = """
                    {
                      "schemaVersion": 999,
                      "createdAtEpochMs": 1,
                      "sourceAppPackage": "com.crackmydroid.android",
                      "sourceAppVersion": "1.2",
                      "deviceInfo": { "state": "ready", "data": {
                        "manufacturer": "Google",
                        "model": "Pixel",
                        "androidVersion": "14",
                        "securityPatch": "2026-06-05",
                        "hardware": "tensor",
                        "bootloader": "n/a",
                        "batteryLevel": null,
                        "networkType": "Wi-Fi",
                        "totalMemory": null,
                        "freeMemory": null
                      }},
                      "rootStatus": { "state": "failed", "error": "root" },
                      "playIntegrity": { "state": "skipped", "error": "skip" },
                      "installedApps": { "state": "ready", "data": [] },
                      "permissions": { "state": "ready", "data": [] },
                      "activities": { "state": "ready", "data": [] },
                      "logs": { "state": "ready", "data": [] }
                    }
                """.trimIndent()
            ),
            cache = cache
        )

        val outcome = importer.import(
            device = ConnectedDevice(serial = "SERIAL1", state = "device"),
            adbPath = "/usr/bin/adb"
        )

        assertFalse(outcome.usedSnapshot)
        assertTrue(outcome.message.contains("schema"))
        assertEquals(null, cache.deviceInfo)
    }
}

private class FakeSnapshotAdbBridge(
    private val snapshotText: String? = null
) : AdbBridge {
    override suspend fun resolveBinary(configuredPath: String?): Result<String> =
        Result.success(configuredPath ?: "/usr/bin/adb")

    override suspend fun listDevices(configuredPath: String?): Result<List<ConnectedDevice>> =
        Result.success(emptyList())

    override suspend fun shell(
        serial: String,
        command: String,
        configuredPath: String?,
        asRoot: Boolean
    ): Result<String> = Result.success("")

    override suspend fun pull(
        serial: String,
        remotePath: String,
        localPath: String,
        configuredPath: String?
    ): Result<String> = runCatching {
        requireNotNull(snapshotText) { "missing remote snapshot" }
        val file = File(localPath)
        file.parentFile?.mkdirs()
        file.writeText(snapshotText)
        localPath
    }

    override suspend fun logcat(
        serial: String,
        configuredPath: String?,
        lines: Int
    ): Result<List<String>> = Result.success(emptyList())
}
