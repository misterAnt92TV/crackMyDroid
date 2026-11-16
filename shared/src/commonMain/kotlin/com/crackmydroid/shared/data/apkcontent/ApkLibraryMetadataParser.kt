package com.crackmydroid.shared.data.apkcontent

import com.crackmydroid.shared.domain.model.apkcontent.ApkMavenLibrary
import com.crackmydroid.shared.domain.model.apkcontent.ApkMavenLibraryDiscovery
import com.crackmydroid.shared.domain.model.apkcontent.MavenLibrarySource

/**
 * Converts archive metadata into Maven coordinates without guessing from code or library names.
 */
internal object ApkLibraryMetadataParser {
    fun discover(
        packageName: String,
        pomProperties: List<String>,
        versionEntries: List<VersionEntry>,
    ): ApkMavenLibraryDiscovery {
        val libraries = linkedMapOf<String, ApkMavenLibrary>()
        pomProperties.mapNotNull(::fromPomProperties).forEach { library ->
            libraries.putIfAbsent(library.packageUrl, library)
        }
        versionEntries.mapNotNull(::fromVersionEntry).forEach { library ->
            libraries.putIfAbsent(library.packageUrl, library)
        }
        return ApkMavenLibraryDiscovery(packageName, libraries.values.sortedBy { it.displayName })
    }

    private fun fromPomProperties(content: String): ApkMavenLibrary? {
        val values = content.lineSequence()
            .map(String::trim)
            .filter { it.isNotEmpty() && !it.startsWith('#') && !it.startsWith('!') }
            .mapNotNull { line -> line.substringBefore('=').trim().takeIf { '=' in line }?.let { it to line.substringAfter('=').trim() } }
            .toMap()
        return coordinate(
            groupId = values["groupId"],
            artifactId = values["artifactId"],
            version = values["version"],
            source = MavenLibrarySource.PomProperties,
        )
    }

    private fun fromVersionEntry(entry: VersionEntry): ApkMavenLibrary? {
        val name = entry.path.removePrefix(META_INF_PREFIX).removeSuffix(VERSION_SUFFIX)
        val separator = name.lastIndexOf('_')
        if (separator <= 0 || separator == name.lastIndex) return null
        val groupId = name.substring(0, separator)
        val artifactId = name.substring(separator + 1)
        if (trustedGroupPrefixes.none(groupId::startsWith)) return null
        return coordinate(groupId, artifactId, entry.content, MavenLibrarySource.VersionMetadata)
    }

    private fun coordinate(
        groupId: String?,
        artifactId: String?,
        version: String?,
        source: MavenLibrarySource,
    ): ApkMavenLibrary? {
        val normalizedGroup = groupId?.trim()?.takeIf(groupPattern::matches) ?: return null
        val normalizedArtifact = artifactId?.trim()?.takeIf(artifactPattern::matches) ?: return null
        val normalizedVersion = version?.trim()?.takeIf(versionPattern::matches) ?: return null
        return ApkMavenLibrary(
            displayName = "$normalizedGroup:$normalizedArtifact",
            packageUrl = "pkg:maven/$normalizedGroup/$normalizedArtifact@$normalizedVersion",
            version = normalizedVersion,
            source = source,
        )
    }

    data class VersionEntry(val path: String, val content: String)

    private const val META_INF_PREFIX = "META-INF/"
    private const val VERSION_SUFFIX = ".version"
    private val trustedGroupPrefixes = listOf(
        "androidx.",
        "com.google.android.material",
        "com.google.android.play",
        "com.google.firebase",
    )
    private val groupPattern = Regex("[A-Za-z0-9]+(?:[.-][A-Za-z0-9]+)+")
    private val artifactPattern = Regex("[A-Za-z0-9][A-Za-z0-9._-]*")
    private val versionPattern = Regex("[^\\s@]{1,128}")
}
