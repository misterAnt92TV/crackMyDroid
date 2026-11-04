package com.crackmydroid.shared.i18n

import com.crackmydroid.shared.domain.model.app.AppLanguage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LanguageResolverTest {
    @Test
    fun systemCode_isRestoredAsSystemLanguage() {
        assertEquals(AppLanguage.SYSTEM, AppLanguage.fromCode("system"))
        assertEquals(AppLanguage.SYSTEM, AppLanguage.fromCode(null))
    }

    @Test
    fun explicitLanguageCode_isPreserved() {
        assertEquals(AppLanguage.ITALIAN, AppLanguage.fromCode("it-IT"))
        assertEquals("it", resolveLanguageCode(AppLanguage.ITALIAN, "de-DE"))
    }

    @Test
    fun systemLanguage_usesDeviceLanguageCode() {
        assertEquals("de", resolveLanguageCode(AppLanguage.SYSTEM, "de-DE"))
    }

    @Test
    fun unsupportedSystemLanguage_isPassedToStringsFallback() {
        assertEquals("xx", resolveLanguageCode(AppLanguage.SYSTEM, "xx-XX"))
        assertEquals("Settings", stringsFor(resolveLanguageCode(AppLanguage.SYSTEM, "xx-XX")).navSettings)
    }

    @Test
    fun desktopStrings_areAvailableForEverySupportedLanguage() {
        listOf("it", "en", "de", "es", "fr").forEach { languageCode ->
            val strings = stringsFor(languageCode)

            listOf(
                strings.desktopAdbPickerTitle,
                strings.desktopAdbPickerDescription,
                strings.desktopAdbPathLabel,
                strings.desktopAdbPathConfigured,
                strings.desktopDetectDevices,
                strings.desktopAdbSettings,
                strings.desktopNoDevices,
                strings.desktopSerialLabel,
                strings.desktopStateLabel,
                strings.desktopProductLabel,
                strings.desktopDeviceLabel,
                strings.desktopTransportLabel,
                strings.desktopDeviceOffline,
                strings.desktopDeviceUnauthorized,
                strings.desktopDeviceNotReady,
                strings.desktopChangeDevice,
                strings.desktopRescan,
                strings.desktopAdbSettingsDescription,
                strings.desktopSave,
                strings.desktopCancel
            ).forEach { desktopString -> assertTrue(desktopString.isNotBlank()) }
            assertTrue(strings.desktopDataSourceLabel.contains("%s"))
            listOf(
                strings.refreshActivitiesContentDescription,
                strings.refreshPermissionsContentDescription,
                strings.refreshAppsContentDescription,
                strings.shareActivitiesContentDescription,
                strings.sharePermissionsContentDescription,
                strings.shareAppsContentDescription,
                strings.shareApkContentDescription,
                strings.reportActivitiesContentDescription,
                strings.favoriteContentDescription,
                strings.shellUnavailableContentDescription,
                strings.shareDeviceInfoContentDescription,
                strings.clearSearchContentDescription,
                strings.clearSearchHistoryContentDescription,
                strings.scrollToTopContentDescription,
                strings.expandedStateDescription,
                strings.collapsedStateDescription,
                strings.penTestSuccessContentDescription,
                strings.penTestErrorContentDescription,
                strings.penTestQueuedContentDescription,
                strings.penTestPendingContentDescription,
                strings.hideRecentOperationContentDescription
            ).forEach { accessibilityString -> assertTrue(accessibilityString.isNotBlank()) }
            assertTrue(strings.removeSearchHistoryContentDescription.contains("%s"))
            assertTrue(strings.penTestProgressSummary.contains("%1${'$'}d"))
        }
    }

    @Test
    fun selectedLanguage_resolvesDesktopStringsImmediately() {
        val strings = stringsFor(resolveLanguageCode(AppLanguage.GERMAN, "it-IT"))

        assertEquals("ADB-Gerät auswählen", strings.desktopAdbPickerTitle)
        assertEquals("Datenquelle: Snapshot", strings.desktopDataSourceLabel.format("Snapshot"))
    }

    @Test
    fun operationResultLabel_isAvailableForEverySupportedLanguage() {
        listOf("it", "en", "de", "es", "fr").forEach { languageCode ->
            val strings = stringsFor(languageCode)
            assertTrue(strings.operationResultLabel.isNotBlank())
            assertTrue(strings.homeLatestOperationsEmpty.isNotBlank())
            assertTrue(strings.homeOperationHistoryTitle.isNotBlank())
            assertTrue(strings.homeOperationHistoryTitleWithCount.contains("%d"))
            assertTrue(strings.homeOperationHistoryTitleWithCount.format(0).contains("0"))
            assertTrue(strings.homeOperationHistoryTitleWithCount.format(3).contains("3"))
            assertEquals(
                "Demo App · com.example.demo",
                strings.permissionsExpandedAppLabel.format("Demo App", "com.example.demo")
            )
        }
    }
}
