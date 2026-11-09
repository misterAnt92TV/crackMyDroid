package com.crackmydroid.shared.i18n

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNotEquals

class UseCasePresentationTextsTest {
    @Test
    fun reportLabelsAndFallbacksAreAvailableInEverySupportedLanguage() {
        val modes = mutableSetOf<String>()
        for (language in listOf("it", "en", "de", "es", "fr")) {
            val copy = UseCasePresentationTexts(language)
            modes += copy.automaticScan
            val labels = listOf(
                copy.automaticScan, copy.singleTarget, copy.allInstalledApps,
                copy.mode, copy.analyzedApps, copy.totalChecks, copy.errors,
                copy.error, copy.queued, copy.description, copy.outcome,
                copy.checkTarget, copy.details, copy.noChecks, copy.selectCheck,
                copy.noInstalledApps, copy.noPermissionApps, copy.noPackageData,
                copy.reportFailed, copy.permissionScanFailed, copy.checkFailed,
                copy.checksFailed, copy.scanFailed
            )
            assertTrue(labels.all { it.isNotBlank() }, language)
            assertTrue(copy.activitySurface("HIGH", 1).contains("HIGH"))
            assertTrue(copy.activitySurface("HIGH", 2).contains("2"))
            assertNotEquals(
                copy.activitySurface("HIGH", 1).substringAfter("(1 "),
                copy.activitySurface("HIGH", 2).substringAfter("(2 ")
            )
        }
        assertEquals(5, modes.size)
    }

    @Test
    fun unsupportedLanguageUsesEnglish() {
        val english = UseCasePresentationTexts("en")
        val unknown = UseCasePresentationTexts("xx")
        assertEquals(english.noChecks, unknown.noChecks)
        assertEquals(english.reportFailed, unknown.reportFailed)
        assertEquals(english.activitySurface("HIGH", 2), unknown.activitySurface("HIGH", 2))
    }
}
