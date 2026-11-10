package com.crackmydroid.shared.i18n

import com.crackmydroid.shared.i18n.operation.formatOperationTimestamp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals

class OperationTimestampFormatterTest {
    @Test
    fun invalidTimestampUsesStableFallback() {
        assertEquals("—", formatOperationTimestamp(0L, "it"))
    }

    @Test
    fun supportedAppLanguagesProduceVisibleDateAndTime() {
        listOf("it", "en", "de", "es", "fr").forEach { languageCode ->
            val formatted = formatOperationTimestamp(1_700_000_000_000L, languageCode)
            assertNotEquals("—", formatted)
            assertFalse(formatted.isBlank())
        }
    }
}
