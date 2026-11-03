package com.crackmydroid.shared.i18n

import com.crackmydroid.shared.domain.model.AppLanguage
import kotlin.test.Test
import kotlin.test.assertEquals

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
}
