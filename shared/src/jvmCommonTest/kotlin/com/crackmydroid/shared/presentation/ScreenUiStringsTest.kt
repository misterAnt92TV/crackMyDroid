package com.crackmydroid.shared.presentation

import com.crackmydroid.shared.i18n.ScreenUiStrings
import com.crackmydroid.shared.i18n.stringsFor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class ScreenUiStringsTest {
    @Test
    fun allSupportedLanguagesProvideScreenCopy() {
        listOf("it", "en", "de", "es", "fr").forEach { language ->
            val strings = ScreenUiStrings.from(stringsFor(language))
            assertTrue(strings.searchFilters.isNotBlank())
            assertTrue(strings.confirmCommand.isNotBlank())
            assertTrue(strings.riskScanTitle.isNotBlank())
            assertTrue(strings.logTrimmed.isNotBlank())
            assertTrue(strings.exitConfirmTitle.isNotBlank())
            assertTrue(strings.exitConfirmMessage.isNotBlank())
            assertTrue(strings.exitConfirmButton.isNotBlank())
            assertTrue(strings.exitCancelButton.isNotBlank())
        }
    }

    @Test
    fun localizedTemplatesKeepTheirPlaceholders() {
        val strings = ScreenUiStrings.from(stringsFor("it"))

        assertEquals("Ricerca frequente: esempio", strings.frequentSearch.format("esempio"))
        assertEquals("Risultati: 2 / 5", strings.resultsSummary.format(2, 5))
        assertEquals("Exit code: 1", strings.exitCode.format(1))
        assertNotEquals(
            strings.searchFilters,
            ScreenUiStrings.from(stringsFor("en")).searchFilters,
        )
    }
}
