package com.crackmydroid.shared.util

import kotlin.test.Test
import kotlin.test.assertEquals

class TextExportAndroidTest {
    @Test
    fun shareMimeTypeMatchesExportFormat() {
        assertEquals("application/pdf", exportMimeType("PDF"))
        assertEquals("application/json", exportMimeType("json"))
        assertEquals("text/html", exportMimeType("html"))
        assertEquals("text/plain", exportMimeType("txt"))
    }
}
