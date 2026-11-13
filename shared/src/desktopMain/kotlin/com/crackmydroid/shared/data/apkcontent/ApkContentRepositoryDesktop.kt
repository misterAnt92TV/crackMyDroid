package com.crackmydroid.shared.data.apkcontent

import com.crackmydroid.shared.domain.model.apkcontent.ApkArchiveMetadata
import com.crackmydroid.shared.domain.model.apkcontent.ApkContentAnalysis
import com.crackmydroid.shared.domain.model.apkcontent.ApkContentFinding
import com.crackmydroid.shared.domain.model.apkcontent.ApkFindingSeverity
import com.crackmydroid.shared.domain.model.apkcontent.ApkLibrary
import com.crackmydroid.shared.domain.repository.AdbBridge
import com.crackmydroid.shared.domain.repository.ApkContentRepository
import com.crackmydroid.shared.domain.repository.DeviceSessionController
import com.crackmydroid.shared.domain.repository.RemotePackageManager
import com.crackmydroid.shared.util.DesktopPaths
import java.io.File
import java.security.MessageDigest
import java.util.zip.ZipFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ApkContentRepositoryDesktop(
    private val adb: AdbBridge,
    private val session: DeviceSessionController,
    private val packageManager: RemotePackageManager,
) : ApkContentRepository {
    override suspend fun analyze(packageName: String): Result<ApkContentAnalysis> = withContext(Dispatchers.IO) {
        runCatching {
            val device = requireNotNull(session.state.value.selectedDevice) { "No selected device" }
            val remote = packageManager.readPackagePaths(packageName).firstOrNull() ?: error("APK not found")
            val workspace = File(DesktopPaths.exportsDir(), "apk-content-${System.nanoTime()}").apply { mkdirs() }
            try {
                val apk = File(workspace, "$packageName.apk")
                adb.pull(
                    device.serial,
                    remote,
                    apk.absolutePath,
                    session.state.value.adbPath.takeIf {
                        it.isNotBlank()
                    },
                ).getOrThrow()
                val zip = File(workspace, "$packageName.zip")
                require(apk.renameTo(zip)) { "Unable to rename APK copy" }
                ApkArchiveInspector.inspect(packageName, zip)
            } finally {
                workspace.deleteRecursively()
            }
        }
    }
}

internal object ApkArchiveInspector {
    fun inspect(packageName: String, archive: File): ApkContentAnalysis {
        val entries = mutableListOf<String>()
        var expanded = 0L
        ZipFile(archive).use { zip ->
            val iterator = zip.entries()
            while (iterator.hasMoreElements()) {
                val entry = iterator.nextElement()
                require(entries.size < 20_000) { "APK archive has too many entries" }
                require(!entry.name.startsWith('/') && ".." !in entry.name.split('/')) {
                    "Unsafe archive path"
                }
                val size = entry.size.coerceAtLeast(0L)
                val compressed = entry.compressedSize.coerceAtLeast(1L)
                require(size / compressed <= 200L) { "Suspicious compression ratio" }
                expanded += size
                require(expanded <= 1_000_000_000L) { "APK archive exceeds safe size limit" }
                entries += entry.name
            }
        }
        val native = entries.filter {
            it.startsWith("lib/") && it.endsWith(".so")
        }.sorted()
        val certs = entries.filter {
            it.startsWith("META-INF/") &&
                (it.endsWith(".RSA") || it.endsWith(".DSA") || it.endsWith(".EC"))
        }.sorted()
        val libraries = linkedMapOf<String, ApkLibrary>()
        fun library(name: String, evidence: String) {
            libraries.putIfAbsent(name, ApkLibrary(name, evidence))
        }
        entries.forEach { path ->
            when {
                path.contains("firebase", true) -> library("Firebase", path)

                path.contains("okhttp", true) -> library("OkHttp", path)

                path.contains("flutter", true) -> library("Flutter", path)

                path.contains(
                    "react-native",
                    true,
                ) || path.contains("reactnative", true) -> library("React Native", path)

                path.contains("kotlin", true) -> library("Kotlin", path)
            }
        }
        val findings = mutableListOf<ApkContentFinding>()
        fun finding(s: ApkFindingSeverity, c: String, e: String, r: String) {
            findings += ApkContentFinding(s, c, e, r)
        }
        if ("AndroidManifest.xml" !in
            entries
        ) {
            finding(
                ApkFindingSeverity.High,
                "Archive integrity",
                "AndroidManifest.xml is absent",
                "Verify the APK source and signature.",
            )
        }
        if (entries.none {
                it.matches(Regex("classes(\\d+)?\\.dex"))
            }
        ) {
            finding(
                ApkFindingSeverity.High,
                "Archive integrity",
                "No DEX bytecode was found",
                "Verify that the artifact is a complete APK.",
            )
        }
        if (certs.isEmpty()) {
            finding(
                ApkFindingSeverity.Medium,
                "Signing",
                "No legacy META-INF certificate entry was found",
                "Verify APK signing metadata.",
            )
        }
        entries.filter {
            it.endsWith(".jar") ||
                (it.endsWith(".dex") && !it.matches(Regex("classes(\\d+)?\\.dex")))
        }.forEach {
            finding(
                ApkFindingSeverity.Medium,
                "Dynamic payload",
                "Embedded code payload: $it",
                "Review whether this payload is loaded dynamically.",
            )
        }
        native.filter {
            it.contains("exec", true) ||
                it.contains("shell", true) ||
                it.contains("inject", true)
        }.forEach {
            finding(
                ApkFindingSeverity.High,
                "Native code",
                "Potentially sensitive library: $it",
                "Review library origin and runtime behavior.",
            )
        }
        return ApkContentAnalysis(
            packageName,
            ApkArchiveMetadata(
                sha256(archive),
                archive.length(),
                entries.size,
                expanded,
                entries.count {
                    it.matches(Regex("classes(\\d+)?\\.dex"))
                },
                native,
                certs,
            ),
            libraries.values.sortedBy {
                it.name
            },
            findings.sortedByDescending { it.severity.ordinal },
            entries.filter {
                it ==
                    "AndroidManifest.xml" ||
                    it == "resources.arsc" ||
                    it.startsWith("assets/") ||
                    it.startsWith("res/raw/")
            }.sorted(),
        )
    }
    private fun sha256(file: File): String =
        MessageDigest.getInstance("SHA-256").digest(file.readBytes()).joinToString("") { "%02x".format(it) }
}
