package com.crackmydroid.shared.presentation.components

import androidx.compose.runtime.staticCompositionLocalOf

data class AccessibilityPrefs(
    val highContrast: Boolean = false,
    val reduceMotion: Boolean = false,
    val largeText: Boolean = false
)

fun effectiveReduceMotion(
    preferenceEnabled: Boolean,
    systemMotionReductionEnabled: Boolean
): Boolean = preferenceEnabled || systemMotionReductionEnabled

val LocalAccessibilityPrefs = staticCompositionLocalOf { AccessibilityPrefs() }
