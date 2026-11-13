package com.crackmydroid.ui.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Centralized spacing and padding design tokens for CrackMyDroid.
 * Provides a standardized scale and semantic tokens across Android and Desktop.
 */
@Immutable
data class AppSpacing(
    val none: Dp = 0.dp,
    val xxs: Dp = 2.dp,
    val extraSmall: Dp = 4.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 12.dp,
    val default: Dp = 16.dp,
    val large: Dp = 24.dp,
    val extraLarge: Dp = 32.dp,
    val huge: Dp = 48.dp,

    // Semantic layout and component spacing tokens
    val screenHorizontal: Dp = 16.dp,
    val screenVertical: Dp = 16.dp,
    val screenHorizontalWide: Dp = 24.dp,
    val itemSpacing: Dp = 12.dp,
    val rowSpacing: Dp = 8.dp,
    val sectionSpacing: Dp = 16.dp,
    val cardPaddingHorizontal: Dp = 16.dp,
    val cardPaddingVertical: Dp = 12.dp,
    val cardContentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
)

val LocalAppSpacing = staticCompositionLocalOf { AppSpacing() }

/**
 * Calculates uniform screen content padding combining system insets and app design spacing.
 */
@Composable
fun appScreenContentPadding(
    systemPadding: PaddingValues = PaddingValues(0.dp),
    horizontal: Dp = CrackMyDroidTheme.spacing.screenHorizontal,
    vertical: Dp = CrackMyDroidTheme.spacing.screenVertical,
): PaddingValues = calculateScreenContentPadding(
    systemPadding = systemPadding,
    horizontal = horizontal,
    vertical = vertical,
    layoutDirection = LocalLayoutDirection.current,
)

/**
 * Pure calculation helper for screen content padding given explicit layout direction.
 */
fun calculateScreenContentPadding(
    systemPadding: PaddingValues,
    horizontal: Dp,
    vertical: Dp,
    layoutDirection: androidx.compose.ui.unit.LayoutDirection,
): PaddingValues = PaddingValues(
    start = systemPadding.calculateStartPadding(layoutDirection) + horizontal,
    top = systemPadding.calculateTopPadding() + vertical,
    end = systemPadding.calculateEndPadding(layoutDirection) + horizontal,
    bottom = systemPadding.calculateBottomPadding() + vertical,
)
