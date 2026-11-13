package com.crackmydroid.ui.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SpacingTest {

    @Test
    fun testDefaultSpacingScaleProgression() {
        val spacing = AppSpacing()

        assertEquals(0.dp, spacing.none)
        assertTrue(spacing.none < spacing.xxs)
        assertTrue(spacing.xxs < spacing.extraSmall)
        assertTrue(spacing.extraSmall < spacing.small)
        assertTrue(spacing.small < spacing.medium)
        assertTrue(spacing.medium < spacing.default)
        assertTrue(spacing.default < spacing.large)
        assertTrue(spacing.large < spacing.extraLarge)
        assertTrue(spacing.extraLarge < spacing.huge)
    }

    @Test
    fun testSemanticDefaults() {
        val spacing = AppSpacing()

        assertEquals(spacing.default, spacing.screenHorizontal)
        assertEquals(spacing.default, spacing.screenVertical)
        assertEquals(spacing.large, spacing.screenHorizontalWide)
        assertEquals(spacing.medium, spacing.itemSpacing)
        assertEquals(spacing.small, spacing.rowSpacing)
        assertEquals(spacing.default, spacing.sectionSpacing)
        assertEquals(spacing.default, spacing.cardPaddingHorizontal)
        assertEquals(spacing.medium, spacing.cardPaddingVertical)
        assertEquals(16.dp, spacing.cardContentPadding.calculateLeftPadding(LayoutDirection.Ltr))
        assertEquals(12.dp, spacing.cardContentPadding.calculateTopPadding())
    }

    @Test
    fun testCalculateScreenContentPadding() {
        val spacing = AppSpacing(
            screenHorizontal = 16.dp,
            screenVertical = 20.dp,
        )
        val insets = PaddingValues(
            start = 8.dp,
            top = 24.dp,
            end = 12.dp,
            bottom = 16.dp,
        )

        val result = calculateScreenContentPadding(
            systemPadding = insets,
            horizontal = spacing.screenHorizontal,
            vertical = spacing.screenVertical,
            layoutDirection = LayoutDirection.Ltr,
        )

        assertEquals(24.dp, result.calculateLeftPadding(LayoutDirection.Ltr))
        assertEquals(28.dp, result.calculateRightPadding(LayoutDirection.Ltr))
        assertEquals(44.dp, result.calculateTopPadding())
        assertEquals(36.dp, result.calculateBottomPadding())
    }

    @Test
    fun testCalculateScreenContentPaddingRtl() {
        val spacing = AppSpacing(
            screenHorizontal = 10.dp,
            screenVertical = 10.dp,
        )
        val insets = PaddingValues(
            start = 5.dp,
            top = 0.dp,
            end = 15.dp,
            bottom = 0.dp,
        )

        val result = calculateScreenContentPadding(
            systemPadding = insets,
            horizontal = spacing.screenHorizontal,
            vertical = spacing.screenVertical,
            layoutDirection = LayoutDirection.Rtl,
        )

        // In RTL, start is right (5dp + 10dp = 15dp) and end is left (15dp + 10dp = 25dp)
        assertEquals(25.dp, result.calculateLeftPadding(LayoutDirection.Rtl))
        assertEquals(15.dp, result.calculateRightPadding(LayoutDirection.Rtl))
    }
}
