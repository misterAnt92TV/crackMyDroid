package com.crackmydroid.android

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

@Composable
fun rememberSystemMotionReduction(): Boolean {
    val context = LocalContext.current
    var enabled by remember(context) { mutableStateOf(context.systemMotionReductionEnabled()) }
    DisposableEffect(context) {
        val accessibilityManager = context.getSystemService(AccessibilityManager::class.java)
        val touchExplorationListener = AccessibilityManager.TouchExplorationStateChangeListener {
            enabled = context.systemMotionReductionEnabled()
        }
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                enabled = context.systemMotionReductionEnabled()
            }
        }
        accessibilityManager?.addTouchExplorationStateChangeListener(touchExplorationListener)
        context.contentResolver.registerContentObserver(
            Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE),
            false,
            observer,
        )
        onDispose {
            accessibilityManager?.removeTouchExplorationStateChangeListener(touchExplorationListener)
            context.contentResolver.unregisterContentObserver(observer)
        }
    }
    return enabled
}

private fun Context.systemMotionReductionEnabled(): Boolean {
    val touchExploration = getSystemService(AccessibilityManager::class.java)
        ?.isTouchExplorationEnabled == true
    val animationScale = runCatching {
        Settings.Global.getFloat(contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
    }.getOrDefault(1f)
    return touchExploration || animationScale == 0f
}
