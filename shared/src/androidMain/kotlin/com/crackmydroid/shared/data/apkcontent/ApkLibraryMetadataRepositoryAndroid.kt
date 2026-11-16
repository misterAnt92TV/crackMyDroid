package com.crackmydroid.shared.data.apkcontent

import android.content.Context
import com.crackmydroid.shared.domain.model.apkcontent.ApkMavenLibraryDiscovery
import com.crackmydroid.shared.domain.repository.ApkLibraryMetadataRepository
import java.io.File
import java.util.zip.ZipFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ApkLibraryMetadataRepositoryAndroid(private val context: Context) : ApkLibraryMetadataRepository {
    override suspend fun discoverMavenLibraries(packageName: String): Result<ApkMavenLibraryDiscovery> =
        withContext(Dispatchers.IO) {
            runCatching {
                val apk = File(context.packageManager.getApplicationInfo(packageName, 0).sourceDir)
                require(apk.isFile) { "APK source is unavailable" }
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
            }
        }

    private companion object {
        val POM_PROPERTIES_PATH = Regex("META-INF/maven/[^/]+/[^/]+/pom\\.properties")
        val VERSION_PATH = Regex("META-INF/[^/]+\\.version")
    }
}
