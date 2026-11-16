package com.crackmydroid.shared.data.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class SnapshotSanitizationTest {
    @Test
    fun redactsBearerTokensInLogLines() {
        val raw = "2026-11-16 10:00:00 [HTTP] Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.secretpayload.sig"
        val sanitized = sanitizeLogLine(raw)

        assertFalse(sanitized.contains("eyJhbGciOiJIUzI1NiJ9"))
        assertEquals("2026-11-16 10:00:00 [HTTP] Authorization: Bearer [REDACTED]", sanitized)
    }

    @Test
    fun redactsSecretKeyValuePairsInLogLines() {
        val raw = "Connecting to backend with api_key=abc123secret and token: xyz987"
        val sanitized = sanitizeLogLine(raw)

        assertFalse(sanitized.contains("abc123secret"))
        assertFalse(sanitized.contains("xyz987"))
        assertEquals("Connecting to backend with api_key=[REDACTED] and token=[REDACTED]", sanitized)
    }

    @Test
    fun leavesNormalLogLinesUnchanged() {
        val raw = "2026-11-16 10:00:00 [Main] Application initialized normally"
        val sanitized = sanitizeLogLine(raw)

        assertEquals(raw, sanitized)
    }
}
