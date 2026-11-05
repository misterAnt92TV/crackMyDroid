package com.crackmydroid.shared.presentation

import com.crackmydroid.shared.ui.settings.ThirdPartyLibraries
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ThirdPartyLibrariesTest {
    @Test
    fun catalogContainsRuntimeLibrariesAndExcludesTestOnlyDependencies() {
        val names = ThirdPartyLibraries.catalog.map { it.name }

        assertTrue(names.containsAll(listOf("Ktor Client", "SQLDelight", "Kotlinx Serialization", "WorkManager")))
        assertFalse(names.contains("MockK"))
    }

    @Test
    fun catalogHasStableOrderAndOfficialUrls() {
        val catalog = ThirdPartyLibraries.catalog

        assertEquals("Compose Multiplatform", catalog.first().name)
        assertEquals("WorkManager", catalog.last().name)
        assertTrue(catalog.all { it.url.startsWith("https://") })
        assertEquals(catalog.size, catalog.map { it.name }.toSet().size)
    }
}
