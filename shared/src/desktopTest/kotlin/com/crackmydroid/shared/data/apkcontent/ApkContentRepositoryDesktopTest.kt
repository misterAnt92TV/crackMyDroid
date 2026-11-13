package com.crackmydroid.shared.data.apkcontent

import com.crackmydroid.shared.domain.model.ConnectedDevice
import com.crackmydroid.shared.domain.model.DeviceSessionState
import com.crackmydroid.shared.domain.model.InstalledAppEntry
import com.crackmydroid.shared.domain.model.apkcontent.ApkFindingSeverity
import com.crackmydroid.shared.domain.repository.AdbBridge
import com.crackmydroid.shared.domain.repository.DeviceSessionController
import com.crackmydroid.shared.domain.repository.RemotePackageManager
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest

class ApkContentRepositoryDesktopTest {

    private fun createZip(vararg entries: Pair<String, ByteArray>): File {
        val tempFile = File.createTempFile("test_apk_", ".zip").apply { deleteOnExit() }
        ZipOutputStream(FileOutputStream(tempFile)).use { zos ->
            for ((name, content) in entries) {
                zos.putNextEntry(ZipEntry(name))
                zos.write(content)
                zos.closeEntry()
            }
        }
        return tempFile
    }

    @Test
    fun inspector_analyzesValidApkArchive() {
        val zip = createZip(
            "AndroidManifest.xml" to "<manifest/>".encodeToByteArray(),
            "classes.dex" to "dex_bytes".encodeToByteArray(),
            "classes2.dex" to "dex_bytes_2".encodeToByteArray(),
            "META-INF/CERT.RSA" to "cert_bytes".encodeToByteArray(),
            "okhttp3/internal/publicsuffix.gz" to "okhttp_evidence".encodeToByteArray(),
            "com/google/firebase/analytics/FirebaseAnalytics.class" to "fb".encodeToByteArray(),
            "kotlin/Unit.class" to "kt".encodeToByteArray(),
            "res/raw/config.json" to "{}".encodeToByteArray(),
            "lib/arm64-v8a/libapp.so" to "elf".encodeToByteArray(),
        )

        try {
            val analysis = ApkArchiveInspector.inspect("com.example.test", zip)

            assertEquals("com.example.test", analysis.packageName)
            assertEquals(9, analysis.metadata.entryCount)
            assertEquals(2, analysis.metadata.dexCount)
            assertEquals(listOf("lib/arm64-v8a/libapp.so"), analysis.metadata.nativeLibraries)
            assertEquals(listOf("META-INF/CERT.RSA"), analysis.metadata.certificateEntries)

            val libraryNames = analysis.libraries.map { it.name }.toSet()
            assertTrue("Firebase" in libraryNames)
            assertTrue("OkHttp" in libraryNames)
            assertTrue("Kotlin" in libraryNames)

            // Valid APK with manifest and DEX should not have integrity findings
            assertTrue(analysis.findings.none { it.category == "Archive integrity" })
            assertTrue(analysis.findings.none { it.category == "Signing" })
        } finally {
            zip.delete()
        }
    }

    @Test
    fun inspector_flagsMissingManifestAndDexAsHighSeverity() {
        val zip = createZip(
            "assets/data.txt" to "dummy".encodeToByteArray(),
        )

        try {
            val analysis = ApkArchiveInspector.inspect("com.corrupt.app", zip)

            val missingManifest = analysis.findings.find { it.evidence.contains("AndroidManifest.xml is absent") }
            val missingDex = analysis.findings.find { it.evidence.contains("No DEX bytecode was found") }
            val missingCert = analysis.findings.find { it.category == "Signing" }

            assertEquals(ApkFindingSeverity.High, missingManifest?.severity)
            assertEquals(ApkFindingSeverity.High, missingDex?.severity)
            assertEquals(ApkFindingSeverity.Medium, missingCert?.severity)
        } finally {
            zip.delete()
        }
    }

    @Test
    fun inspector_flagsEmbeddedDynamicPayloadsAndSensitiveNativeLibs() {
        val zip = createZip(
            "AndroidManifest.xml" to "<manifest/>".encodeToByteArray(),
            "classes.dex" to "dex".encodeToByteArray(),
            "META-INF/CERT.RSA" to "cert".encodeToByteArray(),
            "assets/payload.jar" to "embedded_jar".encodeToByteArray(),
            "assets/dynamic.dex" to "embedded_dex".encodeToByteArray(),
            "lib/arm64-v8a/libinject.so" to "hook_code".encodeToByteArray(),
            "lib/x86/libexec.so" to "shell_code".encodeToByteArray(),
        )

        try {
            val analysis = ApkArchiveInspector.inspect("com.suspicious.app", zip)

            val dynamicFindings = analysis.findings.filter { it.category == "Dynamic payload" }
            assertEquals(2, dynamicFindings.size)
            assertTrue(dynamicFindings.all { it.severity == ApkFindingSeverity.Medium })

            val nativeFindings = analysis.findings.filter { it.category == "Native code" }
            assertEquals(2, nativeFindings.size)
            assertTrue(nativeFindings.all { it.severity == ApkFindingSeverity.High })
        } finally {
            zip.delete()
        }
    }

