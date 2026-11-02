package com.crackmydroid.shared.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1A73E8),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD2E3FC),
    onPrimaryContainer = Color(0xFF0B3C74),
    secondary = Color(0xFF188038),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCEEAD6),
    onSecondaryContainer = Color(0xFF0F3A1B),
    tertiary = Color(0xFFF9AB00),
    onTertiary = Color(0xFF2F1F00),
    tertiaryContainer = Color(0xFFFEEFC3),
    onTertiaryContainer = Color(0xFF4A3300),
    error = Color(0xFFD93025),
    onError = Color.White,
    errorContainer = Color(0xFFFDE8E7),
    onErrorContainer = Color(0xFF5C0A05),
    background = Color(0xFFF7F9FC),
    onBackground = Color(0xFF1C1F26),
    surface = Color.White,
    onSurface = Color(0xFF1C1F26),
    surfaceVariant = Color(0xFFE9EDF4),
    onSurfaceVariant = Color(0xFF444A56),
    outline = Color(0xFF737A88),
    outlineVariant = Color(0xFFC2C8D3)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFA8C7FA),
    onPrimary = Color(0xFF053062),
    primaryContainer = Color(0xFF174EA6),
    onPrimaryContainer = Color(0xFFD8E2FF),
    secondary = Color(0xFF81C995),
    onSecondary = Color(0xFF0B3819),
    secondaryContainer = Color(0xFF0F5223),
    onSecondaryContainer = Color(0xFFB7F1C3),
    tertiary = Color(0xFFFDD663),
    onTertiary = Color(0xFF443100),
    tertiaryContainer = Color(0xFF604600),
    onTertiaryContainer = Color(0xFFFFE59A),
    error = Color(0xFFF28B82),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF101418),
    onBackground = Color(0xFFE3E8F0),
    surface = Color(0xFF101418),
    onSurface = Color(0xFFE3E8F0),
    surfaceVariant = Color(0xFF3F4652),
    onSurfaceVariant = Color(0xFFC0C7D2),
    outline = Color(0xFF8A92A1),
    outlineVariant = Color(0xFF3F4652)
)

@Composable
fun CrackMyDroidTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        content = content
    )
}
