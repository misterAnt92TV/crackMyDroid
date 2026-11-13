package com.crackmydroid.shared.i18n

import com.crackmydroid.shared.domain.model.app.AppLanguage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
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
                strings.desktopCancel,
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
                strings.hideRecentOperationContentDescription,
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
        assertEquals(strings.desktopAdbPickerTitle, strings.app.desktopAdb.pickerTitle)
        assertEquals(strings.desktopDetectDevices, strings.app.desktopAdb.detectDevices)
        assertEquals(strings.desktopDataSourceLabel, strings.app.desktopAdb.dataSourceLabel)
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
                strings.permissionsExpandedAppLabel.format("Demo App", "com.example.demo"),
            )
        }
    }

    @Test
    fun componentPenTestLabels_andSearchHistoryAction_areLocalized() {
        val checkIds = listOf(
            "lib_scan",
            "manifest_flags",
            "file_scout",
            "network_monitor",
            "permission_risk",
        )
        val italian = stringsFor("it")
        val italianLabels = checkIds.map { italian.pentestChecks.getValue(it).first }

        listOf("it", "en", "de", "es", "fr").forEach { languageCode ->
            val strings = stringsFor(languageCode)
            checkIds.forEachIndexed { index, checkId ->
                val label = strings.pentestChecks[checkId]?.first.orEmpty()
                assertTrue(label.isNotBlank(), "$languageCode/$checkId")
                if (languageCode != "it") {
                    assertNotEquals(italianLabels[index], label, "$languageCode/$checkId still uses Italian")
                }
            }
            assertTrue(strings.clearSearchHistoryContentDescription.isNotBlank(), languageCode)
            if (languageCode != "it") {
                assertNotEquals(
                    italian.clearSearchHistoryContentDescription,
                    strings.clearSearchHistoryContentDescription,
                    "$languageCode/clearSearchHistoryContentDescription still uses Italian",
                )
            }
        }
    }

    @Test
    fun analyzeApkContentStrings_areLocalizedForEverySupportedLanguage() {
        listOf("it", "en", "de", "es", "fr").forEach { languageCode ->
            val strings = stringsFor(languageCode)
            val screenStrings = ScreenUiStrings(languageCode)

            assertTrue(strings.navAnalyzeApkContent.isNotBlank(), "$languageCode navAnalyzeApkContent is blank")
            assertTrue(strings.helpAnalyzeApkContent.isNotBlank(), "$languageCode helpAnalyzeApkContent is blank")

            listOf(
                screenStrings.apkContentStep1Title,
                screenStrings.apkContentStep1Desc,
                screenStrings.apkContentStep2Title,
                screenStrings.apkContentStep2Desc,
                screenStrings.apkContentStep3Title,
                screenStrings.apkContentStep3Desc,
                screenStrings.apkContentSelect,
                screenStrings.apkContentAnalyzingProgress,
                screenStrings.apkContentChangeApp,
                screenStrings.apkContentAnalyze,
                screenStrings.apkContentAnalyzing,
                screenStrings.apkContentExportReport,
                screenStrings.apkContentShareReport,
                screenStrings.apkContentAnalyzeAnother,
                screenStrings.apkContentReportTitle,
                screenStrings.apkContentMetricsTitle,
                screenStrings.apkContentMetricArchiveSize,
                screenStrings.apkContentMetricUncompressed,
                screenStrings.apkContentMetricTotalEntries,
                screenStrings.apkContentMetricDexFiles,
                screenStrings.apkContentMetricNativeLibs,
                screenStrings.apkContentMetricCerts,
                screenStrings.apkContentChecksumSha256,
                screenStrings.apkContentNoLibraries,
                screenStrings.apkContentNoFindings,
                screenStrings.apkContentDisclaimer,
                screenStrings.apkContentPlaintextFilterAll,
                screenStrings.apkContentPlaintextFilterEndpoints,
                screenStrings.apkContentPlaintextFilterConfig,
                screenStrings.apkContentPlaintextFilterStrings,
                screenStrings.apkContentNoPlaintext,
            ).forEach { value -> assertTrue(value.isNotBlank(), "$languageCode has blank APK content text") }

            assertTrue(screenStrings.apkContentRiskBadge.contains("%s"), languageCode)
            assertTrue(screenStrings.apkContentLibrariesTitle.contains("%d"), languageCode)
            assertTrue(screenStrings.apkContentFindingsTitle.contains("%d"), languageCode)
            assertTrue(screenStrings.apkContentPlaintextTitle.contains("%d"), languageCode)
            assertTrue(screenStrings.apkContentRemediationPrefix.contains("%s"), languageCode)
            assertTrue(screenStrings.apkContentExportedTo.contains("%s"), languageCode)
            assertTrue(screenStrings.apkContentSourceLabel.contains("%s"), languageCode)
        }
    }

    @Test
    fun activityUiStrings_areLocalizedForEverySupportedLanguage() {
        listOf("it", "en", "de", "es", "fr").forEach { languageCode ->
            val ui = stringsFor(languageCode).activityUi
            listOf(
                ui.searchToolsSubtitle, ui.suggestionsCleared, ui.exportSuccess, ui.frequentSearch,
                ui.resultsSummary, ui.noResults, ui.noActivities, ui.searchHint,
                ui.resetFilter, ui.refresh, ui.activityCount, ui.guidedLauncherTitle,
                ui.reportTitle, ui.totals, ui.riskSummary, ui.recommendations,
                ui.highlights, ui.exportHtml, ui.closeReport, ui.exportedTitle,
                ui.exportedSummary, ui.appDetail, ui.packageDetail, ui.activitiesDetail,
                ui.maximumRiskDetail, ui.riskLow, ui.riskMedium, ui.riskHigh, ui.riskCritical,
            ).forEach { value -> assertTrue(value.isNotBlank(), "$languageCode has blank Activity UI text") }
            assertTrue(ui.resultsSummary.contains("%d"), languageCode)
            assertTrue(ui.riskSummary.contains("%s"), languageCode)
            assertTrue(ui.noResults.contains("%s"), languageCode)
        }
    }
}
