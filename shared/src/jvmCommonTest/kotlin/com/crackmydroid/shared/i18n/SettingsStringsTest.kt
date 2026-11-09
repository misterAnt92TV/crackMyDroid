package com.crackmydroid.shared.i18n

import com.crackmydroid.shared.domain.model.app.AppLanguage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class SettingsStringsTest {
    private val languages = listOf("it", "en", "de", "fr", "es")

    @Test
    fun settingsSections_haveExpectedTranslationsInEveryLanguage() {
        val expected = mapOf(
            "it" to listOf(
                "Suggerimenti di ricerca", "Dialoghi di ingresso alle sezioni", "Introduzione",
                "Formato di esportazione", "Informazioni app", "Nessun permesso richiesto",
            ),
            "en" to listOf(
                "Search suggestions", "Section introduction dialogs", "Introduction",
                "Export format", "App information", "No permissions requested",
            ),
            "de" to listOf(
                "Suchvorschläge", "Einführungsdialoge für Bereiche", "Einführung",
                "Exportformat", "App-Informationen", "Keine Berechtigungen angefordert",
            ),
            "fr" to listOf(
                "Suggestions de recherche", "Dialogues de présentation des sections", "Présentation",
                "Format d'exportation", "Informations sur l'application", "Aucune autorisation demandée",
            ),
            "es" to listOf(
                "Sugerencias de búsqueda", "Diálogos de introducción a las secciones", "Introducción",
                "Formato de exportación", "Información de la aplicación", "No se han solicitado permisos",
            ),
        )
        languages.forEach { code ->
            val strings = stringsFor(code)
            assertEquals(
                expected.getValue(code),
                listOf(
                    strings.settingsSearchSuggestionsTitle, strings.settingsSectionHintsTitle,
                    strings.settingsOnboardingTitle, strings.settingsExportFormatTitle,
                    strings.settingsAppInfoTitle, strings.settingsNoRequestedPermissions,
                ),
                code,
            )
        }
    }

    @Test
    fun previouslyHardcodedCopy_hasNoItalianFallbackOutsideItalian() {
        val italian = settingsCopy(stringsFor("it"))
        languages.forEach { code ->
            val copy = settingsCopy(stringsFor(code))
            assertEquals(20, copy.size)
            copy.forEach { (key, value) ->
                assertTrue(value.isNotBlank(), "$code/$key")
                if (code != "it" && key != "package") {
                    assertNotEquals(italian.getValue(key), value, "$code/$key still uses Italian")
                }
            }
        }
    }

    @Test
    fun hintDescriptions_quoteTheLocalizedDisableAction() {
        languages.forEach { code ->
            val strings = stringsFor(code)
            assertTrue(
                strings.settingsSectionHintsDescription.contains("\"${strings.sectionHintDisable}\""),
                code,
            )
        }
    }

    @Test
    fun exportOptions_preserveTechnicalFormatNames() {
        languages.forEach { code ->
            val strings = stringsFor(code)
            assertTrue(strings.settingsExportText.contains("(.txt)"), code)
            assertTrue(strings.settingsExportJson.contains("JSON"), code)
            assertTrue(strings.settingsAppSdk.contains("SDK"), code)
        }
    }

    @Test
    fun existingSettingsCopy_isAvailableIncludingHelpAndAccessibility() {
        languages.forEach { code ->
            val strings = stringsFor(code)
            listOf(
                strings.navSettings, strings.themeTitle, strings.light, strings.dark, strings.system,
                strings.languageTitle, strings.languageDescription, strings.languageEnglish,
                strings.languageItalian, strings.languageGerman, strings.languageFrench,
                strings.languageSpanish, strings.verboseTitle, strings.verboseSubtitle,
                strings.accessibilityTitle, strings.accessibilityDesc, strings.accessibilityContrast,
                strings.accessibilityReduceMotion, strings.accessibilityLargeText,
                strings.thirdPartyTitle, strings.thirdPartyIntro, strings.disclaimerTitle,
                strings.disclaimerBody, strings.helpSettings, strings.faqTitle, strings.close,
                strings.confirm, strings.helpContentDescription, strings.menuContentDescription,
                strings.scrollToTopContentDescription,
            ).forEach { assertTrue(it.isNotBlank(), code) }
        }
    }

    @Test
    fun explicitLanguage_usesSelectedSettingsCatalogInsteadOfSystemLanguage() {
        AppLanguage.entries.filter { it != AppLanguage.SYSTEM }.forEach { selected ->
            val systemLanguage = if (selected == AppLanguage.ITALIAN) "en-US" else "it-IT"
            val code = resolveLanguageCode(selected, systemLanguage)
            assertSame(stringsFor(selected.code), stringsFor(code))
        }
        assertEquals(
            "Export format",
            stringsFor(resolveLanguageCode(AppLanguage.ENGLISH, "it-IT")).settingsExportFormatTitle,
        )
        assertSame(stringsFor("en"), stringsFor(resolveLanguageCode(AppLanguage.SYSTEM, "xx-XX")))
        assertEquals("No permissions requested", stringsFor("xx").settingsNoRequestedPermissions)
    }

    private fun settingsCopy(strings: Strings): Map<String, String> = with(strings) {
        mapOf(
            "searchTitle" to settingsSearchSuggestionsTitle,
            "searchEnabled" to settingsSearchSuggestionsEnabled,
            "searchDescription" to settingsSearchSuggestionsDescription,
            "clearSuggestions" to settingsClearSuggestions,
            "hintsTitle" to settingsSectionHintsTitle,
            "hintsEnabled" to settingsSectionHintsEnabled,
            "hintsDescription" to settingsSectionHintsDescription,
            "introTitle" to settingsOnboardingTitle,
            "introEnabled" to settingsShowIntroOnLaunch,
            "introDescription" to settingsOnboardingDescription,
            "exportTitle" to settingsExportFormatTitle,
            "exportText" to settingsExportText,
            "exportJson" to settingsExportJson,
            "exportDescription" to settingsExportDescription,
            "appInfo" to settingsAppInfoTitle,
            "version" to settingsAppVersion,
            "package" to settingsAppPackage,
            "sdk" to settingsAppSdk,
            "permissions" to settingsRequestedPermissions,
            "noPermissions" to settingsNoRequestedPermissions,
        )
    }
}
