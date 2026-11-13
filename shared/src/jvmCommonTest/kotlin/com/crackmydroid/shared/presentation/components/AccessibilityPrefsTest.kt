package com.crackmydroid.shared.presentation.components

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import com.crackmydroid.ui.components.effectiveReduceMotion

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
