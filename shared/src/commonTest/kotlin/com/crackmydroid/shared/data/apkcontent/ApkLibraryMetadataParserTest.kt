package com.crackmydroid.shared.data.apkcontent

import com.crackmydroid.shared.domain.model.apkcontent.MavenLibrarySource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApkLibraryMetadataParserTest {
    @Test
    fun `prefers pom properties and deduplicates version metadata`() {
        val discovery = ApkLibraryMetadataParser.discover(
            packageName = "com.example.app",
            pomProperties = listOf("groupId=androidx.window\nartifactId=window\nversion=1.5.0"),
            versionEntries = listOf(
                ApkLibraryMetadataParser.VersionEntry("META-INF/androidx.window_window.version", "1.5.0"),
            ),
        )

        assertEquals(1, discovery.libraries.size)
        assertEquals(MavenLibrarySource.PomProperties, discovery.libraries.single().source)
        assertEquals("pkg:maven/androidx.window/window@1.5.0", discovery.libraries.single().packageUrl)
    }

    @Test
    fun `accepts trusted version metadata and ignores ambiguous entries`() {
        val discovery = ApkLibraryMetadataParser.discover(
            packageName = "com.example.app",
            pomProperties = emptyList(),
            versionEntries = listOf(
                ApkLibraryMetadataParser.VersionEntry("META-INF/androidx.window_window.version", "1.5.0"),
                ApkLibraryMetadataParser.VersionEntry("META-INF/com.google.android.material_material.version", "1.14.0"),
                ApkLibraryMetadataParser.VersionEntry("META-INF/kotlinx_coroutines_core.version", "1.11.0"),
                ApkLibraryMetadataParser.VersionEntry("META-INF/androidx.window_window.version", ""),
            ),
        )

        assertEquals(2, discovery.libraries.size)
        assertTrue(discovery.libraries.all { it.source == MavenLibrarySource.VersionMetadata })
        assertTrue(discovery.libraries.none { it.displayName.contains("kotlinx") })
    }
}
