package com.crackmydroid.shared.presentation.components

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AccessibilityPrefsTest {
    @Test
    fun effectiveReduceMotion_isDisabledWhenNoSignalIsActive() {
        assertFalse(effectiveReduceMotion(false, false))
    }

    @Test
    fun effectiveReduceMotion_isEnabledByTheAppPreference() {
        assertTrue(effectiveReduceMotion(true, false))
    }

    @Test
    fun effectiveReduceMotion_isEnabledByTheSystemSignal() {
        assertTrue(effectiveReduceMotion(false, true))
    }
}
