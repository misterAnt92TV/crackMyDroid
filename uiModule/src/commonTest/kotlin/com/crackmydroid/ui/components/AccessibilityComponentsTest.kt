package com.crackmydroid.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.sp
import com.crackmydroid.ui.theme.CrackMyDroidHighContrastDarkColorScheme
import com.crackmydroid.ui.theme.CrackMyDroidHighContrastLightColorScheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AccessibilityComponentsTest {

    @Test
    fun testAccessibilityPrefsDefaults() {
        val prefs = AccessibilityPrefs()
        assertFalse(prefs.highContrast)
        assertFalse(prefs.reduceMotion)
        assertFalse(prefs.largeText)
    }

    @Test
    fun testEffectiveReduceMotionCombinations() {
        assertFalse(effectiveReduceMotion(preferenceEnabled = false, systemMotionReductionEnabled = false))
        assertTrue(effectiveReduceMotion(preferenceEnabled = true, systemMotionReductionEnabled = false))
        assertTrue(effectiveReduceMotion(preferenceEnabled = false, systemMotionReductionEnabled = true))
        assertTrue(effectiveReduceMotion(preferenceEnabled = true, systemMotionReductionEnabled = true))
    }

    @Test
    fun testHighContrastColorGuarantees() {
        val light = CrackMyDroidHighContrastLightColorScheme
        val dark = CrackMyDroidHighContrastDarkColorScheme

        assertEquals(Color.Black, light.onBackground)
        assertEquals(Color.Black, light.onSurface)

        assertEquals(Color.White, dark.onBackground)
        assertEquals(Color.White, dark.onSurface)
    }

    @Test
    fun testProportionalTextScalingCalculation() {
        val baseStyle = TextStyle(fontSize = 14.sp, lineHeight = 20.sp)
        val scale = 1.15f

        val scaledFontSize = baseStyle.fontSize * scale
        val scaledLineHeight = if (baseStyle.lineHeight.isSpecified) baseStyle.lineHeight * scale else baseStyle.lineHeight

        assertEquals(16.1.sp.value, scaledFontSize.value, 0.01f)
        assertEquals(23.0.sp.value, scaledLineHeight.value, 0.01f)
    }
}
