package com.crackmydroid.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class CrackMyDroidThemeTest {

    @Test
    fun testLightAndDarkColorSchemes() {
        val light = CrackMyDroidLightColorScheme
        val dark = CrackMyDroidDarkColorScheme

        assertTrue(light.background.luminance() > 0.5f, "Light background should have high luminance")
        assertTrue(dark.background.luminance() < 0.5f, "Dark background should have low luminance")

        assertNotEquals(light.primary, dark.primary)
        assertEquals(Color.White, light.onPrimary)
        assertNotEquals(Color.White, dark.onPrimary)
    }

    @Test
    fun testHighContrastColorSchemes() {
        val hcLight = CrackMyDroidHighContrastLightColorScheme
        val hcDark = CrackMyDroidHighContrastDarkColorScheme

        assertEquals(Color.Black, hcLight.onBackground)
        assertEquals(Color.Black, hcLight.onSurface)

        assertEquals(Color.White, hcDark.onBackground)
        assertEquals(Color.White, hcDark.onSurface)
    }

    @Test
    fun testSemanticStatusColorsAreThemeSpecificAndLegible() {
        assertNotEquals(CrackMyDroidLightStatusColors.info, CrackMyDroidDarkStatusColors.info)
        assertNotEquals(CrackMyDroidLightStatusColors.successContainer, CrackMyDroidDarkStatusColors.successContainer)
        assertNotEquals(CrackMyDroidLightStatusColors.warningContainer, CrackMyDroidDarkStatusColors.warningContainer)
        assertNotEquals(CrackMyDroidLightStatusColors.infoContainer, CrackMyDroidLightStatusColors.info)
        assertNotEquals(CrackMyDroidDarkStatusColors.successContainer, CrackMyDroidDarkStatusColors.success)
    }

    @Test
    fun testTypographyHierarchy() {
        val typography = CrackMyDroidTypography

        assertTrue(typography.headlineLarge.fontSize.value > typography.headlineSmall.fontSize.value)
        assertTrue(typography.titleLarge.fontSize.value > typography.titleSmall.fontSize.value)
        assertTrue(typography.bodyLarge.fontSize.value > typography.bodySmall.fontSize.value)
        assertTrue(typography.labelLarge.fontSize.value >= typography.labelSmall.fontSize.value)
    }

    @Test
    fun testShapesProgression() {
        val shapes = CrackMyDroidShapes

        // Shapes must have progressive rounded corner radiuses
        assertTrue(shapes.small.topStart.toPx(androidx.compose.ui.geometry.Size(100f, 100f), androidx.compose.ui.unit.Density(1f)) > 0f)
        assertTrue(shapes.medium.topStart.toPx(androidx.compose.ui.geometry.Size(100f, 100f), androidx.compose.ui.unit.Density(1f)) >
            shapes.small.topStart.toPx(androidx.compose.ui.geometry.Size(100f, 100f), androidx.compose.ui.unit.Density(1f)))
        assertTrue(shapes.extraLarge.topStart.toPx(androidx.compose.ui.geometry.Size(100f, 100f), androidx.compose.ui.unit.Density(1f)) >
            shapes.large.topStart.toPx(androidx.compose.ui.geometry.Size(100f, 100f), androidx.compose.ui.unit.Density(1f)))
    }

    @Test
    fun testLogLevelColorsMapping() {
        val logColors = LogLevelColors()
        val defaultColor = Color.Magenta

        assertEquals(logColors.error, logColors.colorFor('E', defaultColor))
        assertEquals(logColors.warning, logColors.colorFor('W', defaultColor))
        assertEquals(logColors.info, logColors.colorFor('I', defaultColor))
        assertEquals(logColors.debug, logColors.colorFor('D', defaultColor))
        assertEquals(logColors.verbose, logColors.colorFor('V', defaultColor))
        assertEquals(defaultColor, logColors.colorFor('X', defaultColor))
        assertEquals(defaultColor, logColors.colorFor(null, defaultColor))
    }
}
