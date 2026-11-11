package com.crackmydroid.shared.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.crackmydroid.shared.presentation.components.text.AppText

@Composable
fun AppProgressIndicator(label: String, modifier: Modifier = Modifier) {
    if (LocalAccessibilityPrefs.current.reduceMotion) {
        AppText(label, modifier = modifier)
    } else {
        LinearProgressIndicator(modifier = modifier.fillMaxWidth())
    }
}
