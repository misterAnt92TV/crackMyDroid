package com.crackmydroid.shared.domain.model

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeviceScanSnapshotJsonTest {
    @Test
    fun roundTripSnapshotWithReadyFailedAndSkippedSections() {
        val snapshot = DeviceScanSnapshot(
            createdAtEpochMs = 123456789L,
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
                    bootloader = "unknown",
                    batteryLevel = 88,
                    networkType = "Wi-Fi",
                    totalMemory = 8_000L,
                    freeMemory = 4_000L
                )
            ),
            rootStatus = SnapshotSection.ready(
                RootStatus(
                    isRooted = false,
                    details = "Nessuna traccia di root"
                )
            ),
            playIntegrity = SnapshotSection.skipped(
                error = "Non eseguito automaticamente",
                data = PlayIntegrityResult(
                    basicIntegrity = null,
                    deviceIntegrity = null,
                    details = "Non eseguito automaticamente"
                )
            ),
            installedApps = SnapshotSection.ready(
                listOf(InstalledAppEntry("Demo", "pkg.demo", "/data/app/pkg.demo/base.apk"))
            ),
            permissions = SnapshotSection.failed("Permessi non disponibili"),
            activities = SnapshotSection.ready(
                listOf(ActivityEntry("Main", "Demo", "pkg.demo", "pkg.demo.MainActivity"))
            ),
            logs = SnapshotSection.ready(listOf("I/Test: ok"))
        )

        val json = DeviceScanSnapshotJson.format.encodeToString(snapshot)
        val decoded = DeviceScanSnapshotJson.format.decodeFromString<DeviceScanSnapshot>(json)

        assertEquals(DeviceScanSnapshot.SCHEMA_VERSION, decoded.schemaVersion)
        assertEquals("SERIAL1", decoded.deviceSerial)
        assertEquals(SnapshotSectionState.FAILED, decoded.permissions.state)
        assertEquals("Permessi non disponibili", decoded.permissions.error)
        assertEquals(SnapshotSectionState.SKIPPED, decoded.playIntegrity.state)
        assertEquals("I/Test: ok", decoded.logs.data?.first())
    }

    @Test
    fun ignoresUnknownFieldsWhenDecodingSnapshot() {
        val json = """
            {
              "schemaVersion": 1,
              "createdAtEpochMs": 10,
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
              "rootStatus": { "state": "failed", "error": "root unavailable" },
              "playIntegrity": { "state": "skipped", "error": "skipped" },
              "installedApps": { "state": "ready", "data": [] },
              "permissions": { "state": "ready", "data": [] },
              "activities": { "state": "ready", "data": [] },
              "logs": { "state": "ready", "data": [] },
              "unknownField": "ignored"
            }
        """.trimIndent()

        val decoded = DeviceScanSnapshotJson.format.decodeFromString<DeviceScanSnapshot>(json)

        assertEquals(10L, decoded.createdAtEpochMs)
        assertEquals(SnapshotSectionState.FAILED, decoded.rootStatus.state)
        assertTrue(decoded.deviceInfo.data != null)
    }
}
