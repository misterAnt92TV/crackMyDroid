package com.crackmydroid.ui.components.progress

import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppProgressBarTest {

    @Test
    fun testProgressBarVariantValues() {
        val variants = ProgressBarVariant.values()
        assertEquals(5, variants.size)
        assertTrue(variants.contains(ProgressBarVariant.Primary))
        assertTrue(variants.contains(ProgressBarVariant.Success))
        assertTrue(variants.contains(ProgressBarVariant.Error))
        assertTrue(variants.contains(ProgressBarVariant.Warning))
        assertTrue(variants.contains(ProgressBarVariant.Info))
    }

    @Test
    fun testProgressClamping() {
        fun clamp(progress: Float): Float = progress.coerceIn(0f, 1f)

        assertEquals(0f, clamp(-0.5f))
        assertEquals(0f, clamp(0f))
        assertEquals(0.42f, clamp(0.42f))
        assertEquals(1f, clamp(1.0f))
        assertEquals(1f, clamp(1.5f))
    }

    @Test
    fun testPercentageFormatting() {
        fun formatPercentage(progress: Float): String {
            val clamped = progress.coerceIn(0f, 1f)
            return "${(clamped * 100).roundToInt()}%"
        }

        assertEquals("0%", formatPercentage(0f))
        assertEquals("0%", formatPercentage(-0.1f))
        assertEquals("45%", formatPercentage(0.454f))
        assertEquals("46%", formatPercentage(0.456f))
        assertEquals("100%", formatPercentage(1.0f))
        assertEquals("100%", formatPercentage(2.0f))
    }
}
