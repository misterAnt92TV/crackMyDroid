package com.crackmydroid.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import com.crackmydroid.ui.components.LocalAccessibilityPrefs

val LocalLogLevelColors = staticCompositionLocalOf { LogLevelColors() }

/**
 * Accessor object for CrackMyDroid theme tokens.
 */
object CrackMyDroidTheme {
    val colorScheme: ColorScheme
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme

    val typography: Typography
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.typography

    val shapes: Shapes
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.shapes

    val logColors: LogLevelColors
        @Composable
        @ReadOnlyComposable
        get() = LocalLogLevelColors.current
}

@Composable
fun CrackMyDroidTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    highContrast: Boolean = LocalAccessibilityPrefs.current.highContrast,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        highContrast && darkTheme -> CrackMyDroidHighContrastDarkColorScheme
        highContrast && !darkTheme -> CrackMyDroidHighContrastLightColorScheme
        darkTheme -> CrackMyDroidDarkColorScheme
        else -> CrackMyDroidLightColorScheme
    }
    val logColors = remember(darkTheme) { LogLevelColors() }

    CompositionLocalProvider(
        LocalLogLevelColors provides logColors,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = CrackMyDroidTypography,
            shapes = CrackMyDroidShapes,
            content = content,
        )
    }
}

