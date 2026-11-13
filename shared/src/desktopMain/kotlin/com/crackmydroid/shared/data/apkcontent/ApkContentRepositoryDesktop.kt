package com.crackmydroid.shared.data.apkcontent

import com.crackmydroid.shared.domain.model.apkcontent.ApkArchiveMetadata
import com.crackmydroid.shared.domain.model.apkcontent.ApkContentAnalysis
import com.crackmydroid.shared.domain.model.apkcontent.ApkContentFinding
import com.crackmydroid.shared.domain.model.apkcontent.ApkFindingSeverity
import com.crackmydroid.shared.domain.model.apkcontent.ApkLibrary
import com.crackmydroid.shared.domain.model.apkcontent.ApkPlainTextCategory
import com.crackmydroid.shared.domain.model.apkcontent.ApkPlainTextItem
import com.crackmydroid.shared.domain.model.apkcontent.ApkPlaintextContent
import com.crackmydroid.shared.domain.repository.AdbBridge
import com.crackmydroid.shared.domain.repository.ApkContentRepository
import com.crackmydroid.shared.domain.repository.DeviceSessionController
import com.crackmydroid.shared.domain.repository.RemotePackageManager
import com.crackmydroid.shared.util.DesktopPaths
import com.crackmydroid.shared.util.revealInDesktop
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
                val temporaryZip = File(workspace, "$packageName.zip")
                require(apk.renameTo(temporaryZip)) { "Unable to rename APK copy" }
                ApkArchiveInspector.inspect(packageName, temporaryZip)
            } finally {
                workspace.deleteRecursively()
            }
        }
    }

    override suspend fun prepareAndOpenArchive(packageName: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val device = requireNotNull(session.state.value.selectedDevice) { "No selected device" }
            val remote = packageManager.readPackagePaths(packageName).firstOrNull() ?: error("APK not found")
            val workspace = File(DesktopPaths.exportsDir(), "apk-navigation-${System.nanoTime()}").apply { mkdirs() }
            val copiedApk = File(workspace, "$packageName.apk")
            adb.pull(
                device.serial,
                remote,
                copiedApk.absolutePath,
                session.state.value.adbPath.takeIf { it.isNotBlank() },
            ).getOrThrow()
            val zip = File(workspace, "$packageName.zip")
            require(copiedApk.renameTo(zip)) { "Unable to rename APK copy" }
            revealInDesktop(zip)
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
        val plainItems = extractPlaintextContent(archive, entries)

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
            plaintextContent = ApkPlaintextContent(plainItems),
        )
    }

    private fun extractPlaintextContent(archive: File, entries: List<String>): List<ApkPlainTextItem> {
        val results = mutableListOf<ApkPlainTextItem>()
        val seen = mutableSetOf<String>()

        fun addItem(category: ApkPlainTextCategory, value: String, source: String) {
            val trimmed = value.trim()
            if (trimmed.length in 4..250 && seen.add("${category.name}:$trimmed")) {
                results += ApkPlainTextItem(category, trimmed, source)
            }
        }

        val urlRegex = Regex("""https?://[a-zA-Z0-9.-]+(?::[0-9]+)?(?:/[^\s"']*)?""", RegexOption.IGNORE_CASE)
        val configKeyRegex =
            Regex(
                """\b([A-Za-z0-9_]{3,40}(?:API_KEY|SECRET|TOKEN|CLIENT_ID|APP_ID|BASE_URL|HOST))""" +
                    """\s*[:=]\s*([^\s"';,]{3,100})\b""",
                RegexOption.IGNORE_CASE,
            )
        val plainTextRegex = Regex("""[a-zA-Z0-9_. -]{6,100}""")

        val targets = entries.filter { path ->
            path == "AndroidManifest.xml" ||
                path.startsWith("assets/") ||
                path.startsWith("res/raw/") ||
                path.endsWith(".json") ||
                path.endsWith(".properties") ||
                path.endsWith(".xml") ||
                path.matches(Regex("classes(\\d+)?\\.dex"))
        }.take(30)

        ZipFile(archive).use { zip ->
            for (path in targets) {
                if (results.size >= 120) break
                val entry = zip.getEntry(path) ?: continue
                val maxRead = if (path.endsWith(".dex")) 64 * 1024 else 32 * 1024
                val bytes = zip.getInputStream(entry).use { it.readNBytes(maxRead) }
                val content = String(bytes, Charsets.ISO_8859_1)

                // 1. URLs
                urlRegex.findAll(content).forEach { match ->
                    if (results.size < 120) {
                        addItem(ApkPlainTextCategory.Endpoint, match.value, path)
                    }
                }

                // 2. Config & Keys
                configKeyRegex.findAll(content).forEach { match ->
                    if (results.size < 120) {
                        val key = match.groupValues[1]
                        val value = match.groupValues[2]
                        addItem(ApkPlainTextCategory.ConfigurationKey, "$key = $value", path)
                    }
                }

                // 3. Plaintext readable strings (e.g., config / properties / assets)
                if (path.endsWith(".json") || path.endsWith(".properties") || path.startsWith("assets/")) {
                    plainTextRegex.findAll(content).forEach { match ->
                        val text = match.value.trim()
                        if (results.size < 120 && text.isNotBlank() && !text.startsWith("http", ignoreCase = true)) {
                            addItem(ApkPlainTextCategory.TextString, text, path)
                        }
                    }
                }
            }
        }

        return results
    }
    private fun sha256(file: File): String =
        MessageDigest.getInstance("SHA-256").digest(file.readBytes()).joinToString("") { "%02x".format(it) }
}
