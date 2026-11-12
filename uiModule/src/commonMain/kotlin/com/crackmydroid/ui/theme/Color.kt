package com.crackmydroid.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Palette tokens
internal object AppColorTokens {
    val BluePrimary = Color(0xFF1A73E8)
    val BluePrimaryContainerLight = Color(0xFFD2E3FC)
    val BlueOnPrimaryContainerLight = Color(0xFF0B3C74)

    val BluePrimaryDark = Color(0xFFA8C7FA)
    val BlueOnPrimaryDark = Color(0xFF053062)
    val BluePrimaryContainerDark = Color(0xFF174EA6)
    val BlueOnPrimaryContainerDark = Color(0xFFD8E2FF)

    val GreenSecondary = Color(0xFF188038)
    val GreenSecondaryContainerLight = Color(0xFFCEEAD6)
    val GreenOnSecondaryContainerLight = Color(0xFF0F3A1B)

    val GreenSecondaryDark = Color(0xFF81C995)
    val GreenOnSecondaryDark = Color(0xFF0B3819)
    val GreenSecondaryContainerDark = Color(0xFF0F5223)
    val GreenOnSecondaryContainerDark = Color(0xFFB7F1C3)

    val AmberTertiary = Color(0xFFF9AB00)
    val AmberOnTertiaryLight = Color(0xFF2F1F00)
    val AmberTertiaryContainerLight = Color(0xFFFEEFC3)
    val AmberOnTertiaryContainerLight = Color(0xFF4A3300)

    val AmberTertiaryDark = Color(0xFFFDD663)
    val AmberOnTertiaryDark = Color(0xFF443100)
    val AmberTertiaryContainerDark = Color(0xFF604600)
    val AmberOnTertiaryContainerDark = Color(0xFFFFE59A)

    val RedError = Color(0xFFD93025)
    val RedErrorContainerLight = Color(0xFFFDE8E7)
    val RedOnErrorContainerLight = Color(0xFF5C0A05)

    val RedErrorDark = Color(0xFFF28B82)
    val RedOnErrorDark = Color(0xFF690005)
    val RedErrorContainerDark = Color(0xFF8C1D18)
    val RedOnErrorContainerDark = Color(0xFFFFDAD6)

    val BackgroundLight = Color(0xFFF7F9FC)
    val OnBackgroundLight = Color(0xFF1C1F26)
    val SurfaceLight = Color.White
    val SurfaceVariantLight = Color(0xFFE9EDF4)
    val OnSurfaceVariantLight = Color(0xFF444A56)
    val OutlineLight = Color(0xFF737A88)
    val OutlineVariantLight = Color(0xFFC2C8D3)

    val BackgroundDark = Color(0xFF101418)
    val OnBackgroundDark = Color(0xFFE3E8F0)
    val SurfaceDark = Color(0xFF101418)
    val SurfaceVariantDark = Color(0xFF3F4652)
    val OnSurfaceVariantDark = Color(0xFFC0C7D2)
    val OutlineDark = Color(0xFF8A92A1)
    val OutlineVariantDark = Color(0xFF3F4652)
}

/** Semantic status colors keep product feedback consistent without local alpha overlays. */
data class StatusColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    val info: Color,
    val onInfo: Color,
    val infoContainer: Color,
    val onInfoContainer: Color,
)

val CrackMyDroidLightStatusColors = StatusColors(
    success = AppColorTokens.GreenSecondary,
    onSuccess = Color.White,
    successContainer = AppColorTokens.GreenSecondaryContainerLight,
    onSuccessContainer = AppColorTokens.GreenOnSecondaryContainerLight,
    warning = AppColorTokens.AmberTertiary,
    onWarning = AppColorTokens.AmberOnTertiaryLight,
    warningContainer = AppColorTokens.AmberTertiaryContainerLight,
    onWarningContainer = AppColorTokens.AmberOnTertiaryContainerLight,
    info = AppColorTokens.BluePrimary,
    onInfo = Color.White,
    infoContainer = AppColorTokens.BluePrimaryContainerLight,
    onInfoContainer = AppColorTokens.BlueOnPrimaryContainerLight,
)

val CrackMyDroidDarkStatusColors = StatusColors(
    success = AppColorTokens.GreenSecondaryDark,
    onSuccess = AppColorTokens.GreenOnSecondaryDark,
    successContainer = AppColorTokens.GreenSecondaryContainerDark,
    onSuccessContainer = AppColorTokens.GreenOnSecondaryContainerDark,
    warning = AppColorTokens.AmberTertiaryDark,
    onWarning = AppColorTokens.AmberOnTertiaryDark,
    warningContainer = AppColorTokens.AmberTertiaryContainerDark,
    onWarningContainer = AppColorTokens.AmberOnTertiaryContainerDark,
    info = AppColorTokens.BluePrimaryDark,
    onInfo = AppColorTokens.BlueOnPrimaryDark,
    infoContainer = AppColorTokens.BluePrimaryContainerDark,
    onInfoContainer = AppColorTokens.BlueOnPrimaryContainerDark,
)

