package com.crackmydroid.shared.presentation.components

import androidx.compose.runtime.staticCompositionLocalOf

data class AccessibilityPrefs(
    val highContrast: Boolean = false,
    val reduceMotion: Boolean = false,
    val largeText: Boolean = false
)

val LocalAccessibilityPrefs = staticCompositionLocalOf { AccessibilityPrefs() }
