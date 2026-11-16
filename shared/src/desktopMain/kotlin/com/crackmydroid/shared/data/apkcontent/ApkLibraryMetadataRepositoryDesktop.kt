package com.crackmydroid.shared.data.apkcontent

import com.crackmydroid.shared.domain.model.apkcontent.ApkMavenLibraryDiscovery
import com.crackmydroid.shared.domain.repository.AdbBridge
import com.crackmydroid.shared.domain.repository.ApkLibraryMetadataRepository
import com.crackmydroid.shared.domain.repository.DeviceSessionController
import com.crackmydroid.shared.domain.repository.RemotePackageManager
import com.crackmydroid.shared.util.DesktopPaths
import java.io.File
import java.util.zip.ZipFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ApkLibraryMetadataRepositoryDesktop(
    private val adb: AdbBridge,
    private val session: DeviceSessionController,
    private val packageManager: RemotePackageManager,
) : ApkLibraryMetadataRepository {
    override suspend fun discoverMavenLibraries(packageName: String): Result<ApkMavenLibraryDiscovery> =
        withContext(Dispatchers.IO) {
            runCatching {
                val device = requireNotNull(session.state.value.selectedDevice) { "No selected device" }
                val remotePath = packageManager.readPackagePaths(packageName).firstOrNull() ?: error("APK not found")
                val workspace = File(DesktopPaths.exportsDir(), "apk-metadata-${System.nanoTime()}").apply { mkdirs() }
                try {
                    val apk = File(workspace, "$packageName.apk")
                    adb.pull(device.serial, remotePath, apk.absolutePath, session.state.value.adbPath.takeIf { it.isNotBlank() })
                        .getOrThrow()
                    ZipFile(apk).use { archive ->
                        val entries = archive.entries().asSequence().toList()
                        val pomProperties = entries.filter { it.name.matches(POM_PROPERTIES_PATH) }
                            .map { archive.getInputStream(it).bufferedReader().use { reader -> reader.readText() } }
                        val versions = entries.filter { it.name.matches(VERSION_PATH) }
                            .map { entry ->
                                ApkLibraryMetadataParser.VersionEntry(
                                    entry.name,
                                    archive.getInputStream(entry).bufferedReader().use { reader -> reader.readText() },
                                )
                            }
                        ApkLibraryMetadataParser.discover(packageName, pomProperties, versions)
                    }
                } finally {
                    workspace.deleteRecursively()
                }
            }
        }

    private companion object {
        val POM_PROPERTIES_PATH = Regex("META-INF/maven/[^/]+/[^/]+/pom\\.properties")
        val VERSION_PATH = Regex("META-INF/[^/]+\\.version")
    }
}