val CrackMyDroidLightColorScheme: ColorScheme = lightColorScheme(
    primary = AppColorTokens.BluePrimary,
    onPrimary = Color.White,
    primaryContainer = AppColorTokens.BluePrimaryContainerLight,
    onPrimaryContainer = AppColorTokens.BlueOnPrimaryContainerLight,
    secondary = AppColorTokens.GreenSecondary,
    onSecondary = Color.White,
    secondaryContainer = AppColorTokens.GreenSecondaryContainerLight,
    onSecondaryContainer = AppColorTokens.GreenOnSecondaryContainerLight,
    tertiary = AppColorTokens.AmberTertiary,
    onTertiary = AppColorTokens.AmberOnTertiaryLight,
    tertiaryContainer = AppColorTokens.AmberTertiaryContainerLight,
    onTertiaryContainer = AppColorTokens.AmberOnTertiaryContainerLight,
    error = AppColorTokens.RedError,
    onError = Color.White,
    errorContainer = AppColorTokens.RedErrorContainerLight,
    onErrorContainer = AppColorTokens.RedOnErrorContainerLight,
    background = AppColorTokens.BackgroundLight,
    onBackground = AppColorTokens.OnBackgroundLight,
    surface = AppColorTokens.SurfaceLight,
    onSurface = AppColorTokens.OnBackgroundLight,
    surfaceVariant = AppColorTokens.SurfaceVariantLight,
    onSurfaceVariant = AppColorTokens.OnSurfaceVariantLight,
    outline = AppColorTokens.OutlineLight,
    outlineVariant = AppColorTokens.OutlineVariantLight,
)

val CrackMyDroidDarkColorScheme: ColorScheme = darkColorScheme(
    primary = AppColorTokens.BluePrimaryDark,
    onPrimary = AppColorTokens.BlueOnPrimaryDark,
    primaryContainer = AppColorTokens.BluePrimaryContainerDark,
    onPrimaryContainer = AppColorTokens.BlueOnPrimaryContainerDark,
    secondary = AppColorTokens.GreenSecondaryDark,
    onSecondary = AppColorTokens.GreenOnSecondaryDark,
    secondaryContainer = AppColorTokens.GreenSecondaryContainerDark,
    onSecondaryContainer = AppColorTokens.GreenOnSecondaryContainerDark,
    tertiary = AppColorTokens.AmberTertiaryDark,
    onTertiary = AppColorTokens.AmberOnTertiaryDark,
    tertiaryContainer = AppColorTokens.AmberTertiaryContainerDark,
    onTertiaryContainer = AppColorTokens.AmberOnTertiaryContainerDark,
    error = AppColorTokens.RedErrorDark,
    onError = AppColorTokens.RedOnErrorDark,
    errorContainer = AppColorTokens.RedErrorContainerDark,
    onErrorContainer = AppColorTokens.RedOnErrorContainerDark,
    background = AppColorTokens.BackgroundDark,
    onBackground = AppColorTokens.OnBackgroundDark,
    surface = AppColorTokens.SurfaceDark,
    onSurface = AppColorTokens.OnBackgroundDark,
    surfaceVariant = AppColorTokens.SurfaceVariantDark,
    onSurfaceVariant = AppColorTokens.OnSurfaceVariantDark,
    outline = AppColorTokens.OutlineDark,
    outlineVariant = AppColorTokens.OutlineVariantDark,
)

/**
 * High contrast variants with enhanced contrast for text and outlines.
 */
val CrackMyDroidHighContrastLightColorScheme: ColorScheme = CrackMyDroidLightColorScheme.copy(
    onBackground = Color.Black,
    onSurface = Color.Black,
    outline = Color(0xFF1C1F26),
)

val CrackMyDroidHighContrastDarkColorScheme: ColorScheme = CrackMyDroidDarkColorScheme.copy(
    onBackground = Color.White,
    onSurface = Color.White,
    outline = Color(0xFFE3E8F0),
)

/**
 * Centralized semantic colors for log entries.
 */
data class LogLevelColors(
    val error: Color = Color(0xFFFF6B6B),
    val warning: Color = Color(0xFFF4A261),
    val info: Color = Color(0xFF2A9D8F),
    val debug: Color = Color(0xFF4D6DE3),
    val verbose: Color = Color(0xFF9AA0A6),
) {
    fun colorFor(level: Char?, defaultColor: Color): Color = when (level) {
        'E' -> error
        'W' -> warning
        'I' -> info
        'D' -> debug
        'V' -> verbose
        else -> defaultColor
    }
}