    @Test
    fun inspector_rejectsUnsafePathTraversal() {
        val zip = createZip(
            "assets/../secret.txt" to "bad".encodeToByteArray(),
        )

        try {
            assertFailsWith<IllegalArgumentException> {
                ApkArchiveInspector.inspect("com.traversal.app", zip)
            }
        } finally {
            zip.delete()
        }
    }

    @Test
    fun inspector_extractsPlaintextEndpointsAndConfigs() {
        val zip = createZip(
            "AndroidManifest.xml" to
                """<manifest xmlns:android="http://schemas.android.com/apk/res/android"/>""".encodeToByteArray(),
            "classes.dex" to "some bytes https://api.crackmydroid.com/v1/auth more bytes".encodeToByteArray(),
            "assets/app_config.json" to
                """{"BASE_URL": "https://gateway.example.com", "APP_API_KEY": "secret_abc123"}""".encodeToByteArray(),
            "res/raw/server.properties" to
                "REMOTE_HOST=staging.example.net\nSERVER_TOKEN=xyz987token".encodeToByteArray(),
        )

        try {
            val analysis = ApkArchiveInspector.inspect("com.plaintext.test", zip)

            assertTrue(analysis.plaintextContent.totalCount >= 2)
            val endpoints = analysis.plaintextContent.endpoints.map { it.value }
            assertTrue(endpoints.any { it.contains("crackmydroid.com") || it.contains("gateway.example.com") })

            val configs = analysis.plaintextContent.configKeys.map { it.value }
            assertTrue(
                configs.any {
                    it.contains("APP_API_KEY") || it.contains("BASE_URL") || it.contains("REMOTE_HOST") ||
                        it.contains("SERVER_TOKEN")
                },
            )
        } finally {
            zip.delete()
        }
    }

    @Test
    fun repository_pullsApkAndAnalyzesSuccessfully() = runTest {
        val fakeDevice = ConnectedDevice("emulator-5554", "device", model = "Pixel 8")
        val sessionState = MutableStateFlow(
            DeviceSessionState(
                availableDevices = listOf(fakeDevice),
                selectedDevice = fakeDevice,
                adbPath = "/fake/adb",
            ),
        )

        val fakeSession = object : DeviceSessionController {
            override val state: StateFlow<DeviceSessionState> = sessionState
            override suspend fun refreshDevices(adbPathOverride: String?): Result<List<ConnectedDevice>> =
                Result.success(listOf(fakeDevice))
            override suspend fun selectDevice(device: ConnectedDevice, adbPathOverride: String?): Result<Unit> =
                Result.success(Unit)
            override suspend fun clearSelection() {}
            override suspend fun rescanSelectedDevice(): Result<Unit> = Result.success(Unit)
        }

        val fakePkgManager = object : RemotePackageManager {
            override suspend fun listInstalledApps(): List<InstalledAppEntry> = emptyList()
            override suspend fun readPackageDump(packageName: String): String = ""
            override suspend fun readPackagePaths(packageName: String): List<String> =
                listOf("/data/app/~~test/com.example.app-base.apk")
        }

        val sampleZip = createZip(
            "AndroidManifest.xml" to "<manifest/>".encodeToByteArray(),
            "classes.dex" to "dex_code".encodeToByteArray(),
            "META-INF/CERT.RSA" to "cert".encodeToByteArray(),
        )

        val fakeAdb = object : AdbBridge {
            override suspend fun resolveBinary(configuredPath: String?): Result<String> = Result.success("/fake/adb")
            override suspend fun listDevices(configuredPath: String?): Result<List<ConnectedDevice>> =
                Result.success(listOf(fakeDevice))
            override suspend fun shell(
                serial: String,
                command: String,
                configuredPath: String?,
                asRoot: Boolean,
            ): Result<String> = Result.success("")
            override suspend fun pull(
                serial: String,
                remotePath: String,
                localPath: String,
                configuredPath: String?,
            ): Result<String> {
                sampleZip.copyTo(File(localPath), overwrite = true)
                return Result.success("pulled")
            }
            override suspend fun logcat(serial: String, configuredPath: String?, lines: Int): Result<List<String>> =
                Result.success(emptyList())
        }

        try {
            val repository = ApkContentRepositoryDesktop(fakeAdb, fakeSession, fakePkgManager)
            val result = repository.analyze("com.example.app")

            assertTrue(result.isSuccess)
            val analysis = result.getOrThrow()
            assertEquals("com.example.app", analysis.packageName)
            assertEquals(3, analysis.metadata.entryCount)
            assertEquals(1, analysis.metadata.dexCount)
        } finally {
            sampleZip.delete()
        }
    }
}
